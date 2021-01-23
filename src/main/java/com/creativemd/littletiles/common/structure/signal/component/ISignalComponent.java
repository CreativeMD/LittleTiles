package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;

import net.minecraft.world.World;

public interface ISignalComponent {
    
    public int getBandwidth() throws CorruptedConnectionException, NotYetConnectedException;
    
    public default void updateState(boolean[] state) {
        try {
            if (!BooleanUtils.equals(state, getState())) {
                BooleanUtils.set(getState(), state);
                changed();
            }
        } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public void changed() throws CorruptedConnectionException, NotYetConnectedException;
    
    public boolean[] getState() throws CorruptedConnectionException, NotYetConnectedException;
    
    public SignalComponentType getType();
    
    public LittleStructure getStructure();
    
    public World getStructureWorld();
    
}
