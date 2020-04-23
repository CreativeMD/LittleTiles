package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

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
	public List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, boolean requiresCollisionTest) {
		List<LittleTile> tiles = new ArrayList<>();
		LittleGridContext context = te.getContext();
		if (requiresCollisionTest)
			removedTiles.addAll(LittleActionDestroyBoxes.removeBox(te, context, tile.box, false));
		te.convertTo(context);
		tiles.add(tile);
		return tiles;
	}
	
}
