package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RelativeBlockPos {
	
	private BlockPos coord;
	
	public RelativeBlockPos(TileEntity te, BlockPos coord)
	{
		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), coord);
	}
	
	public RelativeBlockPos(BlockPos origin, BlockPos coord)
	{
		this(origin.getX(), origin.getY(), origin.getZ(), coord);
	}
	
	public RelativeBlockPos(int baseX, int baseY, int baseZ, BlockPos coord)
	{
		this(coord.getX() - baseX, coord.getY() - baseY, coord.getZ() - baseZ);
	}
	
	protected RelativeBlockPos(int relativeX, int relativeY, int relativeZ)
	{
		this.coord = new BlockPos(relativeX, relativeY, relativeZ);
	}
	
	public RelativeBlockPos(int[] array)
	{
		if(array.length > 3)
		{
			this.coord = new BlockPos(array[0], array[1], array[2]);
		}
	}
	
	public BlockPos getRelativePos()
	{
		return coord;
	}
	
	public BlockPos getAbsolutePos(TileEntity te)
	{
		return getAbsolutePos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
	}
	
	public BlockPos getAbsolutePos(BlockPos origin)
	{
		return getAbsolutePos(origin.getX(), origin.getY(), origin.getZ());
	}
	
	public BlockPos getAbsolutePos(int x, int y, int z)
	{
		return new BlockPos(coord.getX()+x, coord.getY()+y, coord.getZ()+z);
	}
	
	@Override
	public String toString()
	{
		return "pos:[" + coord.getX() + "," + coord.getY() + "," + coord.getZ() + "]";
	}
	
	public RelativeBlockPos copy()
	{
		return new RelativeBlockPos(coord.getX(), coord.getY(), coord.getZ());
	}
	
}
