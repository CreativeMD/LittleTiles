package team.creative.littletiles.common.structure.signal;

import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;

public interface SignalContext {
    
    public SignalContext getNestedSignalContext(int child) throws CorruptedConnectionException, NotYetConnectedException;
    
    public boolean hasSignalContextParent();
    
    public SignalContext getParentSignalContext() throws CorruptedConnectionException, NotYetConnectedException;
    
    public ISignalComponent getInput(int id, boolean external);
    
    public ISignalComponent getOutput(int id, boolean external);
    
}
