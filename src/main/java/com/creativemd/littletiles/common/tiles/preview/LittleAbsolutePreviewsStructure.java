package com.creativemd.littletiles.common.tiles.preview;

import java.util.ArrayList;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructurePreviewHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class LittleAbsolutePreviewsStructure extends LittleAbsolutePreviews {
	
	public final NBTTagCompound nbt;
	private LittleStructure structure;
	private LittleStructurePreviewHandler handler;
	
	public LittleAbsolutePreviewsStructure(NBTTagCompound nbt, LittleAbsolutePreviews previews) {
		super(previews);
		this.nbt = nbt;
	}
	
	public LittleAbsolutePreviewsStructure(NBTTagCompound nbt, BlockPos pos, LittlePreviews previews) {
		super(pos, previews.context);
		this.previews.addAll(previews.previews);
		this.children.addAll(previews.children);
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
			handler = LittleStructureRegistry.getStructureType(nbt.getString("id")).handler;
		return handler;
	}
	
	@Override
	public LittleAbsolutePreviewsStructure copy() {
		LittleAbsolutePreviewsStructure previews = new LittleAbsolutePreviewsStructure(nbt, pos, context);
		for (LittleTilePreview preview : this.previews) {
			previews.previews.add(preview.copy());
		}
		for (LittlePreviewsStructure child : this.children) {
			previews.children.add(child.copy());
		}
		return previews;
	}
}
