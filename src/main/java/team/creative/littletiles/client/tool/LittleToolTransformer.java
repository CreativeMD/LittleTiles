package team.creative.littletiles.client.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleTransformer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.measure.LittleMeasurement;
import team.creative.littletiles.common.math.measure.LittleMeasurementSimpleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.packet.action.ChangedElementPacket;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PreviewMode;

public class LittleToolTransformer extends LittleTool {
    
    private ShapePosition first;
    private ShapePosition last;
    
    private LittleBoxAbsolute box = null;
    private LittleVecAbsolute[] corners = new LittleVecAbsolute[BoxCorner.values().length];
    private int marked = -1;
    
    private ILittleTransformer transformer;
    
    public LittleToolTransformer(ItemStack stack) {
        super(stack);
        this.transformer = (ILittleTransformer) stack.getItem();
    }
    
    @Override
    protected void tickInternal(PreviewRenderer renderer) {
        var blockHit = renderer.blockHit();
        if (blockHit == null)
            return;
        
        var player = renderer.player();
        last = new ShapePosition(player, PlacementHelper.getPosition(renderer.level(), blockHit, transformer.getPositionGrid(player, stack)), blockHit, false, transformer
                .previewInside(player, stack));
    }
    
    protected LittleBoxAbsolute getSelectionBox() {
        LittleGrid grid;
        BlockPos pos = last.getPos();
        LittleBox selectionBox = LittleBox.ofNothing();
        if (first != null) {
            grid = first.unsafeSameGridRestore(last, () -> {
                selectionBox.growToIncludePixel(first.getRelative(pos));
                selectionBox.growToIncludePixel(last.getRelative(pos));
                return first.getGrid();
            });
        } else {
            selectionBox.growToIncludePixel(last.getRelative(pos));
            grid = last.getGrid();
        }
        return new LittleBoxAbsolute(pos, selectionBox, grid);
    }
    
    protected void updateBox() {
        LittleBox overallBox = LittleBox.ofNothing();
        BlockPos pos = corners[0].getPos();
        LittleGrid grid = corners[0].getGrid();
        LittleVec[] relativeCorners = new LittleVec[corners.length];
        for (int i = 0; i < corners.length; i++) {
            LittleVecAbsolute corner = corners[i];
            LittleVec vec = corner.getRelative(pos);
            
            if (corner.getGrid().count > grid.count) {
                for (int j = 0; j < i; j++)
                    relativeCorners[j].convertTo(grid, corner.getGrid());
                overallBox.convertTo(grid, corner.getGrid());
                grid = corner.getGrid();
            } else if (corner.getGrid().count < grid.count)
                vec.convertTo(corner.getGrid(), grid);
            
            overallBox.growToIncludePixel(vec);
            BoxCorner c = BoxCorner.values()[i];
            if (c.x.positive)
                vec.x += 1;
            if (c.y.positive)
                vec.y += 1;
            if (c.z.positive)
                vec.z += 1;
            relativeCorners[i] = vec;
        }
        
        LittleTransformableBox tBox = new LittleTransformableBox(overallBox, new int[1]);
        CornerCache cache = tBox.new CornerCache(false);
        
        for (int i = 0; i < relativeCorners.length; i++)
            cache.setAbsolute(BoxCorner.values()[i], relativeCorners[i]);
        
        int[] data = cache.getData();
        if (data.length == 1)
            box = new LittleBoxAbsolute(pos, overallBox, grid);
        else {
            tBox.setData(cache.getData());
            box = new LittleBoxAbsolute(pos, tBox, grid);
        }
    }
    
    @Override
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if ((transformer.previewMode(renderer.player(), stack) == PreviewMode.LINES) != lines)
            return;
        
        MeshData mesh;
        BlockPos pos;
        var builder = renderer.createTesselatorBuilder(lines);
        if (box != null) {
            pose.pushPose();
            pose.translate((float) -cam.x, (float) -cam.y, (float) -cam.z);
            for (int i = 0; i < corners.length; i++)
                renderer.renderLineBox(pose, corners[i].getBB(), marked == i);
            pose.popPose();
            
            renderer.buildBox(pose, box.getRenderingBoxWithoutOffset(), builder, 255, lines);
            mesh = builder.build();
            pos = box.pos;
        } else {
            if (last == null)
                return;
            
            LittleBoxAbsolute selectionBox = getSelectionBox();
            renderer.buildBox(pose, selectionBox.getRenderingBoxWithoutOffset(), builder, 255, lines);
            pos = selectionBox.pos;
            mesh = builder.build();
        }
        
