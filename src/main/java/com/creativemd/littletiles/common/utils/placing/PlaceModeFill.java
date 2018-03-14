package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.placing.PlacementMode.SelectionMode;

import net.minecraft.util.math.BlockPos;

public class PlaceModeFill extends PlacementMode {
	
	public PlaceModeFill(String name, SelectionMode mode) {
		super(name, mode);
	}
	
	@Override
	public boolean checkAll()
	{
		return false;
	}

	@Override
	public List<BlockPos> getCoordsToCheck(HashMap<BlockPos, PlacePreviews> splittedTiles, BlockPos pos) {
		return null;
	}

	@Override
	public List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles,
			boolean requiresCollisionTest) {
		List<LittleTile> tiles = new ArrayList<>();
		if(!requiresCollisionTest)
		{
			tiles.add(tile);
			return tiles;
		}
		
		
		List<LittleTileBox> cutout = new ArrayList<>();
		List<LittleTileBox> boxes = te.cutOut(tile.box, cutout);
		
		for (LittleTileBox box : boxes) {
			LittleTile newTile = tile.copy();
			newTile.box = box;
			tiles.add(newTile);
		}
		
		for (LittleTileBox box : cutout) {
			LittleTile newTile = tile.copy();
			newTile.box = box;
			unplaceableTiles.add(newTile);
		}
		
		return tiles;			
	}
}
