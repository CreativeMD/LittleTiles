package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileCoord {
	
	private BlockPos coord;
	public LittleTileVec position;
	
	public LittleTileCoord(TileEntity te, BlockPos coord, LittleTileVec position)
	{
		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), coord, position);
	}
	
	public LittleTileCoord(BlockPos origin, BlockPos coord, LittleTileVec position)
	{
		this(origin.getX(), origin.getY(), origin.getZ(), coord, position);
	}
	
	public LittleTileCoord(int baseX, int baseY, int baseZ, BlockPos coord, LittleTileVec position)
	{
		this(coord.getX() - baseX, coord.getY() - baseY, coord.getZ() - baseZ, position);
	}
	
	protected LittleTileCoord(int relativeX, int relativeY, int relativeZ, LittleTileVec position)
	{
		this.coord = new BlockPos(relativeX, relativeY, relativeZ);
		this.position = position;
	}
	
	public LittleTileCoord(String id, NBTTagCompound nbt)
	{
		if(nbt.hasKey(id + "coord"))
		{
			int[] array = nbt.getIntArray(id + "coord");
			if(array.length == 3)
				coord = new BlockPos(array[0], array[1], array[2]);
			else
				throw new InvalidParameterException("No valid coord given " + nbt);
		}
		else if(nbt.hasKey(id + "coordX"))
			coord = new BlockPos(nbt.getInteger(id + "coordX"), nbt.getInteger(id + "coordY"), nbt.getInteger(id + "coordZ"));
		position = new LittleTileVec(id + "pos", nbt);
	}
	
	public LittleTileCoord(NBTTagCompound nbt)
	{
		this("", nbt);
	}
	
	public BlockPos getAbsolutePosition(TileEntity te)
	{
		return getAbsolutePosition(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
	}
	
	public BlockPos getAbsolutePosition(BlockPos origin)
	{
		return getAbsolutePosition(origin.getX(), origin.getY(), origin.getZ());
	}
	
	public BlockPos getAbsolutePosition(int x, int y, int z)
	{
		return new BlockPos(coord.getX()+x, coord.getY()+y, coord.getZ()+z);
	}
	
	public void writeToNBT(String id, NBTTagCompound nbt)
	{
		/*nbt.setInteger(id + "coordX", coord.getX());
		nbt.setInteger(id + "coordY", coord.getY());
		nbt.setInteger(id + "coordZ", coord.getZ());*/
		nbt.setIntArray(id + "coord", new int[]{coord.getX(), coord.getY(), coord.getZ()});
		position.writeToNBT(id + "pos", nbt);
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		writeToNBT("", nbt);
	}
	
	@Override
	public String toString()
	{
		return "coord:[" + coord.getX() + "," + coord.getY() + "," + coord.getZ() + "]|position:" + position;
	}
	
	public LittleTileCoord copy()
	{
		return new LittleTileCoord(coord.getX(), coord.getY(), coord.getZ(), position.copy());
	}
	
}
