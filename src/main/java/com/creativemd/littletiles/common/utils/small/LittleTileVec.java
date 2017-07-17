package com.creativemd.littletiles.common.utils.small;

import java.security.InvalidParameterException;

import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
		}else if(nbt.getTag(name + "x") instanceof NBTTagInt)
			set(nbt.getInteger(name+"x"), nbt.getInteger(name+"y"), nbt.getInteger(name+"z"));
		else if(nbt.getTag(name) instanceof NBTTagIntArray){
			int[] array = nbt.getIntArray(name);
			if(array.length == 3)
				set(array[0], array[1], array[2]);
			else
				throw new InvalidParameterException("No valid coords given " + nbt);
		}else if(nbt.getTag(name) instanceof NBTTagString){
			String[] coords = nbt.getString(name).split("\\.");
			try{
				set(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			}catch(Exception e){
				set(0, 0, 0);
			}
		}
	}
	
	public LittleTileVec(Vec3d vec)
	{
		double posX = PlacementHelper.round(vec.x*LittleTile.gridSize);
		double posY = PlacementHelper.round(vec.y*LittleTile.gridSize);
		double posZ = PlacementHelper.round(vec.z*LittleTile.gridSize);
		
		if(vec.x < 0)
			posX = Math.floor(posX);
		if(vec.y < 0)
			posY = Math.floor(posY);
		if(vec.z < 0)
			posZ = Math.floor(posZ);
		
		this.x = (int) posX;
		this.y = (int) posY;
		this.z = (int) posZ;
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
	
	public LittleTileVec getRelativeVec(BlockPos pos)
	{
		LittleTileVec vec = new LittleTileVec(pos);
		vec.invert();
		vec.addVec(this);
		return vec;
	}
	
	public BlockPos getBlockPos()
	{
		return new BlockPos((int) getPosX(), (int) getPosY(), (int) getPosZ());
	}
	
	public Vec3d getVec()
	{
		return new Vec3d(getPosX(), getPosY(), getPosZ());
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
		/*nbt.setInteger(name+"x", x);
		nbt.setInteger(name+"y", y);
		nbt.setInteger(name+"z", z);*/
		//nbt.setString(name, x+"."+y+"."+z);
		nbt.setIntArray(name, new int[]{x, y, z});
	}
	
	@Override
	public String toString()
	{
		return "[" + x + "," + y + "," + z + "]";
	}

	public void invert() {
		set(-x, -y, -z);
	}

	public void scale(int factor) {
		x *= factor;
		y *= factor;
		z *= factor;
	}
}
