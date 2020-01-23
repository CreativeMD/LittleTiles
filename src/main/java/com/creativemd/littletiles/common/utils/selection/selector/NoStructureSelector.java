package com.creativemd.littletiles.common.utils.selection.selector;

import com.creativemd.littletiles.common.tile.LittleTile;

import net.minecraft.nbt.NBTTagCompound;

public class NoStructureSelector extends TileSelector {
	
	public NoStructureSelector() {
		
	}
	
	@Override
	protected void saveNBT(NBTTagCompound nbt) {
		
	}
	
	@Override
	protected void loadNBT(NBTTagCompound nbt) {
		
	}
	
	@Override
	public boolean is(LittleTile tile) {
		return !tile.isChildOfStructure();
	}
	
}
