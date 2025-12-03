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

public class SignalOutputHandlerPulse extends SignalOutputHandler {
    
    public final int pulseLength;
    public boolean stateBefore;
    public SignalScheduleTicket pulseStart;
    public SignalScheduleTicket pulseEnd;
    
    public SignalOutputHandlerPulse(ISignalComponent component, int delay, CompoundTag nbt) {
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
        return SignalMode.PULSE;
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
        boolean current = state.any();
        if (pulseEnd == null && !stateBefore && current) {
            try {
                int bandwidth = getBandwidth();
                SignalState startState = SignalState.create(bandwidth).fill(true);
                SignalState endState = SignalState.create(bandwidth);
                pulseStart = LittleTiles.TICKERS.schedule(this, startState, delay);
                pulseEnd = LittleTiles.TICKERS.schedule(this, endState, delay + pulseLength);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        stateBefore = current;
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