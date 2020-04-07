package com.creativemd.littletiles.common.signal;

public interface ISignalOutput extends ISignalBase {
	
	public boolean[] getState();
	
	@Override
	public default SignalType getType() {
		return SignalType.OUTPUT;
	}
	
}
