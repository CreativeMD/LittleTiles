package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.util.math.BlockPos;

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
	public List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, boolean requiresCollisionTest) {
		List<LittleTile> tiles = new ArrayList<>();
		if (!requiresCollisionTest) {
			tiles.add(tile);
			return tiles;
		}
		
		List<LittleBox> cutout = new ArrayList<>();
		List<LittleBox> boxes = te.cutOut(tile.box, cutout);
		
		for (LittleBox box : boxes) {
			LittleTile newTile = tile.copy();
			newTile.box = box;
			tiles.add(newTile);
		}
		
		for (LittleBox box : cutout) {
			LittleTile newTile = tile.copy();
			newTile.box = box;
			unplaceableTiles.add(newTile);
		}
		
		return tiles;
	}
}
