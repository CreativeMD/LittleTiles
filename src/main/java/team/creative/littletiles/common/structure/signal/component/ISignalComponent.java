package team.creative.littletiles.common.structure.signal.component;

import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;

public interface ISignalComponent {
    
    public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException;
    
    public default void updateState(SignalState state) {
        try {
            if (!state.equals(getBandwidth(), getState())) {
                overwriteState(state);
                changed();
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public void changed() throws CorruptedConnectionException, NotYetConnectedException;
    
    public SignalState getState() throws CorruptedConnectionException, NotYetConnectedException;
    
    @Deprecated
    public void overwriteState(SignalState state) throws CorruptedConnectionException, NotYetConnectedException;
    
    public SignalComponentType getComponentType();
    
    public LittleStructure getStructure();
    
    public Level getStructureLevel();
    
}
