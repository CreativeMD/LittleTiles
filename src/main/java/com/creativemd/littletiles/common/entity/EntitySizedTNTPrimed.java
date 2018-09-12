package com.creativemd.littletiles.common.entity;

import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntitySizedTNTPrimed extends EntityTNTPrimed {

	private static final DataParameter<String> TNTSIZE = EntityDataManager.<String>createKey(EntitySizedTNTPrimed.class, DataSerializers.STRING);

	public EntitySizedTNTPrimed(World worldIn) {
		super(worldIn);
	}

	public LittleGridContext context;
	public LittleTileSize size;

	public EntitySizedTNTPrimed(World worldIn, double x, double y, double z, EntityLivingBase igniter, LittleGridContext context, LittleTileSize size) {
		super(worldIn, x, y, z, igniter);
		// setFuse(1000);
		setSize(context, size);
		setSize((float) size.getPosX(context), (float) size.getPosY(context));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(TNTSIZE, "1.1.1.1");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		size.writeToNBT("size", compound);
		context.set(compound);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		size = new LittleTileSize("size", compound);
		context = LittleGridContext.get(compound);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (TNTSIZE.equals(key)) {
			String data = (String) this.dataManager.get(TNTSIZE);
			this.size = new LittleTileSize(data);
			String[] coords = data.split("\\.");
			this.context = LittleGridContext.get(Integer.parseInt(coords[coords.length - 1]));

		}
	}

	public void setSize(LittleGridContext context, LittleTileSize size) {
		this.dataManager.set(TNTSIZE, size.toString() + "." + context.size);
		this.size = size;
		this.context = context;
	}

	public void onUpdate() {
		if (this.getFuse() <= 1) {
			this.setDead();

			if (!this.world.isRemote) {
				this.explode();
			}
		} else
			super.onUpdate();
	}

	protected void explode() {
		this.world.createExplosion(this, this.posX, this.posY + (double) (this.height / 16.0F), this.posZ, (float) (4.0D * size.getPercentVolume(context)), true);
	}

}
