package com.creativemd.littletiles.common.utils.small;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

public class LittleTileVec {
	
	public byte x;
	public byte y;
	public byte z;
	
	public LittleTileVec(String name, NBTTagCompound nbt)
	{
		this(nbt.getByte(name+"x"), nbt.getByte(name+"y"), nbt.getByte(name+"z"));
	}
	
	public LittleTileVec(Vec3 vec)
	{
		this((byte) vec.xCoord, (byte) vec.yCoord, (byte) vec.zCoord);
	}
	
	public LittleTileVec(int x, int y, int z)
	{
		this((byte)x, (byte)y, (byte)z);
	}
	
	public LittleTileVec(byte x, byte y, byte z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getPosX()
	{
		return (double)x/16D;
	}
	
	public double getPosY()
	{
		return (double)y/16D;
	}
	
	public double getPosZ()
	{
		return (double)z/16D;
	}
	
	public void addVec(LittleTileVec vec)
	{
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
	}
	
	public void rotateVec(ForgeDirection direction)
	{
		switch(direction)
		{
		case UP:
		case DOWN:
			byte tempY = y;
			y = x;
			x = tempY;
			break;
		case SOUTH:
		case NORTH:
			byte tempZ = z;
			z = x;
			x = tempZ;
			break;
		default:
			break;
		}
	}
	
	public double distanceTo(LittleTileVec vec)
	{
		return Math.sqrt(Math.pow(vec.x-this.x, 2)+Math.pow(vec.y-this.y, 2)+Math.pow(vec.z-this.z, 2));
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object instanceof LittleTileVec)
			return x == ((LittleTileVec) object).x && y == ((LittleTileVec) object).y && z == ((LittleTileVec) object).z;
		return super.equals(object);
	}
	
	public LittleTileVec copy()
	{
		return new LittleTileVec(x, y, z);
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		nbt.setByte(name+"x", x);
		nbt.setByte(name+"y", y);
		nbt.setByte(name+"z", z);
	}
	
	@Override
	public String toString()
	{
		return "[" + x + "," + y + "," + z + "]";
	}
}
