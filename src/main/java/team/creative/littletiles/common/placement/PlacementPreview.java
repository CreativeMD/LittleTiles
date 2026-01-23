package team.creative.littletiles.common.placement;

import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

/** PlacementPosition + Previews -> PlacementPreview (can be rendered) + Player/ Cause -> Placement */
public class PlacementPreview {
    
    public static PlacementPreview create(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        return new PlacementPreview(level instanceof ISubLevel sub ? sub.getHolder().getUUID() : null, previews, mode, position);
    }
    
    public static PlacementPreview load(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        return new PlacementPreview(levelUUID, previews, mode, position);
    }
    
    public static PlacementPreview create(Level level, PlacementMode mode, LittleGroupAbsolute previews) {
        return new PlacementPreview(level instanceof ISubLevel sub ? sub.getHolder().getUUID() : null, previews, mode, null);
    }
    
    public static PlacementPreview load(UUID levelUUID, PlacementMode mode, LittleGroupAbsolute previews) {
        return new PlacementPreview(levelUUID, previews, mode, null);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview absolute(Level level, PlacementMode mode, LittleGroupAbsolute previews) {
        return new PlacementPreview(level, previews, mode, null);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static PlacementPreview relative(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        return new PlacementPreview(level, previews, mode, position);
    }
    
    public final LittleGroup previews;
    public final PlacementMode mode;
    public final PlacementPosition position;
    public final UUID levelUUID;
    
    PlacementPreview(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        this(level instanceof ISubLevel sub ? sub.getHolder().getUUID() : null, previews, mode, position);
    }
    
    PlacementPreview(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        this.levelUUID = levelUUID;
        this.previews = previews;
        if (previews.hasStructureIncludeChildren() && !mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = position;
    }
    
    PlacementPreview(Level level, LittleGroupAbsolute previews, PlacementMode mode, Facing facing) {
        this(level instanceof ISubLevel sub ? sub.getHolder().getUUID() : null, previews, mode, facing);
    }
    
    PlacementPreview(UUID levelUUID, LittleGroupAbsolute previews, PlacementMode mode, Facing facing) {
        this.levelUUID = levelUUID;
        this.previews = previews.group;
        
        this.mode = check(mode);
        this.position = new PlacementPosition(previews.pos, new LittleVecGrid(), facing);
    }
    
    public void validate() throws LittleActionException {}
    
    public Level getLevel(Level level) throws MissingAnimationException {
        if (levelUUID != null) {
            LittleEntity levelEntity = LittleTiles.ANIMATION_HANDLERS.find(level.isClientSide, levelUUID);
            if (levelEntity == null)
                throw new MissingAnimationException(levelUUID);
            
            level = (Level) levelEntity.getSubLevel();
        }
        return level;
    }
    
    protected PlacementMode check(PlacementMode mode) {
        if (this.previews.hasStructureIncludeChildren() && !mode.canPlaceStructures())
            return PlacementMode.getStructureDefault();
        return mode;
    }
    
    public Set<BlockPos> getPositions() {
        return previews.getPositions(position.getPos());
    }
    
    public PlacementPreview copy() {
        return new PlacementPreview(levelUUID, previews.copy(), mode, position.copy());
    }
    
    public void mirror(Axis axis, LittleBoxAbsolute box) {
        position.mirror(axis, box);
        previews.transform(axis.getMatrix(), box.getDoubledCenter(position.getPos()));
    }
    
    public LittleIngredients getBeforePlaceIngredients(HolderLookup.Provider provider) {
        return mode.getBeforePlaceIngredients(provider, previews);
    }
}
