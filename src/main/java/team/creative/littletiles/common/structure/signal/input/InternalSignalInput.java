package team.creative.littletiles.common.structure.signal.input;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponent;
import team.creative.littletiles.common.structure.signal.component.InternalSignal;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;

public class InternalSignalInput extends InternalSignal<InternalComponent> {
    
    public InternalSignalInput(LittleStructure parent, InternalComponent component) {
        super(parent, component);
    }
    
    @Override
    public void changed() {
        parent.changed(this);
    }
    
    @Override
    public SignalComponentType getComponentType() {
        return SignalComponentType.INPUT;
    }
    
    @Override
    public void load(CompoundTag nbt) {
        BooleanUtils.intToBool(nbt.getInt(component.identifier), getState());
    }
    
    @Override
    public CompoundTag write(boolean preview, CompoundTag nbt) {
        nbt.putInt(component.identifier, BooleanUtils.boolToInt(getState()));
        return nbt;
    }
}
