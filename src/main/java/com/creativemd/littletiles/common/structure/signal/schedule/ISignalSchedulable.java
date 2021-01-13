package com.creativemd.littletiles.common.structure.signal.schedule;

import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;

import net.minecraft.world.World;

public interface ISignalSchedulable {
    
    public void notifyChange();
    
    public boolean hasChanged();
    
    public void markChanged();
    
    public void markUnchanged();
    
    public World getWorld();
    
    public default void updateSignaling() throws CorruptedConnectionException, NotYetConnectedException {
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
