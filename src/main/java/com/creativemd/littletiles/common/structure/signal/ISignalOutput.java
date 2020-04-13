package com.creativemd.littletiles.common.structure.signal;

public interface ISignalOutput extends ISignalBase {
	
	public boolean[] getState();
	
	@Override
	public default SignalType getType() {
		return SignalType.OUTPUT;
	}
	
}
