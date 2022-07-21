package team.creative.littletiles.common.structure.signal.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponent;

public abstract class InternalSignal<T extends InternalComponent> implements ISignalComponent {
    
    public final LittleStructure parent;
    public final T component;
    private final boolean[] state;
    
    public InternalSignal(LittleStructure parent, T component) {
        this.parent = parent;
        this.component = component;
        this.state = new boolean[component.bandwidth];
    }
    
    public abstract void load(CompoundTag nbt);
    
    @Override
    public boolean[] getState() {
        return state;
    }
    
    public abstract CompoundTag write(boolean preview, CompoundTag nbt);
    
    @Override
    public int getBandwidth() {
        return state.length;
    }
    
    @Override
    public LittleStructure getStructure() {
        return parent;
    }
    
    @Override
    public Level getStructureLevel() {
        if (parent.mainBlock != null)
            return parent.getLevel();
        return null;
    }
    
    @Override
    public String toString() {
        return component.identifier;
    }
    
}
