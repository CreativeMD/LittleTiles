package com.creativemd.littletiles.common.structure.signal.input;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;

public class InternalSignalInput extends InternalSignal {
	
	public InternalSignalInput(LittleStructure parent, String name, int bandwidth) {
		super(parent, name, bandwidth);
	}
	
	@Override
	public void changed() {
		parent.changed(this);
	}
	
	@Override
	public SignalComponentType getType() {
		return SignalComponentType.INPUT;
	}
}
