package com.creativemd.littletiles.common.structure.signal.output;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;

import net.minecraft.nbt.NBTTagCompound;

public abstract class InternalSignalOutput extends InternalSignal {
	
	public InternalSignalOutput(LittleStructure parent, String name, int bandwidth, NBTTagCompound nbt) {
		super(parent, name, bandwidth, nbt);
	}
	
}
