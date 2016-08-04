package com.creativemd.littletiles.common.structure;

import com.creativemd.creativecore.gui.container.SubGui;

import net.minecraft.nbt.NBTTagCompound;

public class LittleLadder extends LittleStructure {

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		
	}

	@Override
	public void createControls(SubGui gui, LittleStructure structure) {
		
	}

	@Override
	public LittleStructure parseStructure(SubGui gui) {
		return new LittleLadder();
	}
	
	@Override
	public boolean isLadder()
	{
		return true;
	}
	
}
