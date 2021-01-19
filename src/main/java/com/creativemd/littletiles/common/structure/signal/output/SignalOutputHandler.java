package com.creativemd.littletiles.common.structure.signal.output;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SignalOutputHandler {
    
    public final ISignalComponent component;
    public final int delay;
    public boolean[] lastReacted;
    
    public SignalOutputHandler(ISignalComponent component, int delay, NBTTagCompound nbt) {
        this.component = component;
        this.delay = delay;
    }
    
    public abstract SignalMode getMode();
    
    public void schedule(boolean[] state) {
        if (lastReacted != null && BooleanUtils.equals(state, lastReacted))
            return;
        queue(state);
        if (lastReacted == null)
            lastReacted = new boolean[state.length];
        BooleanUtils.set(lastReacted, state);
    }
    
    public abstract void queue(boolean[] state);
    
    public void performStateChange(boolean[] state) {
        component.updateState(state);
    }
    
    public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException {
        return component.getBandwidth();
    }
    
    public abstract void write(boolean preview, NBTTagCompound nbt);
    
    public static SignalOutputHandler create(ISignalComponent component, SignalMode mode, int delay, NBTTagCompound nbt, LittleStructure structure) {
        return mode.create(component, delay, nbt, structure != null && structure.hasWorld() && !structure.getWorld().isRemote);
    }
    
    @Override
    public String toString() {
        return component.toString();
    }
    
}
