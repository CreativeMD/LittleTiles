package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;
import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleTileRelativeCoord {
	
	protected BlockPos coord;
	public int[] identifier;
	
	public LittleTileRelativeCoord(TileEntity te, BlockPos coord, int[] identifier)
	{
		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), coord, identifier);
	}
	
	public LittleTileRelativeCoord(BlockPos origin, BlockPos coord, int[] identifier)
	{
		this(origin.getX(), origin.getY(), origin.getZ(), coord, identifier);
	}
	
	public LittleTileRelativeCoord(int baseX, int baseY, int baseZ, BlockPos coord, int[] identifier)
	{
		this(coord.getX() - baseX, coord.getY() - baseY, coord.getZ() - baseZ, identifier);
	}
	
	protected LittleTileRelativeCoord(int relativeX, int relativeY, int relativeZ, int[] identifier)
	{
		this.coord = new BlockPos(relativeX, relativeY, relativeZ);
		this.identifier = identifier;
	}
	
	public LittleTileRelativeCoord(String id, NBTTagCompound nbt)
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
		if(nbt.hasKey(id + "pos"))
		{
			LittleTileVec position = new LittleTileVec(id + "pos", nbt);
			identifier = new int[]{position.x, position.y, position.z};
		}else
			identifier = nbt.getIntArray("id");
	}
	
	public LittleTileRelativeCoord(NBTTagCompound nbt)
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
		//position.writeToNBT(id + "pos", nbt);
		nbt.setIntArray("id", identifier);
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		writeToNBT("", nbt);
	}
	
	@Override
	public String toString()
	{
		return "coord:[" + coord.getX() + "," + coord.getY() + "," + coord.getZ() + "]|position:" + Arrays.toString(identifier);
	}
	
	public LittleTileRelativeCoord copy()
	{
		return new LittleTileRelativeCoord(coord.getX(), coord.getY(), coord.getZ(), identifier.clone());
	}
	
}
