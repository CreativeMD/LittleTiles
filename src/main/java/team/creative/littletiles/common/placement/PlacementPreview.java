package team.creative.littletiles.common.placement;

import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.level.WorldAnimationHandler;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.exception.MissingAnimationException;

/** PlacementPosition + Previews -> PlacementPreview (can be rendered) + Player/ Cause -> Placement */
public class PlacementPreview {
    
    public final LittleGroup previews;
    public final PlacementMode mode;
    public final PlacementPosition position;
    public final UUID levelUUID;
    
    public PlacementPreview(Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        if (level instanceof CreativeLevel)
            this.levelUUID = ((CreativeLevel) level).parent.getUUID();
        else
            this.levelUUID = null;
        this.previews = previews;
        if (previews.hasStructureIncludeChildren() && mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = position;
    }
    
    public PlacementPreview(UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        this.levelUUID = levelUUID;
        this.previews = previews;
        if (previews.hasStructureIncludeChildren() && mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = position;
    }
    
    public PlacementPreview(UUID levelUUID, LittleGroupAbsolute previews, PlacementMode mode, Facing facing) {
        this.levelUUID = levelUUID;
        this.previews = previews.group;
        if (this.previews.hasStructureIncludeChildren() && mode.canPlaceStructures())
            mode = PlacementMode.getStructureDefault();
        this.mode = mode;
        this.position = new PlacementPosition(previews.pos, new LittleVecGrid(), facing);
    }
    
    public Level getLevel(Entity entity) throws MissingAnimationException {
        Level level = entity.level;
        if (levelUUID != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(level.isClientSide, levelUUID);
            if (animation == null)
                throw new MissingAnimationException(levelUUID);
            
            level = animation.fakeWorld;
        }
        return level;
    }
    
    public Set<BlockPos> getPositions() {
        return previews.getPositions(position.getPos());
    }
    
    public PlacementPreview copy() {
        return new PlacementPreview(levelUUID, previews.copy(), mode, position.copy());
    }
    
    public void mirror(Axis axis, LittleBoxAbsolute box) {
        position.mirror(axis, box);
        previews.mirror(axis, box.getDoubledCenter(position.getPos()));
    }
}
