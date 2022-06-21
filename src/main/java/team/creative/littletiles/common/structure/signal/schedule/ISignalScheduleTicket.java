package team.creative.littletiles.common.structure.signal.schedule;

import team.creative.littletiles.common.structure.signal.SignalState;

public interface ISignalScheduleTicket {
    
    public int getDelay();
    
    public SignalState getState();
    
    public void overwriteState(SignalState newState);
    
    public void markObsolete();
    
}
