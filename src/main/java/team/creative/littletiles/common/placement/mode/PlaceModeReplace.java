package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
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
    public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleStructure structure, LittleTile tile, boolean requiresCollisionTest) {
        if (!requiresCollisionTest)
            return new ArrayList<>();
        List<LittleTile> tiles = new ArrayList<>();
        for (LittleTile lt : LittleActionDestroyBoxes.removeBox(block.getTe(), block.getContext(), tile.getBox(), false)) {
            LittleTile newTile = tile.copy();
            newTile.setBox(lt.getBox());
            tiles.add(newTile);
            placement.removedTiles.addTile(parent, lt);
        }
        return tiles;
    }
    
}
