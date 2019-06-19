package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.entity.EntityAnimation;

import net.minecraft.nbt.NBTTagCompound;

public interface IStructureChildConnector<T> extends IStructureConnector<T> {
	
	public boolean isChild();
	
	public int getChildID();
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt);
	
	public void destroyStructure();
	
	public EntityAnimation getAnimation();
	
}
