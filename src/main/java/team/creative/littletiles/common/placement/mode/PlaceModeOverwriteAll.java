package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;

public class PlaceModeOverwriteAll extends PlaceModeAll {
    
    public PlaceModeOverwriteAll(String name, PreviewMode mode) {
        super(name, mode);
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
    public void prepareBlock(Placement placement, PlacementBlock block, boolean requiresCollisionTest) {
        block.getBE().updateTilesSecretly((x) -> {
            ParentCollection parent = x.noneStructureTiles();
            for (LittleTile toRemove : parent)
                placement.removedTiles.addTile(parent, toRemove);
            parent.clear();
            
        });
    }
    
}
