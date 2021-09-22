package team.creative.littletiles.common.structure.signal.schedule;

public interface ISignalScheduleTicket {
    
    public int getDelay();
    
    public boolean[] getState();
    
    public void overwriteState(boolean[] newState);
    
    public void markObsolete();
    
}
