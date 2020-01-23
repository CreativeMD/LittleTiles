package com.creativemd.littletiles.common.tile.place;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

public class PlacePreviews extends ArrayList<PlacePreview> {
	
	public LittleGridContext context;
	
	public PlacePreviews(LittleGridContext context, List<PlacePreview> previews) {
		super(previews);
		this.context = context;
		convertToSmallest();
	}
	
	public PlacePreviews(LittleGridContext context) {
		this.context = context;
	}
	
	public void ensureBothAreEqual(TileEntityLittleTiles te) {
		if (te.getContext() != context) {
			if (te.getContext().size > context.size)
				convertTo(te.getContext());
			else
				te.convertTo(context);
		}
	}
	
	public void convertTo(LittleGridContext to) {
		for (PlacePreview preview : this) {
			preview.convertTo(this.context, to);
		}
		this.context = to;
	}
	
	public void convertToSmallest() {
		int size = LittleGridContext.minSize;
		for (PlacePreview preview : this) {
			size = Math.max(size, preview.getSmallestContext(context));
		}
		
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public PlacePreviews copy() {
		PlacePreviews previews = new PlacePreviews(context);
		previews.addAll(this);
		return previews;
	}
}
