package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxesSimple;
import com.creativemd.littletiles.common.tile.parent.ParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class PlacementResult {
    
    public final LittleAbsolutePreviews placedPreviews;
    public final LittleBoxes placedBoxes;
    private BlockPos lastPos = null;
    public final List<TileEntityLittleTiles> tileEntities = new ArrayList<>();
    public LittleStructure parentStructure;
    
    public PlacementResult(BlockPos pos) {
        this.placedPreviews = new LittleAbsolutePreviews(pos, LittleGridContext.getMin());
        this.placedBoxes = new LittleBoxesSimple(pos, LittleGridContext.getMin());
    }
    
    public void addPlacedTile(ParentTileList parent, LittleTile tile) {
        if (lastPos == null || !lastPos.equals(parent.getPos())) {
            lastPos = parent.getPos();
            tileEntities.add(parent.getTe());
        }
        placedPreviews.addTile(parent, tile);
        placedBoxes.addBox(parent, tile);
    }
    
}
