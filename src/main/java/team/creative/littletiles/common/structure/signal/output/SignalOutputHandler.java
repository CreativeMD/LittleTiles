package team.creative.littletiles.common.structure.signal.output;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;

public abstract class SignalOutputHandler {
    
    public final ISignalComponent component;
    public final int delay;
    public boolean[] lastReacted;
    
    public SignalOutputHandler(ISignalComponent component, int delay, CompoundTag nbt) {
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
    
    public abstract void write(boolean preview, CompoundTag nbt);
    
    public static SignalOutputHandler create(ISignalComponent component, SignalMode mode, int delay, CompoundTag nbt, LittleStructure structure) {
        return mode.create(component, delay, nbt, structure != null && structure.hasWorld() && !structure.getLevel().isClientSide);
    }
    
    public boolean isStillAvailable() {
        return component.getStructure().isStillAvailable();
    }
    
    @Override
    public String toString() {
        return component.toString();
    }
    
}
