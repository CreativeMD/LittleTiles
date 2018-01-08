package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileAbsoluteCoord {
	
	public BlockPos pos;
	public int[] identifier;
	
	public LittleTileAbsoluteCoord(LittleTile tile)
	{
		this(tile.te, tile.getIdentifier());
	}
	
	public LittleTileAbsoluteCoord(TileEntity te, int[] identifier)
	{
		this(te.getPos(), identifier);
	}
	
	public LittleTileAbsoluteCoord(BlockPos pos, int[] identifier)
	{
		this.pos = pos;
		this.identifier = identifier;
	}
	
	public LittleTileAbsoluteCoord(NBTTagCompound nbt)
	{
		int[] array = nbt.getIntArray("pos");
		if(array.length == 3)
			pos = new BlockPos(array[0], array[1], array[2]);
		else
			throw new InvalidParameterException("No valid coord given " + nbt);
		
		identifier = nbt.getIntArray("id");
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
		nbt.setIntArray("id", identifier);
	}
	
	
}
