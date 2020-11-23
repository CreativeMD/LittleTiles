package com.creativemd.littletiles.common.structure.signal.schedule;

import net.minecraft.world.World;

public interface ISignalSchedulable {
	
	public void notifyChange();
	
	public boolean hasChanged();
	
	public void markChanged();
	
	public void markUnchanged();
	
	public World getWorld();
	
	public default void updateSignaling() {
		markUnchanged();
		notifyChange();
	}
	
	public default void schedule() {
		if (!hasChanged()) {
			SignalTicker.schedule(getWorld(), this);
			markChanged();
		}
	}
	
}
