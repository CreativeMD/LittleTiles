package com.creativemd.littletiles.common.entity;

import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntitySizedTNTPrimed extends EntityTNTPrimed {
	
	private static final DataParameter<String> TNTSIZE = EntityDataManager.<String>createKey(EntitySizedTNTPrimed.class, DataSerializers.STRING);

	public EntitySizedTNTPrimed(World worldIn) {
		super(worldIn);
	}
	
	public LittleTileSize size;
	
	public EntitySizedTNTPrimed(World worldIn, double x, double y, double z, EntityLivingBase igniter, LittleTileSize size)
    {
		super(worldIn, x, y, z, igniter);
		//setFuse(1000);
		setSize(size);
		setSize((float) size.getPosX(), (float) size.getPosY());
    }
	
	@Override
	protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(TNTSIZE, "16.16.16");
    }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound)
    {
		super.writeEntityToNBT(compound);
        size.writeToNBT("size", compound);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
    	super.readEntityFromNBT(compound);
        size = new LittleTileSize("size", compound);
    }
    
    @Override
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (TNTSIZE.equals(key))
        {
            this.size = new LittleTileSize((String) this.dataManager.get(TNTSIZE));
        }
    }
    
    public void setSize(LittleTileSize size)
    {
        this.dataManager.set(TNTSIZE, size.toString());
        this.size = size;
    }
	
	public void onUpdate()
	{
		if (this.getFuse() <= 1)
		{
			this.setDead();
			
			if (!this.worldObj.isRemote)
			{
				this.explode();
			}
		}else
			super.onUpdate();
	}

	protected void explode()
	{
	    this.worldObj.createExplosion(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, (float) (4.0D*size.getPercentVolume()), true);
	}

}
