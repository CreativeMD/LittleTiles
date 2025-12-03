package team.creative.littletiles.common.structure.signal.output.mode;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;
import team.creative.littletiles.common.structure.signal.schedule.SignalScheduleTicket;

public abstract class SignalOutputHandlerStoreOne extends SignalOutputHandler {
    
    public SignalScheduleTicket ticket;
    
    public SignalOutputHandlerStoreOne(ISignalComponent component, int delay, CompoundTag nbt) {
        super(component, delay, nbt);
    }
    
}