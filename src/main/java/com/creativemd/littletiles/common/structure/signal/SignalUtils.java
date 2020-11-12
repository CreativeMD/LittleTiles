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
	
	public static boolean[] getInputState(LittleStructure structure, int id, boolean external) {
		ISignalComponent component = getInput(structure, id, external);
		if (component != null)
			return component.getState();
		return null;
	}
	
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
		if (id >= 0 && id < structure.getChildren().size())
			try {
				LittleStructure child = structure.getChild(id).getStructure();
				if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getType() == SignalComponentType.INPUT)
					return (ISignalStructureComponent) child;
			} catch (CorruptedConnectionException | NotYetConnectedException e) {}
		return null;
	}
	
	public static void setOutputState(LittleStructure structure, int id, boolean external, boolean[] state) {
		ISignalComponent component = getOutput(structure, id, external);
		if (component != null)
			component.updateState(state);
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
		if (id >= 0 && id < structure.getChildren().size())
			try {
				LittleStructure child = structure.getChild(id).getStructure();
				if (child instanceof ISignalStructureComponent && ((ISignalStructureComponent) child).getType() == SignalComponentType.OUTPUT)
					return (ISignalStructureComponent) child;
			} catch (CorruptedConnectionException | NotYetConnectedException e) {}
		return null;
	}
	
	public static boolean is(boolean[] state, int[] indexes) {
		for (int i = 0; i < indexes.length; i++) {
			int index = indexes[i];
			if (index > 0 != (Math.abs(index) < state.length && state[Math.abs(index)]))
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
