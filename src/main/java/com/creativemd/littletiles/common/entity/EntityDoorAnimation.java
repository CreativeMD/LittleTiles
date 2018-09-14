package com.creativemd.littletiles.common.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceRelative;
import com.creativemd.littletiles.common.structure.type.LittleDoorBase;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.transformation.DoorTransformation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityDoorAnimation extends EntityAnimation<EntityDoorAnimation> {
	
	// private static final DataParameter<Float> ENTITY_PROGRESS =
	// EntityDataManager.<Double>createKey(EntityDoorAnimation.class,
	// DataSerializers.FLOAT);
	
	public EntityPlayer activator;
	
	public long started = System.currentTimeMillis();
	private double progress;
	public int duration;
	public boolean approved = true;
	
	public DoorTransformation transformation;
	
	public BlockPos previewPos;
	
	public void setTransformationStartOffset() {
		preventPush = true;
		transformation.performTransformation(this, 0);
		prevWorldRotX = worldRotX;
		prevWorldRotY = worldRotY;
		prevWorldRotZ = worldRotZ;
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		preventPush = false;
	}
	
	public EntityDoorAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityDoorAnimation(World world, WorldFake worldFake, LittleDoorBase structure, ArrayList<TileEntityLittleTiles> blocks, PlacePreviews previews, LittleTilePos axis, DoorTransformation transformation, UUID uuid, EntityPlayer activator, LittleTileVec additionalAxis, BlockPos previewPos) {
		super(world, worldFake, blocks, previews, uuid, axis, additionalAxis);
		
		this.activator = activator;
		this.structure = structure;
		
		this.transformation = transformation;
		this.duration = structure.duration;
		
		this.previewPos = previewPos;
		
		setTransformationStartOffset();
		
		prevPosY -= 0.1; // To force an update
		updateBoundingBox();
		updateOrigin();
		
		if (world.isRemote)
			approved = false;
	}
	
	@Override
	public void copyExtra(EntityDoorAnimation animation) {
		animation.progress = progress;
		animation.duration = duration;
		animation.approved = approved;
		animation.transformation = transformation;
		animation.lastSendProgress = lastSendProgress;
		animation.additionalAxis = additionalAxis.copy();
	}
	
	private float lastSendProgress = -1;
	
	public void setProgress(double progress) {
		if (progress > duration)
			this.progress = duration;
		else
			this.progress = progress;
		
		/*
		 * if(!world.isRemote && (lastSendProgress == -1 || progress - lastSendProgress
		 * > 10 || progress >= duration)) { dataManager.set(ENTITY_PROGRESS, progress);
		 * lastSendProgress = progress; }
		 */
	}
	
	public double getProgress() {
		return progress;
	}
	
	@Override
	protected void entityInit() {
		// this.dataManager.register(ENTITY_PROGRESS, 0F);
	}
	
	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		// if (world.isRemote && ENTITY_PROGRESS.equals(key) && !isWaitingForRender())
		// this.progress = this.dataManager.get(ENTITY_PROGRESS).intValue();
	}
	
	@Override
	public void onTick() {
		if (world.isRemote && isWaitingForRender())
			return;
		if (transformation != null)
			transformation.performTransformation(this, progress / (double) duration);
	}
	
	@Override
	public void onPostTick() {
		if (transformation == null)
			return;
		
		if (world.isRemote && isWaitingForRender()) {
			ticksToWait--;
			
			if (ticksToWait % 10 == 0) {
				List<TileEntityLittleTiles> tileEntities = null;
				for (Iterator iterator = waitingForRender.iterator(); iterator.hasNext();) {
					TileEntityLittleTiles te = (TileEntityLittleTiles) iterator.next();
					if (te != te.getWorld().getTileEntity(te.getPos())) {
						if (tileEntities == null)
							tileEntities = new ArrayList<>();
						tileEntities.add(te);
					}
				}
				if (tileEntities != null)
					waitingForRender.removeAll(tileEntities);
			}
			
			if (waitingForRender.size() == 0 || ticksToWait < 0) {
				unloadRenderCache();
				isDead = true;
			} else
				isDead = false;
		} else {
			if (progress >= duration) {
				// Try to place door, if not drop ItemStack
				LittleDoorBase structure = this.structure.copyToPlaceDoor();
				
				if (world.isRemote)
					structure.isWaitingForApprove = true;
				
				if (!world.isRemote || approved) {
					if (LittleActionPlaceRelative.placeTilesWithoutPlayer(world, previews.context, previews, structure, PlacementMode.all, previewPos, null, null, null, EnumFacing.EAST) != null) {
						if (world.isRemote) {
							waitingForRender = new CopyOnWriteArrayList<>();
							ArrayList<BlockPos> coordsToCheck = new ArrayList<>(LittleActionPlaceRelative.getSplittedTiles(previews.context, previews, previewPos).keySet());
							for (int i = 0; i < coordsToCheck.size(); i++) {
								TileEntity te = world.getTileEntity(coordsToCheck.get(i));
								if (te instanceof TileEntityLittleTiles) {
									((TileEntityLittleTiles) te).waitingAnimation = this;
									waitingForRender.add((TileEntityLittleTiles) te);
								}
							}
							ticksToWait = 200;
							isDead = false;
							return;
						}
					} else if (!world.isRemote)
						WorldUtils.dropItem(world, this.structure.getStructureDrop(), baseOffset);
				}
				
				isDead = true;
			} else {
				setProgress(((System.currentTimeMillis() - started) / 50D));
			}
		}
	}
	
	@Override
	protected BlockPos getPreviewOffset() {
		return previewPos;
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		
		int[] array = compound.getIntArray("previewPos");
		if (array.length == 3)
			previewPos = new BlockPos(array[0], array[1], array[2]);
		else
			previewPos = baseOffset;
		
		super.readEntityFromNBT(compound);
		duration = compound.getInteger("duration");
		NBTBase tag = compound.getTag("progress");
		if (tag instanceof NBTTagInt)
			setProgress(((NBTTagInt) tag).getInt());
		else if (tag instanceof NBTTagFloat)
			setProgress(((NBTTagFloat) tag).getFloat());
		else
			setProgress(((NBTTagDouble) tag).getDouble());
		
		transformation = DoorTransformation.loadFromNBT(compound.getCompoundTag("transform"));
		setTransformationStartOffset();
		
		started = System.currentTimeMillis() - (long) (progress * 50);
		
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setInteger("duration", duration);
		compound.setDouble("progress", progress);
		
		compound.setIntArray("previewPos", new int[] { previewPos.getX(), previewPos.getY(), previewPos.getZ() });
		
		compound.setTag("transform", transformation.writeToNBT(new NBTTagCompound()));
	}
	
}
