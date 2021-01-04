package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class InternalSignal implements ISignalComponent {
	
	public final LittleStructure parent;
	public final String name;
	private final boolean[] state;
	
	public InternalSignal(LittleStructure parent, String name, int bandwidth) {
		this.parent = parent;
		this.name = name;
		this.state = new boolean[bandwidth];
	}
	
	public abstract void load(NBTTagCompound nbt);
	
	@Override
	public boolean[] getState() {
		return state;
	}
	
	public abstract NBTTagCompound write(NBTTagCompound nbt);
	
	@Override
	public int getBandwidth() {
		return state.length;
	}
	
	@Override
	public LittleStructure getStructure() {
		return parent;
	}
	
	@Override
	public World getWorld() {
		if (parent.mainBlock != null)
			return parent.getWorld();
		return null;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
