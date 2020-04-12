package com.creativemd.littletiles.common.signal;

public interface ISignalInput extends ISignalBase {
	
	public void setState(boolean[] state);
	
	@Override
	public default SignalType getType() {
		return SignalType.INPUT;
	}
}
