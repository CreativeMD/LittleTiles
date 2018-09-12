package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

public class PlaceModeNormal extends PlacementMode {
	
	public PlaceModeNormal(String name, PreviewMode mode) {
		super(name, mode);
	}
	
	@Override
	public PlacementMode place() {
		if (GuiScreen.isCtrlKeyDown())
			return PlacementMode.fill;
		return super.place();
	}
	
	@Override
	public List<BlockPos> getCoordsToCheck(HashMap<BlockPos, PlacePreviews> splittedTiles, BlockPos pos) {
		List<BlockPos> coords = new ArrayList<>();
		coords.add(pos);
		return coords;
	}
	
	@Override
	public List<LittleTile> placeTile(TileEntityLittleTiles te, LittleTile tile, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, boolean requiresCollisionTest) {
		List<LittleTile> tiles = new ArrayList<>();
		if (!requiresCollisionTest || te.isSpaceForLittleTile(tile.box))
			tiles.add(tile);
		else
			unplaceableTiles.add(tile);
		return tiles;
	}
}
