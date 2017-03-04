package com.creativemd.littletiles.common.utils.small;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class LittleTileSize {
	
	public int sizeX;
	public int sizeY;
	public int sizeZ;
	
	public LittleTileSize(String name, NBTTagCompound nbt)
	{
		if(nbt.getTag(name+"x") instanceof NBTTagByte)
			set(nbt.getByte(name+"x"), nbt.getByte(name+"y"), nbt.getByte(name+"z"));
		else if(nbt.getTag(name + "x") instanceof NBTTagInt)
			set(nbt.getInteger(name+"x"), nbt.getInteger(name+"y"), nbt.getInteger(name+"z"));
		else{
			String[] coords = nbt.getString(name).split("\\.");
			try{
				set(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			}catch(Exception e){
				set(0, 0, 0);
			}
		}
	}
	
	public LittleTileSize(String data)
	{
		String[] coords = data.split("\\.");
		try{
			set(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
		}catch(Exception e){
			set(0, 0, 0);
		}
	}
	
	public LittleTileSize(int sizeX, int sizeY, int sizeZ)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}
	
	/*public LittleTileSize(int sizeX, int sizeY, int sizeZ)
	{
		this((byte)sizeX, (byte)sizeY, (byte)sizeZ);
	}*/
	
	public void set(int sizeX, int sizeY, int sizeZ)
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
	
	public int getVolume()
	{
		return sizeX * sizeY * sizeZ;
	}
	
	/**Returns how the volume in percent to a size of a normal block*/
	public double getPercentVolume()
	{
		return (double) getVolume() / (double) (LittleTile.maxTilesPerBlock);
	}
	
	public LittleTileVec calculateInvertedCenter()
	{
		double x = sizeX/2D;
		double y = sizeY/2D;
		double z = sizeZ/2D;
		return new LittleTileVec((int)Math.ceil(x), (int)Math.ceil(y), (int)Math.ceil(z));
	}
	
	
	public LittleTileVec calculateCenter()
	{
		double x = sizeX/2D;
		double y = sizeY/2D;
		double z = sizeZ/2D;
		return new LittleTileVec((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
	}
	
	public double getPosX()
	{
		return (double)sizeX/LittleTile.gridSize;
	}
	
	public double getPosY()
	{
		return (double)sizeY/LittleTile.gridSize;
	}
	
	public double getPosZ()
	{
		return (double)sizeZ/LittleTile.gridSize;
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
	
	public void rotateSize(Rotation direction)
	{
		switch(direction)
		{
		case UP:
		case DOWN:
			int tempY = sizeY;
			sizeY = sizeX;
			sizeX = tempY;
			break;
		case UPX:
		case DOWNX:
			tempY = sizeY;
			sizeY = sizeZ;
			sizeZ = tempY;
			break;
		case SOUTH:
		case NORTH:
			int tempZ = sizeZ;
			sizeZ = sizeX;
			sizeX = tempZ;
			break;
		default:
			break;
		}
	}
	
	public void rotateSize(EnumFacing direction)
	{
		switch(direction)
		{
		case UP:
		case DOWN:
			int tempY = sizeY;
			sizeY = sizeX;
			sizeX = tempY;
			break;
		case SOUTH:
		case NORTH:
			int tempZ = sizeZ;
			sizeZ = sizeX;
			sizeX = tempZ;
			break;
		default:
			break;
		}
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		/*nbt.setInteger(name+"x", sizeX);
		nbt.setInteger(name+"y", sizeY);
		nbt.setInteger(name+"z", sizeZ);*/
		nbt.setString(name, sizeX+"."+sizeY+"."+sizeZ);
		
	}
	
	@Override
	public String toString()
	{
		return sizeX+"."+sizeY+"."+sizeZ;
	}

	public LittleTileSize max(LittleTileSize size) {
		this.sizeX = Math.max(this.sizeX, size.sizeX);
		this.sizeY = Math.max(this.sizeY, size.sizeY);
		this.sizeZ = Math.max(this.sizeZ, size.sizeZ);
		return this;
	}

	public int getSizeOfAxis(Axis axis) {
		switch(axis)
		{
		case X:
			return sizeX;
		case Y:
			return sizeY;
		case Z:
			return sizeZ;
		default:
			return 0;
		}
	}
	
}
