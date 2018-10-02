package com.creativemd.littletiles.common.structure.connection;

import net.minecraft.nbt.NBTTagCompound;

public interface IStructureChildConnector<T> extends IStructureConnector<T> {
	
	public boolean isChild();
	
	public int getChildID();
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt);
	
}
