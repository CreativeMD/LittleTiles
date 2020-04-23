package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

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
	public List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, boolean requiresCollisionTest) {
		if (!requiresCollisionTest)
			return new ArrayList<>();
		List<LittleTile> tiles = new ArrayList<>();
		for (LittleTile lt : LittleActionDestroyBoxes.removeBox(te, te.getContext(), tile.box, false)) {
			LittleTile newTile = tile.copy();
			newTile.te = lt.te;
			newTile.box = lt.box;
			tiles.add(newTile);
			removedTiles.add(lt);
		}
		return tiles;
	}
	
}
