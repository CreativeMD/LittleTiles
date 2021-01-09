package com.creativemd.littletiles.common.structure.signal.input;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponent;
import com.creativemd.littletiles.common.structure.signal.component.InternalSignal;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;

import net.minecraft.nbt.NBTTagCompound;

public class InternalSignalInput extends InternalSignal<InternalComponent> {
    
    public InternalSignalInput(LittleStructure parent, InternalComponent component) {
        super(parent, component);
    }
    
    @Override
    public void changed() {
        parent.changed(this);
    }
    
    @Override
    public SignalComponentType getType() {
        return SignalComponentType.INPUT;
    }
    
    @Override
    public void load(NBTTagCompound nbt) {
        BooleanUtils.intToBool(nbt.getInteger(component.identifier), getState());
    }
    
    @Override
    public NBTTagCompound write(boolean preview, NBTTagCompound nbt) {
        nbt.setInteger(component.identifier, BooleanUtils.boolToInt(getState()));
        return nbt;
    }
}
