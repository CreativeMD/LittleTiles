package team.creative.littletiles.common.structure.signal.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType.InternalComponent;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.SignalState;

public abstract class InternalSignal<T extends InternalComponent> implements ISignalComponent {
    
    public final LittleStructure parent;
    public final T component;
    private SignalState state;
    
    public InternalSignal(LittleStructure parent, T component) {
        this.parent = parent;
        this.component = component;
        this.state = SignalState.create(component.bandwidth);
    }
    
    public abstract void load(CompoundTag nbt);
    
    @Override
    public SignalState getState() {
        return state;
    }
    
    protected void overwrite(SignalState state) {
        this.state = state;
    }
    
    @Override
    public void overwriteState(SignalState state) throws CorruptedConnectionException, NotYetConnectedException {
        this.state = this.state.overwrite(state);
        this.state.shrinkTo(getBandwidth());
    }
    
    public abstract CompoundTag write(boolean preview, CompoundTag nbt);
    
    @Override
    public int getBandwidth() {
        return component.bandwidth;
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
