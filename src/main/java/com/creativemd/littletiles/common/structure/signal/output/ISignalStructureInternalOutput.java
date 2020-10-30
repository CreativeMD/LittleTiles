package com.creativemd.littletiles.common.structure.signal.output;

import net.minecraft.nbt.NBTTagCompound;

public interface ISignalStructureInternalOutput {
	
	public void createOutputs(NBTTagCompound nbt);
	
	public InternalSignalOutput getInternalOutput(int id);
	
	public void saveOutputs(NBTTagCompound nbt);
	
}
