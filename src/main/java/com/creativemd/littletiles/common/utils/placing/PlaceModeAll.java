package com.creativemd.littletiles.common.utils.placing;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

public class PlaceModeAll extends PlaceModeNormal {
	
	public PlaceModeAll(String name, SelectionMode mode) {
		super(name, mode);
	}

	@Override
	public boolean canPlaceStructures() {
		return true;
	}
	
	@Override
	public PlacementMode place() {
		if(GuiScreen.isCtrlKeyDown())
			return PlacementMode.overwrite;
		return super.place();
	}

	@Override
	public List<BlockPos> getCoordsToCheck(HashMapList<BlockPos, PlacePreviewTile> splittedTiles, BlockPos pos) {
		return new ArrayList<>(splittedTiles.keySet());
	}
}
