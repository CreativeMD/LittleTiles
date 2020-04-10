package com.creativemd.littletiles.common.tile.preview;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;

public class LittlePreviewsStructure extends LittlePreviews {
	
	public NBTTagCompound nbt;
	private LittleStructure structure;
	
	public LittlePreviewsStructure(NBTTagCompound nbt, LittlePreviews previews) {
		super(previews);
		this.nbt = nbt;
	}
	
	public LittlePreviewsStructure(NBTTagCompound nbt, LittleGridContext context) {
		super(context);
		this.nbt = nbt;
	}
	
	@Override
	public boolean hasStructure() {
		return nbt.getSize() > 0;
	}
	
	@Override
	public void deleteCachedStructure() {
		super.deleteCachedStructure();
		structure = null;
	}
	
	@Override
	public void addChild(LittlePreviews child) {
		super.addChild(child);
		if (structure != null && child.hasStructure())
			structure.addTempChild(child.getStructure());
	}
	
	@Override
	public LittleStructure getStructure() {
		if (structure == null) {
			structure = LittleStructure.createAndLoadStructure(nbt, null);
			structure.createTempChildList();
			for (LittlePreviews child : getChildren()) {
				if (child.hasStructure())
					structure.addTempChild(child.getStructure());
			}
		}
		return structure;
	}
	
	@Override
	public NBTTagCompound getStructureData() {
		return nbt;
	}
	
	@Override
	public LittlePreviewsStructure copy() {
		LittlePreviewsStructure previews = new LittlePreviewsStructure(nbt, context);
		for (LittlePreview preview : this.previews)
			previews.previews.add(preview.copy());
		
		for (LittlePreviews child : this.children)
			previews.children.add(child.copy());
		return previews;
	}
	
}
