package com.creativemd.littletiles.common.tiles.preview;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry.LittleStructurePreviewHandler;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;

public class LittlePreviewsStructure extends LittlePreviews {
	
	public NBTTagCompound nbt;
	private LittleStructure structure;
	private LittleStructurePreviewHandler handler;
	
	protected List<LittlePreviewsStructure> children = new ArrayList<>();
	
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
		return true;
	}
	
	@Override
	public LittleStructure getStructure() {
		if (structure == null) {
			structure = LittleStructure.createAndLoadStructure(nbt, null);
			structure.tempChildren = new ArrayList<>();
			for (LittlePreviewsStructure child : getChildren()) {
				structure.tempChildren.add(child.getStructure());
			}
		}
		return structure;
	}
	
	@Override
	public NBTTagCompound getStructureData() {
		return nbt;
	}
	
	@Override
	public LittleStructurePreviewHandler getStructureHandler() {
		if (handler == null)
			handler = LittleStructureRegistry.getStructureEntry(nbt.getString("id")).handler;
		return handler;
	}
	
	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	@Override
	public List<LittlePreviewsStructure> getChildren() {
		return children;
	}
	
	@Override
	public void addChild(LittlePreviews child) {
		children.add((LittlePreviewsStructure) child);
	}
}
