package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.world.World;

public interface ISignalComponent {
	
	public int getBandwidth();
	
	public default void updateState(boolean[] state) {
		if (!BooleanUtils.equals(state, getState())) {
			BooleanUtils.set(getState(), state);
			changed();
		}
	}
	
	public default void updateState(int offset, boolean[] state) {
		boolean[] internal = getState();
		boolean changed = false;
		for (int i = 0; i < state.length; i++) {
			int index = i + offset;
			if (index < internal.length && state[i] != internal[index]) {
				internal[index] = state[i];
				changed = true;
			}
		}
		if (changed)
			changed();
	}
	
	public void changed();
	
	public boolean[] getState();
	
	public SignalComponentType getType();
	
	public LittleStructure getStructure();
	
	public World getWorld();
	
}
