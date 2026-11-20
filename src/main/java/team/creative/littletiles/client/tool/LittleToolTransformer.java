package team.creative.littletiles.client.tool;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittleTransformer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.component.TransformMode;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxGrid;
import team.creative.littletiles.common.math.box.LittleBoxLevel;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.CornerCache;
import team.creative.littletiles.common.math.measure.LittleMeasurement;
import team.creative.littletiles.common.math.measure.LittleMeasurementSimpleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.action.ChangedElementPacket;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PreviewMode;

public class LittleToolTransformer extends LittleTool {
    
    private LittleBoxLevel first;
    
    private ILittleLevelContext lastContext;
    private ShapePosition last;
    
    private LittleBoxLevel box = null;
    private LittleBoxAbsolute[] corners = new LittleBoxAbsolute[BoxCorner.values().length];
    
    private TransformMode transformMode = TransformMode.DEFAULT;
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
        
        var placeLevel = renderer.blockHitLevel();
        var player = renderer.player();
        lastContext = renderer.blockHitContext();
        last = new ShapePosition(player, placeLevel, PlacementHelper.getPosition(placeLevel, blockHit, transformer.getPositionGrid(player, stack)), blockHit, false, transformer
                .previewInside(player, stack));
        transformMode = transformer.transformMode(player, stack);
    }
    
    protected LittleBoxLevel getLast() {
        var lastBox = new LittleBoxLevel(lastContext, last.box().copy());
        if (first != null && !first.context.is(lastContext))
            lastBox.convertTo(first.context);
        return lastBox;
    }
    
    protected LittleBoxLevel getSelectionBox() {
        if (first == null)
            return new LittleBoxLevel(lastContext, last.box().copy());
        var box = first.copy();
        box.include(getLast());
        return box;
    }
    
    protected void updateBox() {
        var context = box != null ? box.context : first.context;
        LittleBox overallBox = LittleBox.ofNothing();
        BlockPos pos = corners[0].pos;
        LittleGrid grid = corners[0].getGrid();
        LittleVec[] relativeCorners = new LittleVec[corners.length];
        for (int i = 0; i < corners.length; i++) {
            LittleBoxAbsolute corner = corners[i];
            LittleBoxGrid boxCorner = corner.getRelative(pos);
            
            if (corner.getGrid().count > grid.count) {
                for (int j = 0; j < i; j++)
                    relativeCorners[j].convertTo(grid, corner.getGrid());
                overallBox.convertTo(grid, corner.getGrid());
                grid = corner.getGrid();
            } else if (corner.getGrid().count < grid.count)
                boxCorner.convertTo(grid);
            
            var c = BoxCorner.values()[i];
            overallBox.growToIncludeCorner(boxCorner.getBox(), c);
            relativeCorners[i] = boxCorner.getBox().get(c);
        }
        
        LittleTransformableBox tBox = new LittleTransformableBox(overallBox, new int[1]);
        CornerCache cache = tBox.new CornerCache(false);
        
        for (int i = 0; i < relativeCorners.length; i++)
            cache.setAbsolute(BoxCorner.values()[i], relativeCorners[i]);
        
        int[] data = cache.getData();
        if (data.length == 1)
            box = new LittleBoxLevel(context, pos, overallBox, grid);
        else {
            tBox.setData(cache.getData());
            box = new LittleBoxLevel(context, pos, tBox, grid);
        }
    }
    
    @Override
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if ((transformer.previewMode(renderer.player(), stack) == PreviewMode.LINES) != lines)
            return;
        
        var renderBox = box != null ? box : getSelectionBox();
        var pos = renderBox.pos;
        
        renderer.renderLineBoxes(pose, cam, SingletonList.of(renderBox), null);
        
        if (box != null) {
            renderer.wrapPreviewRender(box.context, pos, cam, true, () -> {
                pose.pushPose();
                transformMode.render(renderer, pose, pos, marked, box, corners);
                pose.popPose();
            });
        }
    }
    
    @Override
    public boolean onMouseWheelClickBlock(PreviewRenderer renderer, Level level, BlockHitResult result) {
        BlockState state = renderer.blockHitLevel().getBlockState(result.getBlockPos());
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
    public boolean onRightClick(PreviewRenderer renderer, Level level, @Nullable BlockHitResult result) {
        if (LittleActionHandlerClient.isUsingSecondMode()) {
            first = null;
            box = null;
            Arrays.fill(corners, null);
            return true;
        }
        
        if (box == null) {
            if (first == null) {
                first = getLast();
                return true;
            }
            // Init transformable box mode
            LittleBoxLevel selectionBox = getSelectionBox();
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
                corners[i] = new LittleBoxAbsolute(selectionBox.pos, new LittleBox(vec), selectionBox.grid);
            }
            first = null;
            return true;
        }
        
        if (transformMode.move(renderer, marked, last, box, corners)) {
            updateBox();
            return true;
        }
        
        transformer.boxFinished(box.context.asLevel(renderer.level()), renderer.player(), stack, box);
        box = null;
        Arrays.fill(corners, null);
        return true;
    }
    
    @Override
    public boolean onLeftClick(PreviewRenderer renderer, Level level, BlockHitResult hit) {
        if (box != null) {
            marked = transformMode.select(renderer, box, corners);
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
                
                BoxCorner[] corners = transformMode.corners(marked);
                if (corners == null)
                    return false;
                
                var offset = new LittleVecGrid(vec, grid);
                
                for (int i = 0; i < corners.length; i++)
                    this.corners[corners[i].ordinal()].move(offset);
                
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
    
    @Override
    public List<BuildingModeFeature> buildingFeatures() {
        List<BuildingModeFeature> features = super.buildingFeatures();
        features.add(BuildingModeFeatures.CYCLE_TRANSFORM_MODE);
        return features;
    }
    
}
