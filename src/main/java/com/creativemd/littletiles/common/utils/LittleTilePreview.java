package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

public class LittleTilePreview {
	
	public boolean canSplit = true;
	public LittleTileSize size = null;
	/**Used for multiblocks**/
	public ArrayList<LittleTilePreview> subTiles = new ArrayList<LittleTilePreview>(); 
	
	public LittleTileVec min = null;
	public LittleTileVec max = null;
	
	public LittleTilePreview(LittleTileSize size)
	{
		this.size = size;
	}
	
	public AxisAlignedBB getLittleBox()
	{
		return AxisAlignedBB.getBoundingBox(min.posX, min.posY, min.posZ, max.posX, max.posY, max.posZ);
	}
	
	public void updateSize()
	{
		size = new LittleTileSize((byte)(max.posX - min.posX), (byte)(max.posY - min.posY), (byte)(max.posZ - min.posZ));
	}
	
	public ArrayList<LittleTilePreview> getAllTiles()
	{
		ArrayList<LittleTilePreview> tiles = new ArrayList<LittleTilePreview>();
		if(min != null && max != null)
			tiles.add(this);
		for (int i = 0; i < subTiles.size(); i++) {
			tiles.addAll(subTiles.get(i).getAllTiles());
		}
		return tiles;
	}
	
	public static LittleTilePreview getPreviewFromNBT(NBTTagCompound nbt)
	{
		if(nbt == null)
			return null;
		LittleTileSize size = null;
		if(nbt.hasKey("sizeX"))
			size = new LittleTileSize(nbt.getByte("sizeX"), nbt.getByte("sizeY"), nbt.getByte("sizeZ"));
		LittleTileVec min = null;
		LittleTileVec max = null;
		if(nbt.hasKey("ix"))
		{
			min = new LittleTileVec(nbt.getByte("ix"), nbt.getByte("iy"), nbt.getByte("iz"));
			max = new LittleTileVec(nbt.getByte("ax"), nbt.getByte("ay"), nbt.getByte("az"));
			if(size == null)
			{
				size = new LittleTileSize(max.posX-min.posX, max.posY-min.posY, max.posZ-min.posZ);
			}
		}
		
		if(size != null)
		{
			LittleTilePreview preview = new LittleTilePreview(size);
			preview.min = min;
			preview.max = max;
			return preview;
		}else{
			return null;
		}
	}
	
}
