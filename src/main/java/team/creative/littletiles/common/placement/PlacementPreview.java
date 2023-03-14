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
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.LittleIngredients;
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
    
    public static PlacementPreview load(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box) {
        return new PlacementPreview(levelUUID, previews, mode, position, box, false);
    }
    
    public static PlacementPreview load(UUID levelUUID, PlacementMode mode, LittleGroupAbsolute previews, Facing facing) {
        return new PlacementPreview(levelUUID, previews, mode, facing, false);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview absolute(Level level, ItemStack stack, LittleGroupAbsolute previews, Facing facing) {
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        return new PlacementPreview(level, previews, iTile.getPlacementMode(stack), facing, false);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview absolute(Level level, PlacementMode mode, LittleGroupAbsolute previews, Facing facing) {
        return new PlacementPreview(level, previews, mode, facing, false);
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
        if (tiles == null)
            return null;
        
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        LittleGrid original = tiles.getGrid();
        PlacementMode mode = iTile.getPlacementMode(stack);
        
        if (!tiles.isEmpty() || tiles.hasChildren()) {
            
            tiles.forceSameGrid(position);
            LittleGrid grid = tiles.getGrid();
            
            LittleVec size = PlacementHelper.getSize(iTile, stack, tiles, original);
            
            List<SecondModeHandler> shifthandlers = new ArrayList<SecondModeHandler>();
            
            boolean singleMode = tiles.totalBoxes() == 1;
            
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
            
            PlacementPosition offset;
            if (fixed)
                offset = new PlacementPosition(position.getPos(), grid, new LittleVec(0, 0, 0), position.facing);
            else {
                offset = new PlacementPosition(position.getPos(), grid, box.getMinVec(), position.facing);
                LittleVec internalOffset = PlacementHelper.getInternalOffset(iTile, stack, tiles, original);
                internalOffset.invert();
                offset.getVec().add(internalOffset);
                
                if ((canBePlaceFixed || (fixed && singleMode)) && mode.placeInside)
                    if (position.getVec().get(position.facing.axis) % grid.count == 0)
                        offset.getVec().add(position.facing.opposite());
            }
            
            return new PlacementPreview(level, tiles, mode, offset, new LittleBoxAbsolute(offset.getPos(), box, grid), true);
        }
        
        return null;
    }
    
    public final LittleGroup previews;
    private final boolean canBeMoved;
    public final PlacementMode mode;
    public final PlacementPosition position;
    public final UUID levelUUID;
    public final LittleBoxAbsolute box;
    
    PlacementPreview(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box, boolean canBeMoved) {
        this(level instanceof ISubLevel ? ((ISubLevel) level).getHolder().getUUID() : null, previews, mode, position, box, canBeMoved);
    }
    
    PlacementPreview(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position, LittleBoxAbsolute box, boolean canBeMoved) {
        this.levelUUID = levelUUID;
        this.previews = previews;
        if (previews.hasStructureIncludeChildren() && !mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = position;
        this.box = box;
        this.canBeMoved = canBeMoved;
    }
    
    PlacementPreview(Level level, LittleGroupAbsolute previews, PlacementMode mode, Facing facing, boolean canBeMoved) {
        this(level instanceof ISubLevel ? ((ISubLevel) level).getHolder().getUUID() : null, previews, mode, facing, canBeMoved);
    }
    
    PlacementPreview(UUID levelUUID, LittleGroupAbsolute previews, PlacementMode mode, Facing facing, boolean canBeMoved) {
        this.levelUUID = levelUUID;
        this.previews = previews.group;
        if (this.previews.hasStructureIncludeChildren() && !mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = new PlacementPosition(previews.pos, new LittleVecGrid(), facing);
        this.box = previews.getBox();
        this.canBeMoved = canBeMoved;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void moveRelative(Entity entity, ItemStack stack, PlacementPosition position, boolean centered, boolean fixed) throws MissingAnimationException {
        if (!canBeMoved)
            return;
        
        Level level = getLevel(entity);
        
        ILittlePlacer iTile = PlacementHelper.getLittleInterface(stack);
        LittleGrid original = previews.getGrid();
        
        if (!previews.isEmpty() || previews.hasChildren()) {
            
            previews.forceSameGrid(position);
            LittleGrid grid = previews.getGrid();
            
            LittleVec size = PlacementHelper.getSize(iTile, stack, previews, original);
            
            List<SecondModeHandler> shifthandlers = new ArrayList<SecondModeHandler>();
            
            boolean singleMode = previews.totalBoxes() == 1;
            
            if (singleMode) {
                shifthandlers.add(new InsideFixedHandler());
                centered = true;
            }
            
            LittleBox box = PlacementHelper.getTilesBox(position, size, centered, position.facing, mode);
            
            boolean canBePlaceFixed = false;
            
            if (fixed) {
                canBePlaceFixed = !singleMode && LittleAction.canPlaceInside(previews, level, position.getPos(), mode.placeInside);
                
                if (!canBePlaceFixed)
                    for (int i = 0; i < shifthandlers.size(); i++)
                        box = shifthandlers.get(i).getBox(level, position.getPos(), grid, box);
            }
            
            PlacementPosition offset;
            if (fixed)
                offset = new PlacementPosition(position.getPos(), grid, new LittleVec(0, 0, 0), position.facing);
            else {
                offset = new PlacementPosition(position.getPos(), grid, box.getMinVec(), position.facing);
                LittleVec internalOffset = PlacementHelper.getInternalOffset(iTile, stack, previews, original);
                internalOffset.invert();
                offset.getVec().add(internalOffset);
                
                if ((canBePlaceFixed || (fixed && singleMode)) && mode.placeInside)
                    if (position.getVec().get(position.facing.axis) % grid.count == 0)
                        offset.getVec().add(position.facing.opposite());
            }
            
            this.position.assign(offset);
            this.box.set(offset.getPos(), box, grid);
        }
    }
    
    public Level getLevel(Entity entity) throws MissingAnimationException {
        Level level = entity.level;
        if (levelUUID != null) {
            LittleEntity levelEntity = LittleTiles.ANIMATION_HANDLERS.find(level.isClientSide, levelUUID);
            if (levelEntity == null)
                throw new MissingAnimationException(levelUUID);
            
            level = (Level) levelEntity.getSubLevel();
        }
        return level;
    }
    
    public Set<BlockPos> getPositions() {
        return previews.getPositions(position.getPos());
    }
    
    public PlacementPreview copy() {
        return new PlacementPreview(levelUUID, previews.copy(), mode, position.copy(), box.copy(), canBeMoved);
    }
    
    public void mirror(Axis axis, LittleBoxAbsolute box) {
        position.mirror(axis, box);
        previews.mirror(axis, box.getDoubledCenter(position.getPos()));
    }
    
    public LittleIngredients getBeforePlaceIngredients() {
        return mode.getBeforePlaceIngredients(previews);
    }
}
