package com.creativemd.littletiles.common.structure.signal.output;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;

public class InternalSignalOutput extends InternalSignal {
	
	public InternalSignalOutput(LittleStructure parent, String name, int bandwidth) {
		super(parent, name, bandwidth);
	}
	
	@Override
	public void changed() {
		parent.performInternalOutputChange(this);
	}
	
	@Override
	public SignalComponentType getType() {
		return SignalComponentType.OUTPUT;
	}
	
}
