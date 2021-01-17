package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.ParentTileList;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.util.math.BlockPos;

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
        block.getTe().updateTilesSecretly((x) -> {
            ParentTileList parent = x.noneStructureTiles();
            for (LittleTile toRemove : parent)
                placement.removedTiles.addTile(parent, toRemove);
            parent.clear();
            
        });
    }
    
}
