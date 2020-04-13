package com.creativemd.littletiles.common.structure.signal;

public interface ISignalTransmitter extends ISignalBase {
	
	@Override
	public default SignalType getType() {
		return SignalType.TRANSMITTER;
	}
}
