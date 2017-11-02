package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.PlacementHelper;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
	
	public LittleTileVec(RayTraceResult result)
	{
		this(result.hitVec, result.sideHit);
	}
	
	public LittleTileVec(Vec3d vec, EnumFacing facing)
	{
		this(vec);
		if(facing.getAxisDirection() == AxisDirection.POSITIVE && !LittleUtils.isAtEdge(RotationUtils.get(facing.getAxis(), vec)))
			setAxis(facing.getAxis(), getAxis(facing.getAxis()) + 1);
	}
	
	public LittleTileVec(Vec3d vec)
	{		
		this.x = LittleUtils.toGrid(vec.x);
		this.y = LittleUtils.toGrid(vec.y);
		this.z = LittleUtils.toGrid(vec.z);
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
		vec.add(this);
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
	
	public void setAxis(Axis axis, int value)
	{
		switch(axis)
		{
		case X:
			x = value;
			break;
		case Y:
			y = value;
			break;
		case Z:
			z = value;
			break;
		}
		
	}
	
	public int getAxis(Axis axis)
	{
		switch(axis)
		{
		case X:
			return x;
		case Y:
			return y;
		case Z:
			return z;
		}
		return 0;
	}
	
	public void add(Vec3i vec)
	{
		x += LittleUtils.toGrid(vec.getX());
		y += LittleUtils.toGrid(vec.getY());
		z += LittleUtils.toGrid(vec.getZ());
	}
	
	public void add(EnumFacing facing)
	{
		setAxis(facing.getAxis(), getAxis(facing.getAxis()) + facing.getAxisDirection().getOffset());
	}
	
	public void add(LittleTileVec vec)
	{
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
	}
	
	public void sub(Vec3i vec)
	{
		x -= LittleUtils.toGrid(vec.getX());
		y -= LittleUtils.toGrid(vec.getY());
		z -= LittleUtils.toGrid(vec.getZ());
	}
	
	public void sub(EnumFacing facing)
	{
		setAxis(facing.getAxis(), getAxis(facing.getAxis()) - facing.getAxisDirection().getOffset());
	}
	
	public void sub(LittleTileVec vec)
	{
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
	}
	
	public void flip(Axis axis)
	{
		setAxis(axis, -getAxis(axis));
	}
	
	public void rotateVec(Rotation rotation)
	{
		int tempX = x;
		int tempY = y;
		int tempZ = z;
		this.x = rotation.getMatrix().getX(tempX, tempY, tempZ);
		this.y = rotation.getMatrix().getY(tempX, tempY, tempZ);
		this.z = rotation.getMatrix().getZ(tempX, tempY, tempZ);
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
