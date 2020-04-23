package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class PlacementResult {
	
	public final LittleAbsolutePreviews placedPreviews;
	public final LittleBoxes placedBoxes;
	private BlockPos lastPos = null;
	public final List<TileEntityLittleTiles> tileEntities = new ArrayList<>();
	public final LittleStructure parentStructure;
	
	public PlacementResult(BlockPos pos, LittleStructure parentStructure) {
		this.placedPreviews = new LittleAbsolutePreviews(pos, LittleGridContext.getMin());
		this.placedBoxes = new LittleBoxes(pos, LittleGridContext.getMin());
		this.parentStructure = parentStructure;
	}
	
	public void addPlacedTile(LittleTile tile) {
		if (lastPos == null || !lastPos.equals(tile.te.getPos())) {
			lastPos = tile.te.getPos();
			tileEntities.add(tile.te);
		}
		placedPreviews.addTile(tile);
		placedBoxes.addBox(tile);
	}
	
}
