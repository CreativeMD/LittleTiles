package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlaceModeFill extends PlacementMode {
    
    public PlaceModeFill(String name, PreviewMode mode) {
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
    public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleStructure structure, LittleTile tile, boolean requiresCollisionTest) {
        List<LittleTile> tiles = new ArrayList<>();
        if (!requiresCollisionTest) {
            tiles.add(tile);
            return tiles;
        }
        
        List<LittleBox> cutout = new ArrayList<>();
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        List<LittleBox> boxes = block.getTe().cutOut(tile.getBox(), cutout, volume);
        
        for (LittleBox box : boxes) {
            LittleTile newTile = tile.copy();
            newTile.setBox(box);
            tiles.add(newTile);
        }
        
        for (LittleBox box : cutout) {
            LittleTile newTile = tile.copy();
            newTile.setBox(box);
            placement.unplaceableTiles.addTile(parent, newTile);
        }
        
        if (volume.has())
            placement.unplaceableTiles.addTile(parent, volume.createFakeTile(tile));
        
        return tiles;
    }
}
