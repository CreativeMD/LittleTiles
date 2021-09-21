package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.util.math.BlockPos;

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
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        if (requiresCollisionTest)
            for (LittleTile removedTile : LittleActionDestroyBoxes.removeBox(block.getTe(), context, tile.getBox(), false, volume)) {
                placement.removedTiles.addTile(block.getTe().noneStructureTiles(), removedTile);
                if (volume.has())
                    placement.addRemovedIngredient(block, tile, volume);
                volume.clear();
            }
        block.getTe().convertTo(context);
        tiles.add(tile);
        return tiles;
    }
    
}
