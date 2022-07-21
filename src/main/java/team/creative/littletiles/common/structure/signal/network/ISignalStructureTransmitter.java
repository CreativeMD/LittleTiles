package team.creative.littletiles.common.structure.signal.network;

import team.creative.littletiles.common.structure.signal.component.ISignalStructureBase;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;

public interface ISignalStructureTransmitter extends ISignalStructureBase {
    
    @Override
    public default SignalComponentType getComponentType() {
        return SignalComponentType.TRANSMITTER;
    }
}
