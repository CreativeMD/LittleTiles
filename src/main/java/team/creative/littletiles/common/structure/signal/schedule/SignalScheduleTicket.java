package team.creative.littletiles.common.structure.signal.schedule;

import java.lang.ref.WeakReference;

import net.minecraft.world.level.Level;
import team.creative.littletiles.common.level.tick.LittleTickTicket;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class SignalScheduleTicket implements Runnable {
    
    private final WeakReference<SignalOutputHandler> outputCondition;
    private SignalState result;
    public final int delay;
    private LittleTickTicket ticker;
    
    public SignalScheduleTicket(SignalOutputHandler outputCondition, SignalState result, int delay) {
        this.outputCondition = new WeakReference<SignalOutputHandler>(outputCondition);
        this.result = result;
        this.delay = delay;
    }
    
    public void scheduled(LittleTickTicket ticker) {
        this.ticker = ticker;
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
    
    public int timeTillExecution() {
        if (ticker == null)
            return 0;
        return ticker.timeTillExecution();
    }
    
    public boolean is(SignalOutputHandler output) {
        return outputCondition.get() == output;
    }
    
    public SignalState getState() {
        return result;
    }
    
    public void overwriteState(SignalState newState) {
        result = result.overwrite(newState);
    }
    
    public boolean isObselete() {
        return ticker == null || outputCondition.refersTo(null);
    }
    
    public void markObsolete() {
        outputCondition.clear();
        ticker = null;
    }
    
    public Level getLevel() {
        SignalOutputHandler handler = outputCondition.get();
        if (handler != null)
            return handler.component.getStructureLevel();
        return null;
    }
    
    public int[] toArray() {
        return new int[] { timeTillExecution(), getState().number() };
    }
    
}
