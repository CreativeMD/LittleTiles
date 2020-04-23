package com.creativemd.littletiles.common.entity.old;

import java.util.ArrayList;
import java.util.UUID;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.outdated.OldDoorTransformation;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementResult;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.world.World;

@Deprecated
public class EntityOldDoorAnimation extends EntityOldAnimation {
	
	// private static final DataParameter<Float> ENTITY_PROGRESS =
	// EntityDataManager.<Double>createKey(EntityDoorAnimation.class,
	// DataSerializers.FLOAT);
	
	public EntityPlayer activator;
	
	public long started = System.currentTimeMillis();
	private double progress;
	public int duration;
	public boolean approved = true;
	
	public OldDoorTransformation transformation;
	
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
	
	public EntityOldDoorAnimation(World worldIn) {
		super(worldIn);
	}
	
	public EntityOldDoorAnimation(World world, CreativeWorld worldFake, ArrayList<TileEntityLittleTiles> blocks, LittleAbsolutePreviews previews, LittleAbsoluteVec axis, OldDoorTransformation transformation, UUID uuid, EntityPlayer activator, LittleVec additionalAxis, int duration) {
		super(world, worldFake, blocks, previews, uuid, axis, additionalAxis);
		
		this.activator = activator;
		
		this.transformation = transformation;
		this.duration = duration;
		
		setTransformationStartOffset();
		
		prevPosY -= 0.1; // To force an update
		updateBoundingBox();
		updateOrigin();
		
		if (world.isRemote)
			approved = false;
	}
	
	@Override
	public void copyExtra(EntityOldAnimation animation) {
		EntityOldDoorAnimation doorAnimation = (EntityOldDoorAnimation) animation;
		doorAnimation.progress = progress;
		doorAnimation.duration = duration;
		doorAnimation.approved = approved;
		doorAnimation.transformation = transformation;
		doorAnimation.lastSendProgress = lastSendProgress;
		animation.additionalAxis = additionalAxis.copy();
	}
	
	private float lastSendProgress = -1;
	
	public void setProgress(double progress) {
		if (progress > duration)
			this.progress = duration;
		else
			this.progress = progress;
		
		/* if(!world.isRemote && (lastSendProgress == -1 || progress - lastSendProgress
		 * > 10 || progress >= duration)) { dataManager.set(ENTITY_PROGRESS, progress);
		 * lastSendProgress = progress; } */
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
		if (transformation != null)
			transformation.performTransformation(this, progress / duration);
	}
	
	@Override
	public void onPostTick() {
		if (transformation == null)
			return;
		
		if (progress >= duration) {
			// Try to place door, if not drop ItemStack
			LittleStructure placedStructureParent = getParentStructure();
			reloadPreviews(placedStructureParent, previews.pos);
			
			if (!world.isRemote || approved) {
				
				Placement placement = new Placement(null, PlacementHelper.getAbsolutePreviews(fakeWorld, previews, previews.pos, PlacementMode.all));
				
				PlacementResult result;
				if ((result = placement.tryPlace()) != null) {
					
					LittleStructure newDoor = result.parentStructure;
					if (placedStructureParent.parent != null && placedStructureParent.parent.isConnected(world)) {
						LittleStructure parentStructure = placedStructureParent.parent.getStructureWithoutLoading();
						
						parentStructure.updateChildConnection(newDoor.parent.getChildID(), newDoor);
						newDoor.updateParentConnection(newDoor.parent.getChildID(), parentStructure);
					}
				} else if (!world.isRemote)
					WorldUtils.dropItem(world, getParentStructure().getStructureDrop(), baseOffset);
			}
			
			isDead = true;
		} else {
			setProgress(((System.currentTimeMillis() - started) / 50D));
		}
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		duration = compound.getInteger("duration");
		NBTBase tag = compound.getTag("progress");
		if (tag instanceof NBTTagInt)
			setProgress(((NBTTagInt) tag).getInt());
		else if (tag instanceof NBTTagFloat)
			setProgress(((NBTTagFloat) tag).getFloat());
		else
			setProgress(((NBTTagDouble) tag).getDouble());
		
		transformation = OldDoorTransformation.loadFromNBT(compound.getCompoundTag("transform"));
		setTransformationStartOffset();
		
		started = System.currentTimeMillis() - (long) (progress * 50);
		
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setInteger("duration", duration);
		compound.setDouble("progress", progress);
		
		compound.setTag("transform", transformation.writeToNBT(new NBTTagCompound()));
	}
	
}
