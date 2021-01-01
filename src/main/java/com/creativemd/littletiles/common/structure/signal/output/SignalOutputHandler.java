package com.creativemd.littletiles.common.structure.signal.output;

import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SignalOutputHandler {
	
	public final ISignalComponent component;
	public final int delay;
	
	public SignalOutputHandler(ISignalComponent component, int delay, NBTTagCompound nbt) {
		this.component = component;
		this.delay = delay;
	}
	
	public abstract SignalMode getMode();
	
	public abstract void schedule(boolean[] state);
	
	public void performStateChange(boolean[] state) {
		component.updateState(state);
	}
	
	public int getBandwidth() {
		return component.getBandwidth();
	}
	
	public abstract void write(NBTTagCompound nbt);
	
	public static SignalOutputHandler create(ISignalComponent component, SignalMode mode, int delay, NBTTagCompound nbt) {
		return mode.create(component, delay, nbt);
	}
	
}
