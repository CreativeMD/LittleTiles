package com.creativemd.littletiles.common.signal;

public interface ISignalTransmitter extends ISignalBase {
	
	@Override
	public default SignalType getType() {
		return SignalType.TRANSMITTER;
	}
}
