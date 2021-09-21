package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.util.math.BlockPos;

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
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        for (LittleTile lt : LittleActionDestroyBoxes.removeBox(block.getTe(), block.getContext(), tile.getBox(), false, volume)) {
            LittleTile newTile = tile.copy();
            newTile.setBox(lt.getBox());
            tiles.add(newTile);
            placement.removedTiles.addTile(parent, lt);
            if (volume.has())
                placement.addRemovedIngredient(block, tile, volume);
            volume.clear();
        }
        return tiles;
    }
    
}
