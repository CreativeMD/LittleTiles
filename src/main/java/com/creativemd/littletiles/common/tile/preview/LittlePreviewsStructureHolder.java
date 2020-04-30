package com.creativemd.littletiles.common.tile.preview;

import java.util.Collections;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;

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
	public String getStructureId() {
		return structure.type.id;
	}
	
	@Override
	public String getStructureName() {
		return structure.name;
	}
	
	@Override
	protected LittleGridContext getSmallestContext() {
		return LittleGridContext.getMin();
	}
	
	@Override
	public LittleStructureType getStructureType() {
		return structure.type;
	}
	
	@Override
	public List<PlacePreview> getPlacePreviews(LittleVec offset) {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public List<PlacePreview> getPlacePreviewsIncludingChildren(LittleVec offset) {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean containsIngredients() {
		return true;
	}
	
	@Override
	public void movePreviews(LittleGridContext context, LittleVec offset) {
		
	}
	
	@Override
	public void flipPreviews(Axis axis, LittleVec doubledCenter) {
		
	}
	
	@Override
	public void rotatePreviews(Rotation rotation, LittleVec doubledCenter) {
		
	}
	
	@Override
	public LittlePreviewsStructureHolder copy() {
		return new LittlePreviewsStructureHolder(structure);
	}
}
