package team.creative.littletiles.client.tool.shaper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.mc.TickUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleShaper;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.packet.item.ShapeConfigPacket;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PreviewMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.config.LittleShapeConfig;

public class LittleToolShaper extends LittleTool {
    
    public final ILittleShaper shaper;
    private final List<ShapePosition> positions = new ArrayList<>();
    private ShapePosition last;
    
    private LittleGrid lastGrid;
    
    private boolean marked;
    private int markedPosition;
    
    private boolean built = false;
    private boolean builtLines;
    private ShapeSelection builtSelection;
    private LittleShape builtShape;
    private LittleShapeConfig builtShapeConfig;
    private CompletableFuture<ShapeResult> worker;
    private ShapeResult builtResult;
    
    public LittleToolShaper(ItemStack stack) {
        super(stack);
        this.shaper = (ILittleShaper) stack.getItem();
    }
    
    protected void removeCache() {
        built = false;
        if (worker != null)
            worker.whenComplete((x, y) -> {
                if (x != null)
                    x.close();
            }); // Make sure the buffers are closed either way
        worker = null;
        if (builtResult != null)
            builtResult.close();
        builtResult = null;
        builtShape = null;
    }
    
    public void clearPositions() {
        marked = false;
        positions.clear();
        removeCache();
    }
    
    protected boolean hasPositionChanged() {
        if ((marked ? 0 : 1) + positions.size() != builtSelection.size())
            return true;
        for (int i = 0; i < positions.size(); i++)
            if (!positions.get(i).equals(builtSelection.get(i)))
                return true;
        if (!marked && !builtSelection.getLast().equals(last))
            return true;
        return false;
    }
    
    protected List<ShapePosition> buildPositions() {
        List<ShapePosition> list = new ArrayList<>(positions.size() + (marked ? 0 : 1));
        for (ShapePosition p : positions)
            list.add(p.copy());
        if (!marked)
            list.add(last.copy());
        return list;
    }
    
    public void buildCache(Level level, LittleShape shape, LittleGrid grid, LittleShapeConfig config, boolean lines, boolean inside) {
        ShapeSelection sel = ShapeSelection.of(level, grid, buildPositions(), inside);
        this.builtSelection = sel;
        this.builtLines = lines;
        worker = CompletableFuture.supplyAsync(() -> {
            PoseStack pose = new PoseStack();
            var boxes = shape.build(sel, config);
            ByteBufferBuilder buffer = null;
            MeshData mesh = null;
            if (!boxes.isEmpty()) {
                buffer = createBuffer();
                var builder = createBuilder(buffer, lines);
                for (LittleBox box : boxes.all()) {
                    LittleRenderBox cube = box.getRenderingBox(boxes.getGrid());
                    if (cube != null)
                        buildBox(pose, cube, builder, 255, lines);
                }
                mesh = builder.build();
                if (mesh instanceof MeshDataExtender m)
                    m.keepAlive(true);
                
            }
            return new ShapeResult(boxes, boxes.pos, buffer, mesh);
        }, Util.backgroundExecutor());
    }
    
    @Override
    public void tick(Level level, Player player, @Nullable BlockHitResult blockHit) {
        if (blockHit == null)
            return;
        var grid = shaper.getPositionGrid(player, stack);
        boolean lines = shaper.previewMode(player, stack) == PreviewMode.LINES;
        var in = shaper.getShape(stack);
        var shapeConfig = in.getConfig(player.registryAccess(), Side.CLIENT);
        var shape = in.shape;
        
        if (blockHit != null)
            last = new ShapePosition(player, PlacementHelper.getPosition(level, blockHit, grid, stack), blockHit, false, shaper.previewInside(player, stack));
        
        if (built && (builtShape != shape || !ShapeRegistry.SHAPE_CONFIG_REGISTRY.equals(builtShapeConfig, shapeConfig,
            Side.CLIENT) || builtLines != lines || lastGrid != grid || hasPositionChanged()))
            removeCache();
        
        if (!built) {
            builtShape = shape;
            builtShapeConfig = shapeConfig;
            if (shaper.hasShape(player, stack) && (last != null || marked))
                buildCache(level, shape, grid, shapeConfig, lines, shaper.previewInside(player, stack));
            else {
                if (builtResult != null)
                    builtResult.close();
                builtResult = null;
                builtShape = null;
                builtShapeConfig = null;
            }
            built = true;
        }
        
        lastGrid = grid;
    }
    
    public void toggleMark() {
        if (marked) {
            while (builtShape.maxAllowed() != -1 && positions.size() >= builtShape.maxAllowed())
                positions.remove(positions.size() - 1);
            markedPosition = positions.size() - 1;
            marked = false;
        } else {
            markedPosition = positions.size();
            positions.add(last);
            marked = true;
        }
    }
    
