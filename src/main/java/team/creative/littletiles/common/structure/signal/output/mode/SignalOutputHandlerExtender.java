package team.creative.littletiles.common.structure.signal.output.mode;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public class SignalOutputHandlerExtender extends SignalOutputHandler {
    
    public final int pulseLength;
    public boolean stateBefore;
    public SignalScheduleTicket pulseStart;
    public SignalScheduleTicket pulseEnd;
    
    public SignalOutputHandlerExtender(ISignalComponent component, int delay, CompoundTag nbt) {
        super(component, delay, nbt);
        this.pulseLength = nbt.contains("length") ? nbt.getInt("length") : 10;
        this.stateBefore = nbt.getBoolean("before");
    }
    
    @Override
    public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException {
        return super.getBandwidth();
    }
    
    @Override
    public SignalMode getMode() {
        return SignalMode.EXTENDER;
    }
    
    @Override
    public void performStateChange(SignalState state) {
        super.performStateChange(state);
        if (state.any())
            pulseStart = null;
        else {
            pulseStart = null;
            pulseEnd = null;
        }
    }
    
    @Override
    public void queue(SignalState state) {
        try {
            int bandwidth = getBandwidth();
            boolean current = state.any();
            if (!stateBefore && current) { // switch from off to on
                if (pulseEnd != null) {
                    pulseEnd.markObsolete();
                    pulseEnd = null;
                } else if (pulseStart == null) {
                    
                    SignalState startState = SignalState.create(bandwidth).fill(true);
                    pulseStart = LittleTiles.TICKERS.schedule(this, startState, delay);
                }
            } else if (stateBefore && !current) { // switch from on to off
                if (pulseEnd != null) {
                    pulseEnd.markObsolete();
                    pulseEnd = null;
                }
                
                pulseEnd = LittleTiles.TICKERS.schedule(this, SignalState.create(bandwidth), delay + pulseLength);
            }
            stateBefore = current;
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @Override
    public void write(boolean preview, CompoundTag nbt) {
        nbt.putInt("length", pulseLength);
        nbt.putBoolean("before", stateBefore);
        if (preview)
            return;
        if (pulseStart != null)
            nbt.putInt("start", pulseStart.getDelay());
        if (pulseEnd != null)
            nbt.putInt("end", pulseEnd.getDelay());
    }
    
}