package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.nbt.NBTTagCompound;

public abstract class InternalSignal implements ISignalComponent {
	
	public final LittleStructure parent;
	public final String name;
	private final boolean[] state;
	
	public InternalSignal(LittleStructure parent, String name, int bandwidth) {
		this.parent = parent;
		this.name = name;
		this.state = new boolean[bandwidth];
	}
	
	public void load(NBTTagCompound nbt) {
		BooleanUtils.intToBool(nbt.getInteger(name), state);
	}
	
	@Override
	public boolean[] getState() {
		return state;
	}
	
	public void write(NBTTagCompound nbt) {
		nbt.setInteger("state", BooleanUtils.boolToInt(state));
	}
	
	@Override
	public int getBandwidth() {
		return state.length;
	}
	
}
