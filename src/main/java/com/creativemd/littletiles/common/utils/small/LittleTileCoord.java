package com.creativemd.littletiles.common.utils.small;

import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

public class LittleTileCoord {
	
	private ChunkCoordinates coord;
	public LittleTileVec position;
	
	public LittleTileCoord(TileEntity te, ChunkCoordinates coord, LittleTileVec position)
	{
		this(te.xCoord, te.yCoord, te.zCoord, coord, position);
	}
	
	public LittleTileCoord(ChunkCoordinates origin, ChunkCoordinates coord, LittleTileVec position)
	{
		this(origin.posX, origin.posY, origin.posZ, coord, position);
	}
	
	public LittleTileCoord(int baseX, int baseY, int baseZ, ChunkCoordinates coord, LittleTileVec position)
	{
		this(coord.posX - baseX, coord.posY - baseY, coord.posZ - baseZ, position);
	}
	
	protected LittleTileCoord(int relativeX, int relativeY, int relativeZ, LittleTileVec position)
	{
		this.coord = new ChunkCoordinates(relativeX, relativeY, relativeZ);
		this.position = position;
	}
	
	public LittleTileCoord(String id, NBTTagCompound nbt)
	{
		coord = new ChunkCoordinates(nbt.getInteger(id + "coordX"), nbt.getInteger(id + "coordY"), nbt.getInteger(id + "coordZ"));
		position = new LittleTileVec(id + "pos", nbt);
	}
	
	public LittleTileCoord(NBTTagCompound nbt)
	{
		this("", nbt);
	}
	
	public ChunkCoordinates getAbsolutePosition(TileEntity te)
	{
		return getAbsolutePosition(te.xCoord, te.yCoord, te.zCoord);
	}
	
	public ChunkCoordinates getAbsolutePosition(ChunkCoordinates origin)
	{
		return getAbsolutePosition(origin.posX, origin.posY, origin.posZ);
	}
	
	public ChunkCoordinates getAbsolutePosition(int x, int y, int z)
	{
		return new ChunkCoordinates(coord.posX+x, coord.posY+y, coord.posZ+z);
	}
	
	public void writeToNBT(String id, NBTTagCompound nbt)
	{
		nbt.setInteger(id + "coordX", coord.posX);
		nbt.setInteger(id + "coordY", coord.posY);
		nbt.setInteger(id + "coordZ", coord.posZ);
		position.writeToNBT(id + "pos", nbt);
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		writeToNBT("", nbt);
	}
	
	@Override
	public String toString()
	{
		return "coord:[" + coord.posX + "," + coord.posY + "," + coord.posZ + "]|position:" + position;
	}
	
	public LittleTileCoord copy()
	{
		return new LittleTileCoord(coord.posX, coord.posY, coord.posZ, position.copy());
	}
	
}
