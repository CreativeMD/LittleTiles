package com.creativemd.littletiles.common.structure.signal.network;

import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureBase;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;

public interface ISignalStructureTransmitter extends ISignalStructureBase {
    
    @Override
    public default SignalComponentType getType() {
        return SignalComponentType.TRANSMITTER;
    }
}
