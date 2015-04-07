package com.creativemd.littletiles.common.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

public class PlacementHelper {
	
	public MovingObjectPosition moving;
	public EntityPlayer player;
	
	public PlacementHelper(MovingObjectPosition moving, EntityPlayer player)
	{
		this.moving = moving;
		this.player = player;
	}
	
	public Vec3 getLookingPos()
	{
		double posX = moving.hitVec.xCoord - moving.blockX;
		double posY = moving.hitVec.yCoord - moving.blockY;
		double posZ = moving.hitVec.zCoord - moving.blockZ;
		//int temp = (int)(posX*16);
		posX = ((int)(posX*16))/16D;
		posY = ((int)(posY*16))/16D;
		posZ = ((int)(posZ*16))/16D;
		//posX = posX*16-7;
		//posY = posY*16-7;
		//posZ = posZ*16-7;
		return Vec3.createVectorHelper(posX, posY, posZ);
	}
	
}