    private boolean interact(Level level, Player player, ItemStack stack, BlockHitResult hit, boolean left) {
        boolean main = left == shaper.selectLeftClick(player, stack);
        
        if (!main && marked) {
            int index = -1;
            double distance = Double.MAX_VALUE;
            float partialTickTime = TickUtils.getFrameTime(player.level());
            Vec3 pos = player.getEyePosition(partialTickTime);
            double reach = PlayerUtils.getReach(player);
            Vec3 view = player.getViewVector(partialTickTime);
            Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
            for (int i = 0; i < positions.size(); i++) {
                Optional<Vec3> result = positions.get(i).getBox().clip(pos, look);
                if (result.isPresent()) {
                    double tempDistance = pos.distanceToSqr(result.get());
                    if (tempDistance < distance) {
                        index = i;
                        distance = tempDistance;
                    }
                }
            }
            if (index != -1)
                markedPosition = index;
            return true;
        } else if (main) {
            if (LittleActionHandlerClient.isUsingSecondMode()) {
                clearPositions();
                return true;
            }
            
            if (builtShape == null)
                return false;
            
            boolean addPoint = (builtShape.pointsBeforePlacing > positions.size() + 1 || Screen.hasControlDown()) && (builtShape.maxAllowed() == -1 || builtShape
                    .maxAllowed() > positions.size() + 1);
            if (marked || !addPoint) {
                LittleBoxes boxes;
                var result = getShapeResult();
                if (result != null) // If result is already available take it otherwise built it once more
                    boxes = result.boxes;
                else
                    boxes = builtShape.build(ShapeSelection.of(level, shaper.getPositionGrid(player, stack), buildPositions(), shaper.previewInside(player, stack)),
                        builtShapeConfig);
                shaper.shapeFinished(level, player, stack, builtSelection, boxes);
                clearPositions();
                return true;
            } else if (hit != null) {
                positions.add(last.copy());
                return true;
            }
            
        }
        return false;
        
    }
    
    @Override
    public boolean onLeftClick(Level level, Player player, BlockHitResult hit) {
        return interact(level, player, stack, hit, true);
    }
    
    @Override
    public boolean onRightClick(Level level, Player player, BlockHitResult hit) {
        return interact(level, player, stack, hit, false);
    }
    
    public ShapeResult getShapeResult() {
        if (worker != null) {
            try {
                var temp = worker.get(10, TimeUnit.MILLISECONDS);
                if (builtResult != null)
                    builtResult.close();
                builtResult = temp;
                worker = null;
            } catch (InterruptedException | ExecutionException e) {
                worker = null;
            } catch (TimeoutException e) {}
        }
        return builtResult;
    }
    
    @Override
    public void render(Level level, Player player, PoseStack pose, Vec3 cam, boolean lines) {
        if (marked) {
            pose.pushPose();
            pose.translate(-cam.x, -cam.y, -cam.z);
            for (int i = 0; i < positions.size(); i++)
                positions.get(i).render(pose, markedPosition == i);
            pose.popPose();
        }
        
        if (builtLines != lines || !built)
            return;
        
        var result = getShapeResult();
        MeshData mesh;
        BlockPos pos;
        
        if (result != null && result.data != null) {
            mesh = result.data;
            pos = result.pos;
        } else {
            var selection = builtSelection;
            var builder = createTesselatorBuilder(lines);
            buildBox(pose, selection.overallBox.getRenderingBox(selection.grid), builder, 255, lines);
            mesh = builder.build();
            pos = selection.pos;
        }
        
        if (mesh != null) {
            var matrix = RenderSystem.getModelViewStack();
            matrix.pushMatrix();
            matrix.translate(pos.getX(), pos.getY(), pos.getZ());
            matrix.translate((float) -cam.x, (float) -cam.y, (float) -cam.z);
            
            RenderSystem.applyModelViewMatrix();
            setupPreviewRenderer(lines);
            
            BufferUploader.drawWithShader(mesh);
            matrix.popMatrix();
        }
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        
        RenderSystem.applyModelViewMatrix();
    }
    
    @Override
    public boolean keyPressed(Level level, Player player, KeyMapping key) {
        if (key == LittleTilesClient.KEY_MARK) {
            toggleMark();
            return true;
        }
        
        if (positions.size() > 1 && marked) {
            var facing = LittleTilesClient.facingFromKeybind(player, key);
            if (facing != null) {
                positions.get(markedPosition).move(lastGrid, facing);
                return true;
            }
        }
        if (built && builtShapeConfig != null && builtShapeConfig.react(player, key)) {
            var in = player.getMainHandItem().get(LittleTilesRegistry.SHAPE).configure(player.registryAccess(), builtShapeConfig, Side.CLIENT);
            LittleTiles.NETWORK.sendToServer(new ShapeConfigPacket(in));
            player.getMainHandItem().set(LittleTilesRegistry.SHAPE, in);
            removeCache();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void removed() {
        clearPositions();
    }
    
    public static record ShapeResult(LittleBoxes boxes, BlockPos pos, ByteBufferBuilder buffer, MeshData data) {
        
        public void close() {
            if (data instanceof MeshDataExtender m) {
                m.keepAlive(false);
                data.close();
            }
            if (buffer != null)
                buffer.close();
        }
    }
}
