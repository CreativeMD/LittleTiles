package com.creativemd.littletiles.common.structure.signal.input;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;

import net.minecraft.nbt.NBTTagCompound;

public class InternalSignalInput extends InternalSignal {
	
	public InternalSignalInput(LittleStructure parent, String name, int bandwidth) {
		super(parent, name, bandwidth);
	}
	
	@Override
	public void changed() {
		parent.changed(this);
	}
	
	@Override
	public SignalComponentType getType() {
		return SignalComponentType.INPUT;
	}
	
	@Override
	public void load(NBTTagCompound nbt) {
		BooleanUtils.intToBool(nbt.getInteger(name), getState());
	}
	
	@Override
	public NBTTagCompound write(boolean preview, NBTTagCompound nbt) {
		nbt.setInteger(name, BooleanUtils.boolToInt(getState()));
		return nbt;
	}
}
