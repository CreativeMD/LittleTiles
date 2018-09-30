package com.creativemd.littletiles.common.tiles.preview;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry.LittleStructurePreviewHandler;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class LittleAbsolutePreviewsStructure extends LittleAbsolutePreviews {
	
	public final NBTTagCompound nbt;
	private LittleStructure structure;
	private LittleStructurePreviewHandler handler;
	
	protected List<LittleAbsolutePreviewsStructure> children = new ArrayList<>();
	
	public LittleAbsolutePreviewsStructure(NBTTagCompound nbt, LittleAbsolutePreviews previews) {
		super(previews);
		this.nbt = nbt;
	}
	
	public LittleAbsolutePreviewsStructure(NBTTagCompound nbt, BlockPos pos, LittleGridContext context) {
		super(pos, context);
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
			for (LittleAbsolutePreviewsStructure child : getChildren()) {
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
	public List<LittleAbsolutePreviewsStructure> getChildren() {
		return children;
	}
	
	@Override
	public void addChild(LittlePreviews child) {
		children.add((LittleAbsolutePreviewsStructure) child);
	}
}
