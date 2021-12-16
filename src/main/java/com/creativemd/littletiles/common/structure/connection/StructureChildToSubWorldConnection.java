package com.creativemd.littletiles.common.structure.connection;

import java.util.UUID;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.MissingAnimationException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.world.LittleNeighborUpdateCollector;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureChildToSubWorldConnection extends StructureChildConnection {
    
    public final UUID entityUUID;
    
    public StructureChildToSubWorldConnection(IWorldPositionProvider parent, boolean dynamic, int childId, BlockPos relative, int index, int attribute, UUID entityUUID) {
        super(parent, false, dynamic, childId, relative, index, attribute);
        this.entityUUID = entityUUID;
    }
    
    public StructureChildToSubWorldConnection(IWorldPositionProvider parent, NBTTagCompound nbt) {
        super(parent, false, nbt);
        this.entityUUID = UUID.fromString(nbt.getString("entity"));
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setString("entity", entityUUID.toString());
        return nbt;
    }
    
    @Override
    protected World getWorld() throws CorruptedConnectionException, NotYetConnectedException {
        EntityAnimation animation = WorldAnimationHandler.getHandler(super.getWorld()).findAnimation(entityUUID);
        if (animation != null)
            return animation.fakeWorld;
        throw new MissingAnimationException(entityUUID);
    }
    
    @Override
    public EntityAnimation getAnimation() {
        return null;
    }
    
    @Override
    public void destroyStructure(LittleNeighborUpdateCollector neighbor) throws CorruptedConnectionException, NotYetConnectedException {
        getStructure().onStructureDestroyed();
        EntityAnimation animation = WorldAnimationHandler.getHandler(super.getWorld()).findAnimation(entityUUID);
        if (animation != null)
            animation.destroyAndNotify();
        neighbor = animation != null ? new LittleNeighborUpdateCollector(animation.fakeWorld) : LittleNeighborUpdateCollector.EMPTY;
        for (StructureChildConnection child : getStructure().getChildren())
            child.destroyStructure(neighbor);
        neighbor.process();
    }
    
    @Override
    public boolean isLinkToAnotherWorld() {
        return true;
    }
}
