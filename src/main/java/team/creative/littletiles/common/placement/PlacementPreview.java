package team.creative.littletiles.common.placement;

import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.CreativeLevel;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.level.WorldAnimationHandler;
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
}
