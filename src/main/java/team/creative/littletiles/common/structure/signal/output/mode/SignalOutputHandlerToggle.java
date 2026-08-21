package team.creative.littletiles.common.structure.signal.output.mode;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public class SignalOutputHandlerToggle extends SignalOutputHandler {
    
    public SignalState stateBefore;
    public SignalState result;
    
    public SignalOutputHandlerToggle(ISignalComponent component, int delay, CompoundTag nbt, SignalState stateBefore, SignalState result) {
        super(component, delay, nbt);
        this.stateBefore = stateBefore;
        this.result = result;
    }
    
    @Override
    public SignalMode getMode() {
        return SignalMode.TOGGLE;
    }
    
    public void triggerToggle() {
        if (result == null) {
            try {
                int bandwidth = component.getBandwidth();
                result = SignalState.create(bandwidth);
                result = result.fill(component.getState());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        
        result = result.invert();
        performStateChange(result);
    }
    
    @Override
    public void queue(SignalState state) {
        try {
            int bandwidth = component.getBandwidth();
            if (stateBefore == null) {
                stateBefore = SignalState.create(bandwidth);
                result = SignalState.create(bandwidth);
            }
            
            for (int i = 0; i < bandwidth; i++) {
                if (!stateBefore.is(i) && state.is(i))
                    result = result.set(i, !result.is(i));
                stateBefore = stateBefore.set(i, state.is(i));
            }
            LittleTiles.TICKERS.schedule(this, result, delay);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @Override
    public void write(boolean preview, CompoundTag nbt) {
        if (stateBefore != null) {
            try {
                nbt.putInt("bandwidth", component.getBandwidth());
                nbt.put("before", stateBefore.save());
                nbt.put("result", result.save());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
        if (preview)
            return;
        List<SignalScheduleTicket> tickets = LittleTiles.TICKERS.findTickets(component, this);
        ListTag list = new ListTag();
        for (int i = 0; i < tickets.size(); i++) {
            SignalScheduleTicket ticket = tickets.get(i);
            list.add(new IntArrayTag(ticket.toArray()));
        }
        if (!list.isEmpty())
            nbt.put("tickets", list);
    }
}