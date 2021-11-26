package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.world.LittleNeighborUpdateCollector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureChildFromSubWorldConnection extends StructureChildConnection {
    
    public StructureChildFromSubWorldConnection(IWorldPositionProvider parent, boolean dynamic, int childId, BlockPos relative, int index, int attribute) {
        super(parent, true, dynamic, childId, relative, index, attribute);
    }
    
    public StructureChildFromSubWorldConnection(IWorldPositionProvider parent, NBTTagCompound nbt) {
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
    public void destroyStructure(LittleNeighborUpdateCollector neighbor) {
        SubWorld fakeWorld = (SubWorld) parent.getWorld();
        ((EntityAnimation) fakeWorld.parent).destroyAndNotify();
        parent.onStructureDestroyed();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
    
}
