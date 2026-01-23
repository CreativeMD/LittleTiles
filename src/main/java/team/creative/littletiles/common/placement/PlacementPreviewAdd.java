package team.creative.littletiles.common.placement;

import java.util.UUID;

import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class PlacementPreviewAdd extends PlacementPreview {
    
    public final StructureLocation parent;
    
    public PlacementPreviewAdd(StructureLocation location, Level level, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        this(location, level instanceof ISubLevel sub ? sub.getHolder().getUUID() : null, previews, mode, position);
    }
    
    public PlacementPreviewAdd(StructureLocation location, UUID levelUUID, LittleGroup previews, PlacementMode mode, PlacementPosition position) {
        super(levelUUID, previews, mode, position);
        this.parent = location;
    }
    
    public PlacementPreviewAdd(StructureLocation location, Level level, LittleGroupAbsolute previews, PlacementMode mode) {
        super(level instanceof ISubLevel sub ? sub.getHolder().getUUID() : null, previews, mode, null);
        this.parent = location;
    }
    
    @Override
    protected PlacementMode check(PlacementMode mode) {
        return mode;
    }
    
    @Override
    public void validate() throws LittleActionException {
        if (previews.hasChildren())
            throw new PlaceAddChildrenDetected();
    }
    
    public static class PlaceAddChildrenDetected extends LittleActionException {
        
        public PlaceAddChildrenDetected() {
            super("action.place_add.invalid.children");
        }
        
    }
    
}
