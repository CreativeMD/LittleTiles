package com.creativemd.littletiles.common.structure.signal.schedule;

import java.lang.ref.WeakReference;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputHandler;

import net.minecraft.world.World;

public class SignalScheduleTicket implements ISignalScheduleTicket {
    
    private int delay;
    private final WeakReference<SignalOutputHandler> outputCondition;
    private final boolean[] result;
    
    public SignalScheduleTicket(SignalOutputHandler outputCondition, boolean[] result, int delay) {
        this.outputCondition = new WeakReference<SignalOutputHandler>(outputCondition);
        this.result = result;
        this.delay = delay;
    }
    
    public int tick() {
        delay--;
        return delay;
    }
    
    public void run() {
        SignalOutputHandler handler = outputCondition.get();
        if (handler != null && handler.isStillAvailable())
            try {
                handler.performStateChange(result);
            } catch (Exception e) {}
        markObsolete();
    }
    
    @Override
    public int getDelay() {
        if (inShortQueue()) {
            SignalOutputHandler handler = outputCondition.get();
            if (handler != null)
                return SignalTicker.get(handler.component).getDelayOfQueue(delay);
        }
        return delay;
    }
    
    public int getExactDelayValue() {
        return delay;
    }
    
    public boolean is(SignalOutputHandler output) {
        return outputCondition.get() == output;
    }
    
    public boolean inShortQueue() {
        return delay < SignalTicker.queueLength;
    }
    
    public void enterShortQueue(int index) {
        this.delay = index;
    }
    
    @Override
    public boolean[] getState() {
        return result;
    }
    
    @Override
    public void overwriteState(boolean[] newState) {
        BooleanUtils.set(result, newState);
    }
    
    @Override
    public void markObsolete() {
        outputCondition.clear();
    }
    
    public World getWorld() {
        SignalOutputHandler handler = outputCondition.get();
        if (handler != null)
            return handler.component.getStructureWorld();
        return null;
    }
    
}
