package com.creativemd.littletiles.common.utils.small;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class LittleTileSize {
	
	public byte sizeX;
	public byte sizeY;
	public byte sizeZ;
	
	public LittleTileSize(String name, NBTTagCompound nbt)
	{
		this(nbt.getByte(name+"x"), nbt.getByte(name+"y"), nbt.getByte(name+"z"));
	}
	
	public LittleTileSize(byte sizeX, byte sizeY, byte sizeZ)
	{
		/*if(sizeX < 1)
			sizeX = 1;
		if(sizeX > 16)
			sizeX = 16;*/
		this.sizeX = sizeX;
		/*if(sizeY < 1)
			sizeY = 1;
		if(sizeY > 16)
			sizeY = 16;*/
		this.sizeY = sizeY;
		/*if(sizeZ < 1)
			sizeZ = 1;
		if(sizeZ > 16)
			sizeZ = 16;*/
		this.sizeZ = sizeZ;
	}
	
	public LittleTileSize(int sizeX, int sizeY, int sizeZ)
	{
		this((byte)sizeX, (byte)sizeY, (byte)sizeZ);
	}
	
	public void set(byte sizeX, byte sizeY, byte sizeZ)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object instanceof LittleTileSize)
			return sizeX == ((LittleTileSize) object).sizeX && sizeY == ((LittleTileSize) object).sizeY && sizeZ == ((LittleTileSize) object).sizeZ;
		return super.equals(object);
	}
	
	public float getVolume()
	{
		return sizeX * sizeY * sizeZ;
	}
	
	/**Returns how the volume in percent to a size of a normal block*/
	public float getPercentVolume()
	{
		return getVolume() / (16*16*16);
	}
	
	public LittleTileVec calculateInvertedCenter()
	{
		double x = sizeX/2D;
		double y = sizeY/2D;
		double z = sizeZ/2D;
		return new LittleTileVec((byte)(Math.ceil(x)), (byte)(Math.ceil(y)), (byte)(Math.ceil(z)));
	}
	
	
	public LittleTileVec calculateCenter()
	{
		double x = sizeX/2D;
		double y = sizeY/2D;
		double z = sizeZ/2D;
		return new LittleTileVec((byte)(Math.floor(x)), (byte)(Math.floor(y)), (byte)(Math.floor(z)));
	}
	
	public double getPosX()
	{
		return (double)sizeX/16D;
	}
	
	public double getPosY()
	{
		return (double)sizeY/16D;
	}
	
	public double getPosZ()
	{
		return (double)sizeZ/16D;
	}
	
	public LittleTileSize copy()
	{
		return new LittleTileSize(sizeX, sizeY, sizeZ);
	}
	
	/*public void rotateby(ForgeDirection direction)
	{
		switch(direction)
		{
		//case EAST:
		//case WEST:
		case SOUTH:
		case NORTH:
			set(sizeZ, sizeY, sizeX);
			break;
		case UP:
			set(sizeX, sizeZ, sizeY);
			break;
		case DOWN:
			set(sizeY, sizeX, sizeZ);
			break;
		default:
			break;
		}
	}*/
	
	public void rotateSize(EnumFacing direction)
	{
		switch(direction)
		{
		case UP:
		case DOWN:
			byte tempY = sizeY;
			sizeY = sizeX;
			sizeX = tempY;
			break;
		case SOUTH:
		case NORTH:
			byte tempZ = sizeZ;
			sizeZ = sizeX;
			sizeX = tempZ;
			break;
		default:
			break;
		}
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		nbt.setByte(name+"x", sizeX);
		nbt.setByte(name+"y", sizeY);
		nbt.setByte(name+"z", sizeZ);
	}
	
	@Override
	public String toString()
	{
		return "[" + sizeX + "," + sizeY + "," + sizeZ + "]";
	}

	public LittleTileSize max(LittleTileSize size) {
		this.sizeX = (byte) Math.max(this.sizeX, size.sizeX);
		this.sizeY = (byte) Math.max(this.sizeY, size.sizeY);
		this.sizeZ = (byte) Math.max(this.sizeZ, size.sizeZ);
		return this;
	}
	
}
