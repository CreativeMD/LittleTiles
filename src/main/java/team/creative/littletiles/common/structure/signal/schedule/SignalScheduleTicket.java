package team.creative.littletiles.common.structure.signal.schedule;

import java.lang.ref.WeakReference;

import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class SignalScheduleTicket implements ISignalScheduleTicket {
    
    private int delay;
    private final WeakReference<SignalOutputHandler> outputCondition;
    private SignalState result;
    
    public SignalScheduleTicket(SignalOutputHandler outputCondition, SignalState result, int delay) {
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
    public SignalState getState() {
        return result;
    }
    
    @Override
    public void overwriteState(SignalState newState) {
        result = result.overwrite(newState);
    }
    
    @Override
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
