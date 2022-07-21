package team.creative.littletiles.common.structure.signal.input;

import net.minecraft.nbt.CompoundTag;
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
        overwrite(getState().load(nbt.get(component.identifier)));
    }
    
    @Override
    public CompoundTag save(boolean preview, CompoundTag nbt) {
        nbt.put(component.identifier, getState().save());
        return nbt;
    }
}
