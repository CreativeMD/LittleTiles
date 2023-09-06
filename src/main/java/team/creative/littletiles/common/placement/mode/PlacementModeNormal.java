package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementModeNormal extends PlacementMode {
    
    public PlacementModeNormal(PreviewMode mode, boolean placeInside) {
        super(mode, placeInside);
    }
    
    @Override
    public PlacementMode place() {
        if (Screen.hasControlDown())
            return PlacementMode.FILL;
        return super.place();
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        List<BlockPos> coords = new ArrayList<>();
        coords.add(pos);
        return coords;
    }
    
    @Override
    public boolean placeTile(PlacementContext context, LittleStructure structure, LittleTile tile) throws LittleActionException {
        if (!context.collisionTest) {
            context.placeTile(tile);
            return true;
        }
        
        List<LittleBox> boxes = new ArrayList<>(tile.size());
        boolean isSpace = true;
        for (LittleBox box : tile) {
            if (context.isSpaceFor(box))
                boxes.add(box.copy());
            else {
                context.addUnplaceable(tile, box.copy());
                isSpace = false;
            }
        }
        
        if (isSpace) {
            context.placeTile(tile);
            return true;
        } else if (this instanceof PlacementModeAll)
            throw new LittleActionException("Could not place all tiles");
        else if (!boxes.isEmpty()) {
            context.placeTile(tile.copy(boxes));
            return true;
        }
        
        return false;
    }
}
