package com.creativemd.littletiles.common.utils.selection.selector;

import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.nbt.NBTTagCompound;

public class AnySelector extends TileSelector {

	@Override
	protected void saveNBT(NBTTagCompound nbt) {

	}

	@Override
	protected void loadNBT(NBTTagCompound nbt) {

	}

	@Override
	public boolean is(LittleTile tile) {
		return true;
	}

}
