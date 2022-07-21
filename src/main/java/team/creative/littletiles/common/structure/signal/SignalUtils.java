package team.creative.littletiles.common.structure.signal;

import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.InternalSignalInput;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class SignalUtils {
    
    public static ISignalComponent getInput(LittleStructure structure, int id, boolean external) {
        if (external)
            return getExternalInput(structure, id);
        return getInternalInput(structure, id);
        
    }
    
    public static InternalSignalInput getInternalInput(LittleStructure structure, int id) {
        if (id >= 0)
            return structure.getInput(id);
        return null;
    }
    
    public static ISignalStructureComponent getExternalInput(LittleStructure structure, int id) {
        try {
            LittleStructure child = structure.children.getChild(id).getStructure();
            if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getComponentType() == SignalComponentType.INPUT)
                return (ISignalStructureComponent) child;
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return null;
    }
    
    public static ISignalComponent getOutput(LittleStructure structure, int id, boolean external) {
        if (external)
            return getExternalOutput(structure, id);
        return getInternalOutput(structure, id);
    }
    
    public static InternalSignalOutput getInternalOutput(LittleStructure structure, int id) {
        if (id >= 0)
            return structure.getOutput(id);
        return null;
    }
    
    public static ISignalStructureComponent getExternalOutput(LittleStructure structure, int id) {
        try {
            LittleStructure child = structure.children.getChild(id).getStructure();
            if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getComponentType() == SignalComponentType.OUTPUT)
                return (ISignalStructureComponent) child;
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return null;
    }
    
}