        if (mesh != null) {
            var matrix = RenderSystem.getModelViewStack();
            matrix.pushMatrix();
            matrix.translate((float) (pos.getX() - cam.x), (float) (pos.getY() - cam.y), (float) (pos.getZ() - cam.z));
            
            RenderSystem.applyModelViewMatrix();
            renderer.setupPreviewRenderer(lines);
            
            BufferUploader.drawWithShader(mesh);
            matrix.popMatrix();
        }
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        
        RenderSystem.applyModelViewMatrix();
    }
    
    @Override
    public boolean onMouseWheelClickBlock(PreviewRenderer renderer, BlockHitResult result) {
        BlockState state = renderer.level().getBlockState(result.getBlockPos());
        if (LittleAction.isBlockValid(state)) {
            LittleTiles.NETWORK.sendToServer(new ChangedElementPacket(new LittleElement(state, ColorUtils.WHITE)));
            return true;
        } else if (state.getBlock() instanceof BlockTile) {
            LittleTileContext context = renderer.selectFocused(result);
            if (context.isComplete())
                LittleTiles.NETWORK.sendToServer(new ChangedElementPacket(context.tile));
            return true;
        }
        return false;
    }
    
    @Override
    public boolean onRightClick(PreviewRenderer renderer, @Nullable BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode()) {
            first = null;
            box = null;
            Arrays.fill(corners, null);
            return true;
        }
        
        if (box == null) {
            if (first == null) {
                first = last.copy();
                return true;
            }
            // Init transformable box mode
            LittleBoxAbsolute selectionBox = getSelectionBox();
            box = selectionBox;
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = BoxCorner.values()[i];
                var vec = selectionBox.box.get(corner);
                if (corner.x.positive)
                    vec.x -= 1;
                if (corner.y.positive)
                    vec.y -= 1;
                if (corner.z.positive)
                    vec.z -= 1;
                corners[i] = new LittleVecAbsolute(selectionBox.pos, selectionBox.grid, vec);
            }
            first = null;
            return true;
        }
        
        if (marked > -1) {
            // Move corner
            corners[marked] = last;
            updateBox();
            return true;
        }
        
        transformer.boxFinished(renderer.level(), renderer.player(), stack, box);
        box = null;
        Arrays.fill(corners, null);
        return true;
    }
    
    @Override
    public boolean onLeftClick(PreviewRenderer renderer, BlockHitResult hit) {
        if (box != null) {
            int index = -1;
            double distance = Double.MAX_VALUE;
            var player = renderer.player();
            float partialTickTime = renderer.partialTickTime();
            Vec3 pos = player.getEyePosition(partialTickTime);
            double reach = PlayerUtils.getReach(player);
            Vec3 view = player.getViewVector(partialTickTime);
            Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
            for (int i = 0; i < corners.length; i++) {
                Optional<Vec3> result = corners[i].getBox().clip(pos, look);
                if (result.isPresent()) {
                    double tempDistance = pos.distanceToSqr(result.get());
                    if (tempDistance < distance) {
                        index = i;
                        distance = tempDistance;
                    }
                }
            }
            marked = index;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean toolKeyPressed(PreviewRenderer renderer, KeyMapping key) {
        if (super.toolKeyPressed(renderer, key))
            return true;
        
        if (box != null && marked > -1) {
            var facing = LittleTilesClient.facingFromKeybind(renderer.player(), key);
            if (facing != null) {
                LittleGrid grid = transformer.getPositionGrid(renderer.player(), stack);
                LittleVec vec = new LittleVec(facing);
                vec.scale(Screen.hasControlDown() ? grid.count : 1);
                corners[marked].add(new LittleVecAbsolute(BlockPos.ZERO, grid, vec));
                if (corners[marked].getGrid().count < grid.count)
                    corners[marked].convertTo(grid);
                updateBox();
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public List<LittleMeasurement> measurements() {
        if (box == null)
            return null;
        return Arrays.asList(new LittleMeasurementSimpleBox(box.copy()));
    }
    
}
