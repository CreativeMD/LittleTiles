package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlaceModeReplace extends PlacementMode {
    
    public PlaceModeReplace(String name, PreviewMode mode) {
        super(name, mode, true);
    }
    
    @Override
    public boolean shouldConvertBlock() {
        return true;
    }
    
    @Override
    public boolean checkAll() {
        return false;
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        return null;
    }
    
    @Override
    public boolean placeTile(PlacementContext context, LittleStructure structure, LittleTile tile) throws LittleActionException {
        if (!context.collisionTest)
            return false;
        
        List<LittleBox> boxes = new ArrayList<>();
        for (LittleBox box : tile)
            for (LittleTile lt : LittleActionDestroyBoxes.removeBox(context.getBE(), context.block.getGrid(), box, false)) {
                for (LittleBox newBox : lt) {
                    boxes.add(newBox);
                    
                }
                context.addRemoved(lt);
            }
        
        if (boxes.isEmpty())
            return false;
        context.placeTile(tile.copy(boxes));
        return true;
    }
    
}
