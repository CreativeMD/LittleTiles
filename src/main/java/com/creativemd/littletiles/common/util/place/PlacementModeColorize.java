package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.util.math.BlockPos;

public class PlacementModeColorize extends PlacementMode {
    
    public PlacementModeColorize(String name, PreviewMode mode) {
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
            return Collections.EMPTY_LIST;
        List<LittleTile> tiles = new ArrayList<>();
        LittleBoxReturnedVolume volume = new LittleBoxReturnedVolume();
        for (LittleTile lt : LittleActionDestroyBoxes.removeBox(block.getTe(), block.getContext(), tile.getBox(), false, volume)) {
            LittleTile newTile = LittleTileColored.setColor(lt, tile instanceof LittleTileColored ? ((LittleTileColored) tile).color : ColorUtils.WHITE);
            if (newTile != null) {
                placement.removedTiles.addTile(parent, lt);
                tiles.add(newTile);
            }
            if (volume.has())
                placement.addRemovedIngredient(block, tile, volume);
            volume.clear();
        }
        return tiles;
    }
}
