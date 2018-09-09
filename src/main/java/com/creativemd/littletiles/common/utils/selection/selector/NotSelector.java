package com.creativemd.littletiles.common.utils.selection.selector;

import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.nbt.NBTTagCompound;

public class NotSelector extends TileSelector {
	
	public TileSelector selector;
	
	public NotSelector(TileSelector selector) {
		this.selector = selector;
	}

	public NotSelector() {
		
	}
	
	@Override
	protected void saveNBT(NBTTagCompound nbt) {
		selector.saveNBT(nbt);
		nbt.setString("type2", getTypeID(selector.getClass()));
	}

	@Override
	protected void loadNBT(NBTTagCompound nbt) {
		selector = TileSelector.loadSelector(nbt.getString("type2"), nbt);
	}

	@Override
	public boolean is(LittleTile tile) {
		return !selector.is(tile);
	}

}
