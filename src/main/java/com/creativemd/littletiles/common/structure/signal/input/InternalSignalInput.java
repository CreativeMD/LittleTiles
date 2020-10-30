package com.creativemd.littletiles.common.structure.signal.input;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.logic.ISignalStructureEvent;

import net.minecraft.nbt.NBTTagCompound;

public class InternalSignalInput extends InternalSignal {
	
	public InternalSignalInput(LittleStructure parent, String name, int bandwidth, NBTTagCompound nbt) {
		super(parent, name, bandwidth, nbt);
	}
	
	@Override
	public void changed() {
		if (parent instanceof ISignalStructureEvent)
			((ISignalStructureEvent) parent).changed(this);
	}
	
}
