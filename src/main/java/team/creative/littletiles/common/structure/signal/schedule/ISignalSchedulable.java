package team.creative.littletiles.common.structure.signal.schedule;

import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public interface ISignalSchedulable {
    
    public void notifyChange();
    
    public boolean hasChanged();
    
    public void markChanged();
    
    public void markUnchanged();
    
    public Level getComponentLevel();
    
    public boolean isStillAvailable();
    
    public default void updateSignaling() throws CorruptedConnectionException, NotYetConnectedException {
        markUnchanged();
        notifyChange();
    }
    
    public default void schedule() {
        if (!hasChanged() && isStillAvailable()) {
            SignalTicker.schedule(getComponentLevel(), this);
            markChanged();
        }
    }
    
}
