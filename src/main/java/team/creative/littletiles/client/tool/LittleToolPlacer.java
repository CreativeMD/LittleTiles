package team.creative.littletiles.client.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxGrid;
import team.creative.littletiles.common.math.measure.LittleMeasurement;
import team.creative.littletiles.common.math.measure.LittleMeasurementSimpleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.item.PlacerMatrixPacket;
import team.creative.littletiles.common.placement.PlacementHelper;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PreviewMode;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.second.InsideFixedHandler;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleToolPlacer extends LittleTool {
    
    private final ILittlePlacer placer;
    
    private PlacementPosition marked = null;
    
    private boolean markedFixed;
    
    private ILittleLevelContext placedContext;
    private Level placedLevel;
    private PlacementPosition placedPosition;
    
    private PlacementPosition aimedPosition;
    private LittleGrid lastGrid;
    
    private boolean built = false;
    private boolean builtLines;
    private IntMatrix3c builtMatrix;
    private int builtHash;
    private LittleBoxGrid builtBox;
    private PlacementMode builtMode;
    private LittleVecGrid builtInternalOffset;
    private LittleVecGrid builtSize;
    private LittleGroup builtLowGroup;
    private boolean builtEmpty;
    private LittleGroupResult builtResult;
    private CompletableFuture<LittleGroupResult> worker;
    
    public LittleToolPlacer(ItemStack stack) {
        super(stack);
        this.placer = (ILittlePlacer) stack.getItem();
    }
    
    public boolean isCentered() {
        if (!placer.canSnapToGrid(stack))
            return marked == null;
        if (placer.snapToGridByDefault(stack))
            return LittleActionHandlerClient.isUsingSecondMode() && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid == LittleActionHandlerClient.isUsingSecondMode() || marked != null;
    }
    
    public boolean isFixed() {
        if (!placer.canSnapToGrid(stack))
            return marked != null;
        if (placer.snapToGridByDefault(stack))
            return !LittleActionHandlerClient.isUsingSecondMode() && marked == null;
        return LittleTiles.CONFIG.building.invertStickToGrid != LittleActionHandlerClient.isUsingSecondMode() && marked == null;
    }
    
    public PlacementPreview getPlacement() {
        return PlacementPreview.relative(placedLevel, builtResult.group, builtMode, placedPosition);
    }
    
    protected void buildCache(PreviewRenderer renderer, IntMatrix3c matrix, PlacementMode mode) {
        built = true;
        builtMode = mode;
        builtMatrix = matrix;
        builtInternalOffset = placer.getCachedMin(stack);
        builtSize = placer.getCachedSize(stack);
        builtEmpty = false;
        if (builtInternalOffset != null && builtSize != null)
            builtInternalOffset.sameGrid(builtSize, () -> {
                LittleVec max = builtSize.getVec().copy();
                max.add(builtInternalOffset.getVec());
                builtBox = new LittleBoxGrid(new LittleBox(builtInternalOffset.getVec(), max), builtInternalOffset.getGrid());
                builtBox.getBox().transform(matrix, builtBox.getGrid().rotationCenter);
                builtInternalOffset = builtBox.getMin();
                builtSize = builtBox.getSize();
            });
        
        builtLines = mode.getPreviewMode() == PreviewMode.LINES;
        var stackData = stack.get(LittleTilesRegistry.DATA);
        builtHash = stackData != null ? stackData.hashCode() : 0;
        
        worker = CompletableFuture.supplyAsync(() -> {
            var buffer = renderer.createBuffer();
            LittleGroup group = placer.get(stack, false);
            group.transform(matrix, group.getGrid().rotationCenter);
            
            BufferBuilder builder = renderer.createBuilder(buffer, builtLines);
            
            int colorAlpha = 255;
            
            for (RenderBox box : group.getPlaceBoxes(LittleVec.ZERO))
                renderer.buildBox(PreviewRenderer.EMPTY, box, builder, colorAlpha, builtLines);
            
            var data = builder.build();
            ((MeshDataExtender) data).keepAlive(true);
            return new LittleGroupResult(group, buffer, data);
        }, Util.backgroundExecutor());
        
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
        builtLowGroup = null;
        builtResult = null;
        builtMode = null;
        builtBox = null;
    }
    
    @Override
    protected void tickInternal(PreviewRenderer renderer) {
        var blockHit = renderer.blockHit();
        if (blockHit == null)
            return;
        var player = renderer.player();
        var level = marked != null ? placedLevel : renderer.blockHitLevel();
        var context = marked != null ? placedContext : renderer.blockHitContext();
        var grid = placer.getPositionGrid(player, stack);
        var pos = marked != null ? marked.copy() : PlacementHelper.getPosition(level, blockHit, grid);
        var mode = placer.getPlacementMode(stack);
        var matrix = placer.getMatrix(stack);
        var hasTiles = placer.hasTiles(stack);
        var hash = Optional.ofNullable(stack.get(LittleTilesRegistry.DATA)).map(CustomData::hashCode).orElse(0);
        
        if (built && (builtMode != mode || builtLines != (mode.getPreviewMode() == PreviewMode.LINES) || !Objects.equal(builtMatrix, matrix) || !hasTiles || builtHash != hash))
            removeCache();
        
        if (!built) {
            if (hasTiles)
                buildCache(renderer, matrix, mode);
            else {
                builtEmpty = true;
                if (builtResult != null || worker != null)
                    removeCache();
            }
            built = true;
        }
        
        aimedPosition = pos;
        lastGrid = grid;
        placedLevel = level;
        placedContext = context;
        if (checkForWorker())
            placedPosition = calculatePlacementPosition(level, builtResult.group);
        else if (checkForGroupLow())
            placedPosition = calculatePlacementPosition(level, null);
    }
    
    @Override
    public boolean toolKeyPressed(PreviewRenderer renderer, KeyMapping key) {
        if (super.toolKeyPressed(renderer, key))
            return true;
        if (key == LittleTilesClient.KEY_MARK) {
            if (marked == null) {
                markedFixed = LittleActionHandlerClient.isUsingSecondMode();
                PlacementPosition pos = aimedPosition.copy();
                if (markedFixed) {
                    var offset = pos.getMin();
                    offset.invert();
                    pos.move(offset);
                }
                marked = pos.copy();
            } else {
                markedFixed = false;
                marked = null;
            }
            return true;
        } else if (key == LittleTilesClient.KEY_UP) {
            if (marked != null)
                moveMarked(lastGrid, LittleActionHandlerClient.isUsingSecondMode() ? Facing.UP : Facing.EAST);
            else
                processTransform(renderer.player(), key, stack);
            return true;
        } else if (key == LittleTilesClient.KEY_DOWN) {
            if (marked != null)
                moveMarked(lastGrid, LittleActionHandlerClient.isUsingSecondMode() ? Facing.DOWN : Facing.WEST);
            else
                processTransform(renderer.player(), key, stack);
            return true;
        } else if (key == LittleTilesClient.KEY_RIGHT) {
            if (marked != null)
                moveMarked(lastGrid, Facing.SOUTH);
            else
                processTransform(renderer.player(), key, stack);
            return true;
        } else if (key == LittleTilesClient.KEY_LEFT) {
            if (marked != null)
                moveMarked(lastGrid, Facing.NORTH);
            else
                processTransform(renderer.player(), key, stack);
            return true;
        } else if (key == LittleTilesClient.KEY_MIRROR)
            processTransform(renderer.player(), key, stack);
        return false;
    }
    
    private void moveMarked(LittleGrid positionGrid, Facing facing) {
        marked.move(positionGrid, facing);
    }
    
    protected void processTransform(Player player, KeyMapping key, ItemStack stack) {
        PlacerMatrixPacket packet = new PlacerMatrixPacket(LittleTilesClient.fromKeybind(player, key));
        packet.executeClient(player);
        LittleTiles.NETWORK.sendToServer(packet);
        removeCache();
    }
    
    public boolean checkForWorker() {
        if (worker != null) {
            try {
                var temp = worker.get(10, TimeUnit.MILLISECONDS);
                if (builtResult != null && builtResult.data instanceof MeshDataExtender m) {
                    m.keepAlive(false);
                    builtResult.data.close();
                }
                if (builtInternalOffset == null)
                    builtInternalOffset = new LittleVecGrid(temp.group.getMinVec(), temp.group.getGrid());
                if (builtSize == null)
                    builtSize = new LittleVecGrid(temp.group.getSize(), temp.group.getGrid());
                builtResult = temp;
                worker = null;
            } catch (InterruptedException | ExecutionException e) {
                worker = null;
            } catch (TimeoutException e) {}
        }
        return builtResult != null;
        
    }
    
    public boolean checkForGroupLow() {
        if (builtBox == null || builtEmpty)
            return false;
        
        if (builtLowGroup == null) {
            builtLowGroup = new LittleGroup();
            builtLowGroup.add(builtBox, new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE));
        }
        
        return builtLowGroup != null;
        
    }
    
    public MeshData getMeshData(PreviewRenderer renderer, boolean lines) {
        if (checkForWorker())
            return builtResult.data;
        
        if (!checkForGroupLow())
            return null;
        
        var builder = renderer.createTesselatorBuilder(lines);
        for (RenderBox box : builtLowGroup.getPlaceBoxes(LittleVec.ZERO))
            renderer.buildBox(PreviewRenderer.EMPTY, box, builder, 255, lines);
        return builder.build();
    }
    
    @Override
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (this.builtLines != lines)
            return;
        
        var mesh = getMeshData(renderer, lines);
        if (mesh == null || placedPosition == null)
            return;
        
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        
        if (marked != null)
            renderer.renderLineBox(pose, marked.getBB(), true);
        
        var matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();
        renderer.setupPreviewRenderer(lines);
        
        placedContext.transformMatrix(matrix, placedPosition.getMinPosX(), placedPosition.getMinPosY(), placedPosition.getMinPosZ(), cam, renderer.partialTickTime());
        RenderSystem.applyModelViewMatrix();
        BufferUploader.drawWithShader(mesh);
        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
        
        if (builtResult != null) {
            int colorAlpha = 255;
            if (LittleActionHandlerClient.isUsingSecondMode() != placer.snapToGridByDefault(stack)) {
                List<RenderBox> cubes = getPositingCubes(placedLevel, aimedPosition.getPos());
                BufferBuilder builder = renderer.createTesselatorBuilder(lines);
                if (cubes != null)
                    for (RenderBox cube : cubes)
                        renderer.buildBox(pose, cube, builder, colorAlpha, lines);
                var data = builder.build();
                if (data != null)
                    BufferUploader.drawWithShader(data);
            }
        }
        
        pose.popPose();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        
    }
    
    private List<RenderBox> getPositingCubes(Level level, BlockPos pos) {
        if (builtResult.group.hasStructure()) {
            LittleStructureType type = builtResult.group.getStructureType();
            if (type != null)
                return type.getPositingCubes(level, pos, stack);
        }
        return null;
    }
    
    @Override
    public void remove() {
        super.remove();
        removeCache();
        placedContext = null;
        placedLevel = null;
    }
    
    public PlacementPosition calculatePlacementPosition(Level level, @Nullable LittleGroup group) {
        if (group != null && group.isEmptyIncludeChildren())
            return null;
        
        if (group != null)
            group.convertToSmallest();
        
        boolean centered = isCentered();
        boolean fixed = isFixed();
        boolean isMarked = marked != null;
        LittleVecGrid size = builtSize.copy();
        PlacementPosition pos = isMarked ? marked.copy() : aimedPosition.copy();
        
        if (group != null)
            group.forceSameGrid(pos, size);
        else
            pos.forceSameGrid(size);
        LittleGrid grid = pos.getGrid();
        
        boolean singleMode = group != null && group.totalBoxes() == 1;
        
        LittleBox box = PlacementHelper.getTilesBox(pos, size.getVec(), centered || singleMode, builtMode.placeInside);
        
        //this works for both single and group
        if (fixed || (isMarked && markedFixed))
            if (LittleAction.canPlaceInside(level, pos.getPos(), builtMode.placeInside)) {
                var offset = new PlacementPosition(pos.getPos(), isMarked ? pos.getMin() : new LittleVecGrid(), pos.facing);
                if (singleMode) {
                    LittleVecGrid internalOffset = this.builtInternalOffset.copy();
                    internalOffset.invert();
                    offset.move(internalOffset);
                }
                return offset;
            }
        
        PlacementPosition offset = new PlacementPosition(pos.getPos(), grid, box.getMinVec(), pos.facing);
        LittleVecGrid internalOffset = this.builtInternalOffset.copy();
        internalOffset.invert();
        offset.move(internalOffset);
        
        if (group != null)
            group.convertToSmallest();
        return offset;
    }
    
    protected PlacementPosition singleModePosition(Level level, LittleBox box, PlacementPosition pos, LittleGrid grid) {
        var fixedHandler = new InsideFixedHandler();
        box = fixedHandler.getBox(level, pos.getPos(), grid, box);
        
        PlacementPosition offset = new PlacementPosition(pos.getPos(), grid, box.getMinVec(), pos.facing);
        LittleVecGrid internalOffset = this.builtInternalOffset.copy();
        internalOffset.invert();
        offset.move(internalOffset);
        
        offset.convertTo(grid);
        return offset;
    }
    
    protected PlacementPosition groupModePosition(Level level, LittleBox box, PlacementPosition pos, LittleGrid grid) {
        PlacementPosition offset = new PlacementPosition(pos.getPos(), grid, box.getMinVec(), pos.facing);
        LittleVecGrid internalOffset = this.builtInternalOffset.copy();
        internalOffset.invert();
        offset.move(internalOffset);
        
        offset.convertTo(grid);
        return offset;
        
    }
    
    @Override
    public boolean onRightClick(PreviewRenderer renderer, Level level, BlockHitResult result) {
        if (!placer.hasTiles(stack))
            return false;
        
        markedFixed = false;
        if (!built)
            return false;
        if (!checkForWorker()) {
            if (builtEmpty || worker == null)
                return false;
            builtResult = worker.join();
        }
        if (LittleTilesClient.INTERACTION.start(true)) {
            PlacementPreview preview = getPlacement();
            if (preview == null)
                return true;
            LittleTilesClient.ACTION_HANDLER.execute(new LittleActionPlace(PlaceAction.PLACER, preview));
            marked = null;
            placedContext = null;
        }
        removeCache();
        return true;
    }
    
    @Override
    public List<LittleMeasurement> measurements() {
        if (!built)
            return null;
        var absoluteBox = new LittleBoxAbsolute(placedPosition.getPos(), builtBox.getBox().copy(), builtBox.getGrid());
        absoluteBox.sameGrid(placedPosition, () -> absoluteBox.box.add(placedPosition.box().box.getMinVec()));
        return Arrays.asList(new LittleMeasurementSimpleBox(absoluteBox));
    }
    
    public static record LittleGroupResult(LittleGroup group, ByteBufferBuilder buffer, MeshData data) {
        
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
