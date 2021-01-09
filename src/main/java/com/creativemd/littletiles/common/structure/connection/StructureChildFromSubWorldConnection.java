package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureChildFromSubWorldConnection extends StructureChildConnection {
    
    public StructureChildFromSubWorldConnection(LittleStructure parent, int childId, BlockPos relative, int index, int attribute) {
        super(parent, true, childId, relative, index, attribute);
    }
    
    public StructureChildFromSubWorldConnection(LittleStructure parent, NBTTagCompound nbt) {
        super(parent, true, nbt);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setBoolean("subWorld", true);
        return nbt;
    }
    
    @Override
    protected World getWorld() throws CorruptedConnectionException, NotYetConnectedException {
        return ((SubWorld) parent.getWorld()).parentWorld;
    }
    
    @Override
    public EntityAnimation getAnimation() {
        SubWorld fakeWorld = (SubWorld) parent.getWorld();
        return (EntityAnimation) fakeWorld.parent;
    }
    
    @Override
    public void destroyStructure() {
        SubWorld fakeWorld = (SubWorld) parent.getWorld();
        ((EntityAnimation) fakeWorld.parent).markRemoved();
        parent.onStructureDestroyed();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
    
}
