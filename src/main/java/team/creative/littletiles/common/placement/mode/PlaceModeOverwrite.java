package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlaceModeOverwrite extends PlacementMode {
    
    public PlaceModeOverwrite(String name, PreviewMode mode) {
        super(name, mode, false);
    }
    
    @Override
    public boolean shouldConvertBlock() {
        return true;
    }
    
    @Override
    public boolean canPlaceStructures() {
        return true;
    }
    
    @Override
    public boolean checkAll() {
        return false;
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        return new ArrayList<>(splittedTiles);
    }
    
    @Override
    public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleStructure structure, LittleTile tile, boolean requiresCollisionTest) {
        List<LittleTile> tiles = new ArrayList<>();
        LittleGridContext context = block.getContext();
        if (requiresCollisionTest)
            for (LittleTile removedTile : LittleActionDestroyBoxes.removeBox(block.getTe(), context, tile.getBox(), false))
                placement.removedTiles.addTile(parent, removedTile);
        block.getTe().convertTo(context);
        tiles.add(tile);
        return tiles;
    }
    
}
