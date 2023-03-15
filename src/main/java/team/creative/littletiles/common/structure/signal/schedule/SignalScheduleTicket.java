package team.creative.littletiles.common.structure.signal.schedule;

import java.lang.ref.WeakReference;

import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.signal.LittleSignalHandler;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class SignalScheduleTicket implements Runnable {
    
    private final WeakReference<SignalOutputHandler> outputCondition;
    private SignalState result;
    private int delay;
    
    public SignalScheduleTicket(SignalOutputHandler outputCondition, SignalState result, int delay) {
        this.outputCondition = new WeakReference<SignalOutputHandler>(outputCondition);
        this.result = result;
        this.delay = delay;
    }
    
    @Override
    public void run() {
        SignalOutputHandler handler = outputCondition.get();
        if (handler != null && handler.isStillAvailable())
            try {
                handler.performStateChange(result);
            } catch (Exception e) {}
        markObsolete();
    }
    
    public int getDelay() {
        return delay;
    }
    
    public boolean is(SignalOutputHandler output) {
        return outputCondition.get() == output;
    }
    
    public boolean inShortQueue() {
        return delay < LittleSignalHandler.QUEUE_LENGTH;
    }
    
    public void enterShortQueue(int index) {
        this.delay = index;
    }
    
    public SignalState getState() {
        return result;
    }
    
    public void overwriteState(SignalState newState) {
        result = result.overwrite(newState);
    }
    
    public void markObsolete() {
        outputCondition.clear();
    }
    
    public Level getLevel() {
        SignalOutputHandler handler = outputCondition.get();
        if (handler != null)
            return handler.component.getStructureLevel();
        return null;
    }
    
}
