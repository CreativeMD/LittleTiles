package team.creative.littletiles.common.placement.mode;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementModeColorize extends PlacementMode {
    
    public PlacementModeColorize(PreviewMode mode) {
        super(mode, true);
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
        
        boolean changed = false;
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        for (LittleBox box : tile) {
            for (LittleTile lt : LittleActionDestroyBoxes.removeBox(context.getBE(), context.block.getGrid(), box, false, volume)) {
                context.addRemoved(lt);
                lt.color = tile.color;
                context.placeTile(lt);
                changed = true;
            }
            if (volume.has())
                context.placement.addRemovedIngredient(context.block, tile, volume);
            volume.clear();
        }
        return changed;
    }
}
