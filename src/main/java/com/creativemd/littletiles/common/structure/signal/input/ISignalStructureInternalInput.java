package com.creativemd.littletiles.common.structure.signal.input;

import net.minecraft.nbt.NBTTagCompound;

public interface ISignalStructureInternalInput {
	
	public void createInputs(NBTTagCompound nbt);
	
	public InternalSignalInput getInput(int id);
	
	public void saveInputs(NBTTagCompound nbt);
	
}
