package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.nbt.NBTTagCompound;

public abstract class InternalSignal implements ISignalComponent {
	
	public final LittleStructure parent;
	public final String name;
	private final boolean[] state;
	
	public InternalSignal(LittleStructure parent, String name, int bandwidth, NBTTagCompound nbt) {
		this.parent = parent;
		this.name = name;
		this.state = new boolean[bandwidth];
		if (nbt != null)
			BooleanUtils.intToBool(nbt.getInteger(name), state);
	}
	
	@Override
	public boolean[] getState() {
		return state;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("state", BooleanUtils.boolToInt(state));
	}
	
	@Override
	public int getBandwidth() {
		return state.length;
	}
	
}
