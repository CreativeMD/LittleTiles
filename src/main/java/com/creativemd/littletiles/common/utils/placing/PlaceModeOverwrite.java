package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class PlaceModeOverwrite extends PlacementMode {

	public PlaceModeOverwrite(String name, PreviewMode mode) {
		super(name, mode);
	}
	
	@Override
	public boolean shouldConvertBlock()
	{
		return true;
	}
	
	@Override
	public boolean canPlaceStructures() {
		return true;
	}
	
	@Override
	public boolean checkAll()
	{
		return false;
	}

	@Override
	public List<BlockPos> getCoordsToCheck(HashMap<BlockPos, PlacePreviews> splittedTiles, BlockPos pos) {
		return new ArrayList<>(splittedTiles.keySet());
	}

	@Override
	public List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles,
			boolean requiresCollisionTest) {
		List<LittleTile> tiles = new ArrayList<>();
		LittleGridContext context = te.getContext();
		if(requiresCollisionTest)
			removedTiles.addAll(LittleActionDestroyBoxes.removeBox(te, context, tile.box, false));
		te.convertTo(context);
		tiles.add(tile);
		return tiles;
	}

}
