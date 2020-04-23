package com.creativemd.littletiles.common.tile.preview;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

public class LittlePreviewsStructureHolder extends LittlePreviews {
	
	public final LittleStructure structure;
	
	public LittlePreviewsStructureHolder(LittleStructure structure) {
		super(LittleGridContext.getMin());
		this.structure = structure;
	}
	
	@Override
	public boolean hasStructure() {
		return true;
	}
	
	@Override
	public LittleStructure createStructure() {
		return structure;
	}
	
	@Override
	public LittlePreviewsStructureHolder copy() {
		return new LittlePreviewsStructureHolder(structure);
	}
}
