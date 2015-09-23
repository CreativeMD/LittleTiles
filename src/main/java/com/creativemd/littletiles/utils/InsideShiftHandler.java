package com.creativemd.littletiles.utils;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class InsideShiftHandler extends ShiftHandler{

	@Override
	public void handleRendering(Minecraft mc, double x, double y, double z) {
		
	}

	@Override
	public double getDistance(LittleTileVec suggestedPos) {
		return 1;
	}

	@Override
	protected LittleTileBox getNewPos(World world, int x, int y, int z, LittleTileBox suggested) {
		double offset = 0;
		if(suggested.minX < LittleTile.minPos)
		{
			offset = LittleTile.minPos-suggested.minX;
			
		}else if(suggested.maxX > LittleTile.maxPos){
			offset = LittleTile.maxPos-suggested.maxX;
		}
		suggested.minX += offset;
		suggested.maxX += offset;
		
		offset = 0;
		if(suggested.minY < LittleTile.minPos)
		{
			offset = LittleTile.minPos-suggested.minY;
			
		}else if(suggested.maxY > LittleTile.maxPos){
			offset = LittleTile.maxPos-suggested.maxY;
		}
		suggested.minY += offset;
		suggested.maxY += offset;
		
		offset = 0;
		if(suggested.minZ < LittleTile.minPos)
		{
			offset = LittleTile.minPos-suggested.minZ;
			
		}else if(suggested.maxZ > LittleTile.maxPos){
			offset = LittleTile.maxPos-suggested.maxZ;
		}
		suggested.minZ += offset;
		suggested.maxZ += offset;
		
		return suggested;
	}

}
