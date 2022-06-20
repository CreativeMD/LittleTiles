package team.creative.littletiles.common.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.second.InsideFixedHandler;
import team.creative.littletiles.common.placement.second.SecondModeHandler;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

/** PlacementPosition + Previews -> PlacementPreview (can be rendered) + Player/ Cause -> Placement */
public class PlacementPreview {
    
    public final LittleGroup previews;
    public final PlacementMode mode;
    public final PlacementPosition position;
    public final UUID levelUUID;
    public final LittleBoxAbsolute box;
    
    PlacementPreview(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box) {
        this(level instanceof ISubLevel ? ((ISubLevel) level).getHolder().getUUID() : null, previews, mode, position, box);
    }
    
    PlacementPreview(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box) {
        this.levelUUID = levelUUID;
        this.previews = previews;
        if (previews.hasStructureIncludeChildren() && mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = position;
        this.box = box;
    }
    
    PlacementPreview(Level level, LittleGroupAbsolute previews, PlacementMode mode, Facing facing) {
        this(level instanceof ISubLevel ? ((ISubLevel) level).getHolder().getUUID() : null, previews, mode, facing);
    }
    
    PlacementPreview(UUID levelUUID, LittleGroupAbsolute previews, PlacementMode mode, Facing facing) {
        this.levelUUID = levelUUID;
        this.previews = previews.group;
        if (this.previews.hasStructureIncludeChildren() && mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = new PlacementPosition(previews.pos, new LittleVecGrid(), facing);
        this.box = previews.getBox();
    }
    
    public Level getLevel(Entity entity) throws MissingAnimationException {
        Level level = entity.level;
        if (levelUUID != null) {
            LittleLevelEntity levelEntity = LittleAnimationHandlers.find(level.isClientSide, levelUUID);
            if (levelEntity == null)
                throw new MissingAnimationException(levelUUID);
            
            level = levelEntity.getFakeLevel();
        }
        return level;
    }
    
    public static PlacementPreview load(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box) {
        return new PlacementPreview(levelUUID, previews, mode, position, box);
    }
    
    public static PlacementPreview load(UUID levelUUID, PlacementMode mode, LittleGroupAbsolute previews, Facing facing) {
        return new PlacementPreview(levelUUID, previews, mode, facing);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview absolute(Level level, ItemStack stack, LittleGroupAbsolute previews, Facing facing) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        return new PlacementPreview(level, previews, iTile.getPlacementMode(stack), facing);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview absolute(Level level, PlacementMode mode, LittleGroupAbsolute previews, Facing facing) {
        return new PlacementPreview(level, previews, mode, facing);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview relative(Level level, ItemStack stack, PlacementPosition position, boolean low) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        return relative(level, stack, iTile.get(stack, low), position, LittleTilesClient.PREVIEW_RENDERER.isCentered(stack, iTile), LittleTilesClient.PREVIEW_RENDERER
                .isFixed(stack, iTile));
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview relative(Level level, ItemStack stack, PlacementPosition position, boolean low, boolean centered, boolean fixed) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        return relative(level, stack, iTile.get(stack, low), position, centered, fixed);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview relative(Level level, ItemStack stack, LittleGroup tiles, PlacementPosition position, boolean centered, boolean fixed) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        LittleGrid original = tiles.getGrid();
        PlacementMode mode = iTile.getPlacementMode(stack);
        
        if (!tiles.isEmpty() || tiles.hasChildren()) {
            
            tiles.forceSameGrid(position);
            LittleGrid grid = tiles.getGrid();
            
            LittleVec size = PlacementHelper.getSize(iTile, stack, tiles, original);
            
            List<SecondModeHandler> shifthandlers = new ArrayList<SecondModeHandler>();
            
            boolean singleMode = tiles.totalSize() == 1;
            
            if (singleMode) {
                shifthandlers.add(new InsideFixedHandler());
                centered = true;
            }
            
            LittleBox box = PlacementHelper.getTilesBox(position, size, centered, position.facing, mode);
            
            boolean canBePlaceFixed = false;
            
            if (fixed) {
                canBePlaceFixed = !singleMode && LittleAction.canPlaceInside(tiles, level, position.getPos(), mode.placeInside);
                
                if (!canBePlaceFixed)
                    for (int i = 0; i < shifthandlers.size(); i++)
                        box = shifthandlers.get(i).getBox(level, position.getPos(), grid, box);
            }
            
            PlacementPosition offset = new PlacementPosition(position.getPos(), grid, box.getMinVec(), position.facing);
            LittleVec internalOffset = PlacementHelper.getInternalOffset(iTile, stack, tiles, original);
            internalOffset.invert();
            offset.getVec().add(internalOffset);
            
            if ((canBePlaceFixed || (fixed && singleMode)) && mode.placeInside)
                if (position.getVec().get(position.facing.axis) % grid.count == 0)
                    offset.getVec().add(position.facing.opposite());
                
            return new PlacementPreview(level, tiles, mode, position, new LittleBoxAbsolute(position.getPos(), box, grid));
        }
        
        return null;
    }
    
    public Set<BlockPos> getPositions() {
        return previews.getPositions(position.getPos());
    }
    
    public PlacementPreview copy() {
        return new PlacementPreview(levelUUID, previews.copy(), mode, position.copy(), box.copy());
    }
    
    public void mirror(Axis axis, LittleBoxAbsolute box) {
        position.mirror(axis, box);
        previews.mirror(axis, box.getDoubledCenter(position.getPos()));
    }
}
