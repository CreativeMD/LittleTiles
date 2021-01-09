package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.world.World;

public interface ISignalComponent {
    
    public int getBandwidth();
    
    public default void updateState(boolean[] state) {
        if (!BooleanUtils.equals(state, getState())) {
            BooleanUtils.set(getState(), state);
            changed();
        }
    }
    
    public void changed();
    
    public boolean[] getState();
    
    public SignalComponentType getType();
    
    public LittleStructure getStructure();
    
    public World getWorld();
    
}
