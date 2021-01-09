package com.creativemd.littletiles.common.entity;

import com.creativemd.littletiles.common.structure.type.LittleChair;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntitySit extends Entity {
    
    public LittleChair chair;
    
    public EntitySit(LittleChair chair, World world, double x, double y, double z) {
        super(world);
        this.chair = chair;
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
        if (!world.isRemote && !isBeingRidden()) {
            if (chair != null)
                chair.setPlayer(null);
            this.setDead();
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
        
    }
    
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        
    }
}
