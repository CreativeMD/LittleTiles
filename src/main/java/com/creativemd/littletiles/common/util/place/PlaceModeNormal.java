package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.util.place.Placement.PlacementBlock;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

public class PlaceModeNormal extends PlacementMode {
	
	public PlaceModeNormal(String name, PreviewMode mode, boolean placeInside) {
		super(name, mode, placeInside);
	}
	
	@Override
	public PlacementMode place() {
		if (GuiScreen.isCtrlKeyDown())
			return PlacementMode.fill;
		return super.place();
	}
	
	@Override
	public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
		List<BlockPos> coords = new ArrayList<>();
		coords.add(pos);
		return coords;
	}
	
	@Override
	public List<LittleTile> placeTile(Placement placement, PlacementBlock block, IParentTileList parent, LittleTile tile, boolean requiresCollisionTest) {
		List<LittleTile> tiles = new ArrayList<>();
		if (!requiresCollisionTest || block.getTe().isSpaceForLittleTile(tile.getBox()))
			tiles.add(tile);
		else if (this instanceof PlaceModeAll)
			throw new RuntimeException("Something went wrong. There should be space for the tile!");
		else
			placement.unplaceableTiles.addTile(parent, tile);
		return tiles;
	}
}
