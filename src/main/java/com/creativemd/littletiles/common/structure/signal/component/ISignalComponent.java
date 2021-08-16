package com.creativemd.littletiles.common.structure.signal.component;

import net.minecraft.world.level.Level;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

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
    
    public SignalComponentType getComponentType();
    
    public LittleStructure getStructure();
    
    public Level getStructureLevel();
    
}
