package com.creativemd.littletiles.common.utils.rotation;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class SlidingDoorTransformation extends DoorTransformation {
	
	public EnumFacing direction;
	public int distance;
	
	public SlidingDoorTransformation() {
		
	}
	
	public SlidingDoorTransformation(EnumFacing direction, int distance) {
		this.direction = direction;
		this.distance = distance;
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setInteger("direction", direction.ordinal());
		nbt.setInteger("distance", distance);
	}

	@Override
	protected void readFromNBT(NBTTagCompound nbt) {
		direction = EnumFacing.getFront(nbt.getInteger("direction"));
		distance = nbt.getInteger("distance");
	}

	@Override
	public void performTransformation(EntityDoorAnimation animation, double progress) {
		switch(direction)
		{
		case EAST:
			animation.posX = animation.getAxisPos().getX() - animation.startOffset.getX() - (distance*LittleTile.gridMCLength*(1-progress));
			break;
		case WEST:
			animation.posX = animation.getAxisPos().getX() - animation.startOffset.getX() + (distance*LittleTile.gridMCLength*(1-progress));
			break;
		case UP:
			animation.posY = animation.getAxisPos().getY() - animation.startOffset.getY() - (distance*LittleTile.gridMCLength*(1-progress));
			break;
		case DOWN:
			animation.posY = animation.getAxisPos().getY() - animation.startOffset.getY() + (distance*LittleTile.gridMCLength*(1-progress));
			break;
		case SOUTH:
			animation.posZ = animation.getAxisPos().getZ() - animation.startOffset.getZ() - (distance*LittleTile.gridMCLength*(1-progress));
			break;
		case NORTH:
			animation.posZ = animation.getAxisPos().getZ() - animation.startOffset.getZ() + (distance*LittleTile.gridMCLength*(1-progress));
			break;
		default:
			break;
		
		}
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof SlidingDoorTransformation)
			return ((SlidingDoorTransformation) object).direction == direction && ((SlidingDoorTransformation) object).distance == distance;
		return false;
	}
	
}
