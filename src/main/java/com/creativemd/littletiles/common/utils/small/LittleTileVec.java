package com.creativemd.littletiles.common.utils.small;

import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import scala.tools.nsc.transform.patmat.Solving.Solver.Lit;

public class LittleTileVec {
	
	public int x;
	public int y;
	public int z;
	
	public LittleTileVec(String name, NBTTagCompound nbt)
	{
		if(nbt.getTag(name + "x") instanceof NBTTagByte)
		{
			set(nbt.getByte(name+"x"), nbt.getByte(name+"y"), nbt.getByte(name+"z"));
			writeToNBT(name, nbt);
		}else
			set(nbt.getInteger(name+"x"), nbt.getInteger(name+"y"), nbt.getInteger(name+"z"));
	}
	
	public LittleTileVec(Vec3d vec)
	{
		this((int) (vec.xCoord*LittleTile.gridSize), (int) (vec.yCoord*LittleTile.gridSize), (int) (vec.zCoord*LittleTile.gridSize));
	}
	
	public LittleTileVec(EnumFacing facing)
	{
		switch(facing)
		{
		case EAST:
			set(1,0,0);
			break;
		case WEST:
			set(-1,0,0);
			break;
		case UP:
			set(0,1,0);
			break;
		case DOWN:
			set(0,-1,0);
			break;
		case SOUTH:
			set(0,0,1);
			break;
		case NORTH:
			set(0,0,-1);
			break;
		default:
			set(0,0,0);
			break;
		}
	}
	
	public LittleTileVec(int x, int y, int z)
	{
		set(x, y, z);
	}
	
	public LittleTileVec(Vec3i vec)
	{
		this(vec.getX()*LittleTile.gridSize, vec.getY()*LittleTile.gridSize, vec.getZ()*LittleTile.gridSize);
	}
	
	public void set(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getPosX()
	{
		return (double)x/LittleTile.gridSize;
	}
	
	public double getPosY()
	{
		return (double)y/LittleTile.gridSize;
	}
	
	public double getPosZ()
	{
		return (double)z/LittleTile.gridSize;
	}
	
	public void addVec(LittleTileVec vec)
	{
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
	}
	
	public void subVec(LittleTileVec vec)
	{
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
	}
	
	public void rotateVec(EnumFacing direction)
	{
		switch(direction)
		{
		case UP:
		case DOWN:
			int tempY = y;
			y = x;
			x = tempY;
			break;
		case SOUTH:
		case NORTH:
			int tempZ = z;
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
		nbt.setInteger(name+"x", x);
		nbt.setInteger(name+"y", y);
		nbt.setInteger(name+"z", z);
	}
	
	@Override
	public String toString()
	{
		return "[" + x + "," + y + "," + z + "]";
	}

	public void invert() {
		set(-x, -y, -z);
	}
}
