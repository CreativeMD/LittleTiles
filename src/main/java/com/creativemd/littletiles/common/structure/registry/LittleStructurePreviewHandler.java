package com.creativemd.littletiles.common.structure.registry;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

public class LittleStructurePreviewHandler {
	
	public LittleGridContext getMinContext(LittlePreviews previews) {
		return LittleGridContext.getMin();
	}
	
	public List<PlacePreviewTile> getSpecialTiles(LittlePreviews previews) {
		return new ArrayList<>();
	}
	
}