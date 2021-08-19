package com.creativemd.littletiles.common.structure.signal.component;

import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import team.creative.littletiles.common.structure.registry.LittleStructureType.InternalComponent;

public abstract class InternalSignal<T extends InternalComponent> implements ISignalComponent {
    
    public final LittleStructure parent;
    public final T component;
    private final boolean[] state;
    
    public InternalSignal(LittleStructure parent, T component) {
        this.parent = parent;
        this.component = component;
        this.state = new boolean[component.bandwidth];
    }
    
    public abstract void load(NBTTagCompound nbt);
    
    @Override
    public boolean[] getState() {
        return state;
    }
    
    public abstract NBTTagCompound write(boolean preview, NBTTagCompound nbt);
    
    @Override
    public int getBandwidth() {
        return state.length;
    }
    
    @Override
    public LittleStructure getStructure() {
        return parent;
    }
    
    @Override
    public World getStructureWorld() {
        if (parent.mainBlock != null)
            return parent.getWorld();
        return null;
    }
    
    @Override
    public String toString() {
        return component.identifier;
    }
    
}
