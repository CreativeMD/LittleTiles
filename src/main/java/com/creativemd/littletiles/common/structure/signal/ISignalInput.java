package com.creativemd.littletiles.common.structure.signal;

public interface ISignalInput extends ISignalBase {
	
	public void setState(boolean[] state);
	
	public boolean[] getState();
	
	@Override
	public default SignalType getType() {
		return SignalType.INPUT;
	}
}
