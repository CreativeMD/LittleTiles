package team.creative.littletiles.client.tool.shaper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.Side;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleShaper;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer.BoxRenderResult;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.measure.LittleMeasurement;
import team.creative.littletiles.common.math.measure.LittleMeasurementSimpleBox;
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
    private CompletableFuture<BoxRenderResult> worker;
    private BoxRenderResult builtResult;
    
    public LittleToolShaper(ItemStack stack) {
        super(stack);
        this.shaper = (ILittleShaper) stack.getItem();
    }
    
    @Override
    public List<BuildingModeFeature> buildingFeatures() {
        var list = super.buildingFeatures();
        list.add(BuildingModeFeatures.TOGGLE_PROPORTIONAL_SCALING);
        return list;
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
    
    public void buildCache(PreviewRenderer renderer, LittleShape shape, LittleGrid grid, LittleShapeConfig config, boolean lines, boolean inside) {
        ShapeSelection sel = ShapeSelection.of(renderer.level(), grid, buildPositions(), inside);
        this.builtSelection = sel;
        this.builtLines = lines;
        worker = CompletableFuture.supplyAsync(() -> {
            var boxes = shape.build(sel, config);
            if (!boxes.isEmpty())
                return renderer.buildBoxes(PreviewRenderer.EMPTY, boxes, lines);
            return new BoxRenderResult(boxes, boxes.pos, null, null);
        }, Util.backgroundExecutor());
    }
    
    @Override
    protected void tickInternal(PreviewRenderer renderer) {
        var blockHit = renderer.blockHit();
        if (blockHit == null)
            return;
        var player = renderer.player();
        var level = renderer.level();
        var grid = shaper.getPositionGrid(player, stack);
        boolean lines = shaper.previewMode(player, stack) == PreviewMode.LINES;
        var in = shaper.getShape(stack);
        var shapeConfig = in.getConfig(player.registryAccess(), Side.CLIENT);
        var shape = in.shape;
        
        if (blockHit != null)
            last = new ShapePosition(player, PlacementHelper.getPosition(level, blockHit, grid), blockHit, false, shaper.previewInside(player, stack));
        
        if (built && (builtShape != shape || !ShapeRegistry.SHAPE_CONFIG_REGISTRY.equals(builtShapeConfig, shapeConfig,
            Side.CLIENT) || builtLines != lines || lastGrid != grid || hasPositionChanged()))
            removeCache();
        
        if (!built) {
            builtShape = shape;
            builtShapeConfig = shapeConfig;
            if (shaper.hasShape(player, stack) && (last != null || marked))
                buildCache(renderer, shape, grid, shapeConfig, lines, shaper.previewInside(player, stack));
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
    
    private boolean interact(PreviewRenderer renderer, ItemStack stack, BlockHitResult hit, boolean left) {
        var player = renderer.player();
        var level = renderer.level();
        boolean main = left == shaper.selectLeftClick(player, stack);
        
        if (!main && marked) {
            int index = renderer.select(positions);
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
                    boxes = result.boxes();
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
    public boolean onLeftClick(PreviewRenderer renderer, BlockHitResult hit) {
        return interact(renderer, stack, hit, true);
    }
    
    @Override
    public boolean onRightClick(PreviewRenderer renderer, BlockHitResult hit) {
        return interact(renderer, stack, hit, false);
    }
    
    public BoxRenderResult getShapeResult() {
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
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (marked)
            renderer.renderPositions(pose, cam, positions, x -> markedPosition == x);
        
        if (builtLines != lines || !built)
            return;
        
        var result = getShapeResult();
        MeshData mesh;
        BlockPos pos;
        
        if (result != null && result.data() != null) {
            mesh = result.data();
            pos = result.pos();
        } else {
            var selection = builtSelection;
            var builder = renderer.createTesselatorBuilder(lines);
            renderer.buildBox(pose, selection.overallBox.getRenderingBox(selection.grid), builder, 255, lines);
            mesh = builder.build();
            pos = selection.pos;
        }
        
        if (mesh != null)
            renderer.renderBoxes(cam, pos, lines, mesh);
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.applyModelViewMatrix();
    }
    
    @Override
    public boolean toolKeyPressed(PreviewRenderer renderer, KeyMapping key) {
        if (super.toolKeyPressed(renderer, key))
            return true;
        
        if (key == LittleTilesClient.KEY_MARK) {
            toggleMark();
            return true;
        }
        
        if (positions.size() > 1 && marked) {
            var facing = LittleTilesClient.facingFromKeybind(renderer.player(), key);
            if (facing != null) {
                positions.get(markedPosition).move(lastGrid, facing);
                return true;
            }
        }
        if (built && builtShapeConfig != null && builtShapeConfig.react(renderer.player(), key)) {
            var in = renderer.player().getMainHandItem().get(LittleTilesRegistry.SHAPE).configure(renderer.player().registryAccess(), builtShapeConfig, Side.CLIENT);
            LittleTiles.NETWORK.sendToServer(new ShapeConfigPacket(in));
            renderer.player().getMainHandItem().set(LittleTilesRegistry.SHAPE, in);
            removeCache();
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<LittleMeasurement> measurements() {
        var selection = builtSelection;
        return Arrays.asList(new LittleMeasurementSimpleBox(new LittleBoxAbsolute(selection.pos, selection.overallBox, selection.grid)));
    }
    
    @Override
    public void remove() {
        super.remove();
        clearPositions();
    }
    
}
