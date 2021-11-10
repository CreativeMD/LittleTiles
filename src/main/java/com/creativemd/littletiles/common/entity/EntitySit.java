package com.creativemd.littletiles.common.entity;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.connection.IWorldPositionProvider;
import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.type.LittleChair;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySit extends Entity implements IWorldPositionProvider, INoPushEntity {
    
    public static final DataParameter<NBTTagCompound> CONNECTION = EntityDataManager.createKey(EntitySit.class, DataSerializers.COMPOUND_TAG);
    public static final DataParameter<Float> CHAIRX = EntityDataManager.createKey(EntitySit.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> CHAIRY = EntityDataManager.createKey(EntitySit.class, DataSerializers.FLOAT);
    public static final DataParameter<Float> CHAIRZ = EntityDataManager.createKey(EntitySit.class, DataSerializers.FLOAT);
    private StructureChildConnection temp;
    
    public EntitySit(LittleChair chair, World world, double x, double y, double z) {
        super(world);
        dataManager.set(CHAIRX, (float) x);
        dataManager.set(CHAIRY, (float) y);
        dataManager.set(CHAIRZ, (float) z);
        this.temp = chair.generateConnection(this);
        this.dataManager.set(CONNECTION, temp.writeToNBT(new NBTTagCompound()));
        noClip = true;
        preventEntitySpawning = true;
        width = 0.0F;
        height = 0.0F;
        setPosition(x, y, z);
    }
    
    public EntitySit(World world) {
        super(world);
        noClip = true;
        preventEntitySpawning = true;
        width = 0.0F;
        height = 0.0F;
    }
    
    @Override
    public boolean canBePushed() {
        return false;
    }
    
    @Override
    public void onUpdate() {
        super.onUpdate();
        StructureChildConnection connection = StructureChildConnection.loadFromNBT(this, dataManager.get(CONNECTION), false);
        if (!world.isRemote && !isBeingRidden()) {
            try {
                
                LittleStructure structure = connection.getStructure();
                if (structure instanceof LittleChair)
                    ((LittleChair) structure).setPlayer(null);
                this.setDead();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            
        } else {
            try {
                LittleStructure structure = connection.getStructure();
                if (structure.getWorld() instanceof IOrientatedWorld) {
                    Vector3d vec = new Vector3d(dataManager.get(CHAIRX), dataManager.get(CHAIRY), dataManager.get(CHAIRZ));
                    ((IOrientatedWorld) structure.getWorld()).getOrigin().transformPointToWorld(vec);
                    setPosition(vec.x, vec.y, vec.z);
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public double getMountedYOffset() {
        return 0;
    }
    
    protected boolean isAIEnabled() {
        return false;
    }
    
    @Override
    protected void entityInit() {
        this.dataManager.register(CONNECTION, new NBTTagCompound());
        this.dataManager.register(CHAIRX, 0F);
        this.dataManager.register(CHAIRY, 0F);
        this.dataManager.register(CHAIRZ, 0F);
    }
    
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        dataManager.set(CONNECTION, nbt.getCompoundTag("connection"));
        dataManager.set(CHAIRX, nbt.getFloat("chairX"));
        dataManager.set(CHAIRY, nbt.getFloat("chairY"));
        dataManager.set(CHAIRZ, nbt.getFloat("chairZ"));
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setTag("connection", dataManager.get(CONNECTION));
        nbt.setFloat("chairX", dataManager.get(CHAIRX));
        nbt.setFloat("chairY", dataManager.get(CHAIRY));
        nbt.setFloat("chairZ", dataManager.get(CHAIRZ));
    }
    
    @Override
    public World getWorld() {
        return world;
    }
    
    @Override
    public BlockPos getPos() {
        return BlockPos.ORIGIN;
    }
    
    @Override
    public void onStructureDestroyed() {}
}
