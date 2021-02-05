package com.creativemd.littletiles.common.structure.signal;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.InternalSignalInput;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;

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
        if (id >= 0 && id < structure.countChildren())
            try {
                LittleStructure child = structure.getChild(id).getStructure();
                if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getType() == SignalComponentType.INPUT)
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
        if (id >= 0 && id < structure.countChildren())
            try {
                LittleStructure child = structure.getChild(id).getStructure();
                if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getType() == SignalComponentType.OUTPUT)
                    return (ISignalStructureComponent) child;
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return null;
    }
    
    public static boolean is(boolean[] state, int[] indexes) {
        for (int i = 0; i < state.length; i++) {
            if (i >= indexes.length)
                continue;
            int indexState = indexes[i];
            if (indexState == 2)
                continue;
            if (indexState != (state[i] ? 1 : 0))
                return false;
        }
        return true;
    }
    
    public static void combine(boolean[] state, boolean[] second) {
        int count = Math.min(state.length, second.length);
        for (int i = 0; i < count; i++)
            state[i] = second[i];
    }
    
}
