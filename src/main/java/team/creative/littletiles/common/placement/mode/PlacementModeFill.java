package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementModeFill extends PlacementMode {
    
    public PlacementModeFill(String name, PreviewMode mode) {
        super(name, mode, false);
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
        if (!context.collisionTest) {
            context.placeTile(tile);
            return true;
        }
        
        List<LittleBox> cutout = new ArrayList<>();
        List<LittleBox> boxes = new ArrayList();
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        for (LittleBox tileBox : tile)
            boxes.addAll(context.getBE().cutOut(tileBox, cutout, volume));
        if (!cutout.isEmpty())
            for (LittleBox box : boxes)
                context.addUnplaceable(tile, box);
            
        if (volume.has())
            context.placement.addRemovedIngredient(context.block, tile, volume);
        
        if (boxes.isEmpty())
            return false;
        
        context.placeTile(tile.copy(boxes));
        return true;
    }
}
