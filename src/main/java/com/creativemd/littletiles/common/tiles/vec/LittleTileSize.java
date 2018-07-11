package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagString;
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
		else if(nbt.getTag(name) instanceof NBTTagIntArray){
			int[] array = nbt.getIntArray(name);
			if(array.length == 3)
				set(array[0], array[1], array[2]);
			else
				throw new InvalidParameterException("No valid coords given " + nbt);
		}
		else if(nbt.getTag(name) instanceof NBTTagString){
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
	
	/**@return the volume in percent to a size of a normal block*/
	public double getPercentVolume(LittleGridContext context)
	{
		return (double) getVolume() / (double) (context.maxTilesPerBlock);
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
	
	public double getPosX(LittleGridContext context)
	{
		return context.toVanillaGrid(sizeX);
	}
	
	public double getPosY(LittleGridContext context)
	{
		return context.toVanillaGrid(sizeY);
	}
	
	public double getPosZ(LittleGridContext context)
	{
		return context.toVanillaGrid(sizeZ);
	}
	
	public int get(Axis axis)
	{
		switch(axis)
		{
		case X:
			return sizeX;
		case Y:
			return sizeY;
		case Z:
			return sizeZ;
		}
		return 0;
	}
	
	public LittleTileSize copy()
	{
		return new LittleTileSize(sizeX, sizeY, sizeZ);
	}
	
	public void rotateSize(Rotation rotation)
	{
		int tempX = sizeX;
		int tempY = sizeY;
		int tempZ = sizeZ;
		this.sizeX = rotation.getMatrix().getX(tempX, tempY, tempZ);
		this.sizeY = rotation.getMatrix().getY(tempX, tempY, tempZ);
		this.sizeZ = rotation.getMatrix().getZ(tempX, tempY, tempZ);
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		nbt.setIntArray(name, new int[]{sizeX, sizeY, sizeZ});
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
