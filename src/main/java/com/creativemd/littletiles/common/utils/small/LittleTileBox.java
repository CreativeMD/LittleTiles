package com.creativemd.littletiles.common.utils.small;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class LittleTileBox {
	
	public byte minX;
	public byte minY;
	public byte minZ;
	public byte maxX;
	public byte maxY;
	public byte maxZ;
	
	public LittleTileBox(LittleTileVec center, LittleTileSize size)
	{
		LittleTileVec offset = size.calculateCenter();
		minX = (byte) (center.x-offset.x);
		minY = (byte) (center.y-offset.y);
		minZ = (byte) (center.z-offset.z);
		maxX = (byte) (minX+size.sizeX);
		maxY = (byte) (minY+size.sizeY);
		maxZ = (byte) (minZ+size.sizeZ);
	}
	
	public LittleTileBox(String name, NBTTagCompound nbt)
	{
		this(nbt.getByte(name+"minX"), nbt.getByte(name+"minY"), nbt.getByte(name+"minZ"), nbt.getByte(name+"maxX"), nbt.getByte(name+"maxY"), nbt.getByte(name+"maxZ"));
	}
	
	public LittleTileBox(CubeObject cube)
	{
		this((byte)(cube.minX*16), (byte)(cube.minY*16), (byte)(cube.minZ*16), (byte)(cube.maxX*16), (byte)(cube.maxY*16), (byte)(cube.maxZ*16));
	}
	
	public LittleTileBox(AxisAlignedBB box)
	{
		this((byte)(box.minX*16), (byte)(box.minY*16), (byte)(box.minZ*16), (byte)(box.maxX*16), (byte)(box.maxY*16), (byte)(box.maxZ*16));
	}
	
	public LittleTileBox(LittleTileVec min, LittleTileVec max)
	{
		this(min.x, min.y, min.z, max.x, max.y, max.z);
	}
	
	public LittleTileBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		this((byte)minX, (byte)minY, (byte)minZ, (byte)maxX, (byte)maxY, (byte)maxZ);
	}
	
	public LittleTileBox(byte minX, byte minY, byte minZ, byte maxX, byte maxY, byte maxZ)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public AxisAlignedBB getBox()
	{
		return AxisAlignedBB.getBoundingBox(minX/16D, minY/16D, minZ/16D, maxX/16D, maxY/16D, maxZ/16D);
	}
	
	public CubeObject getCube()
	{
		return new CubeObject(minX/16D, minY/16D, minZ/16D, maxX/16D, maxY/16D, maxZ/16D);
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		nbt.setByte(name+"minX", minX);
		nbt.setByte(name+"minY", minY);
		nbt.setByte(name+"minZ", minZ);
		nbt.setByte(name+"maxX", maxX);
		nbt.setByte(name+"maxY", maxY);
		nbt.setByte(name+"maxZ", maxZ);
	}
	
	public LittleTileSize getSize()
	{
		return new LittleTileSize((byte)(maxX - minX), (byte)(maxY - minY), (byte)(maxZ - minZ));
	}
	
	public LittleTileBox copy()
	{
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean isValidBox() {
		return maxX > minX && maxY > minY && maxZ > minZ;
	}

	public boolean needsMultipleBlocks() {
		int x = minX/16;
		int y = minY/16;
		int z = minZ/16;
		
		return maxX-x*16<=LittleTile.maxPos && maxY-y*16<=LittleTile.maxPos && maxZ-z*16<=LittleTile.maxPos;
	}
	
	public void addOffset(LittleTileVec vec)
	{
		minX += vec.x;
		minY += vec.y;
		minZ += vec.z;
		maxX += vec.x;
		maxY += vec.y;
		maxZ += vec.z;
	}
	
	public void assignCube(CubeObject cube)
	{
		this.minX = (byte)(cube.minX*16);
		this.minY = (byte)(cube.minY*16);
		this.minZ = (byte)(cube.minZ*16);
		this.maxX = (byte)(cube.maxX*16);
		this.maxY = (byte)(cube.maxY*16);
		this.maxZ = (byte)(cube.maxZ*16);
	}
	
	public LittleTileVec getMinVec()
	{
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public LittleTileVec getMaxVec()
	{
		return new LittleTileVec(maxX, maxY, maxZ);
	}
	
	public void rotateBox(ForgeDirection direction)
	{
		CubeObject cube = this.getCube();
		CubeObject.rotateCube(cube, direction);
		assignCube(cube);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object instanceof LittleTileBox)
			return minX == ((LittleTileBox) object).minX && minY == ((LittleTileBox) object).minY && minZ == ((LittleTileBox) object).minZ && maxX == ((LittleTileBox) object).maxX && maxY == ((LittleTileBox) object).maxY && maxZ == ((LittleTileBox) object).maxZ;
		return super.equals(object);
	}
	
	@Override
	public String toString()
	{
		return "[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "]";
	}
	
	public LittleTileVec getNearstedPointTo(LittleTileVec vec)
	{
		byte x = minX;
		if(vec.x >= minX || vec.x <= maxX)
			x = vec.x;
		if(Math.abs(minX-x) > Math.abs(maxX-x))
			x = maxX;
		
		byte y = minY;
		if(vec.y >= minY || vec.y <= maxY)
			y = vec.y;
		if(Math.abs(minY-y) > Math.abs(maxY-y))
			y = maxY;
		
		byte z = minZ;
		if(vec.z >= minZ || vec.z <= maxZ)
			z = vec.z;
		if(Math.abs(minZ-z) > Math.abs(maxZ-z))
			z = maxZ;
		
		return new LittleTileVec(x, y, z);
	}
	
	public LittleTileVec getNearstedPointTo(LittleTileBox box)
	{
		byte x = 0;
		if(minX >= box.minX && minX <= box.maxX)
			x = minX;
		else if(box.minX >= minX && box.minX <= box.maxX)
			x = box.minX;
		else
			if(Math.abs(minX-box.maxX) > Math.abs(maxX - box.minX))
				x = maxX;
			else
				x = minX;
		
		byte y = 0;
		if(minY >= box.minY && minY <= box.maxY)
			y = minY;
		else if(box.minY >= minY && box.minY <= box.maxY)
			y = box.minY;
		else
			if(Math.abs(minY-box.maxY) > Math.abs(maxY - box.minY))
				y = maxY;
			else
				y = minY;
		
		byte z = 0;
		if(minZ >= box.minZ && minZ <= box.maxZ)
			z = minZ;
		else if(box.minZ >= minZ && box.minZ <= box.maxZ)
			z = box.minZ;
		else
			if(Math.abs(minZ-box.maxZ) > Math.abs(maxZ - box.minZ))
				z = maxZ;
			else
				z = minZ;
		
		return new LittleTileVec(x, y, z);
	}
	
	public double distanceTo(LittleTileBox box)
	{
		return distanceTo(box.getNearstedPointTo(this));
	}
	
	public double distanceTo(LittleTileVec vec)
	{
		return this.getNearstedPointTo(vec).distanceTo(vec);
	}
	
	public boolean isBoxInside(LittleTileBox box) {
		if(this.minX > box.maxX || box.minX > this.minX)
			return false;
		
		if(this.minY > box.maxY || box.minY > this.minY)
			return false;
		
		if(this.minZ > box.maxZ || box.minZ > this.minZ)
			return false;
		
		return true;
	}
	
	public ForgeDirection faceTo(LittleTileBox box) {
		
		boolean x = !(this.minX >= box.maxX || box.minX >= this.maxX);
		boolean y = !(this.minY >= box.maxY || box.minY >= this.maxY);
		boolean z = !(this.minZ >= box.maxZ || box.minZ >= this.maxZ);
		
		if(x && y && z)
			return ForgeDirection.UNKNOWN;
		
		if(x && y)
			if(this.minZ > box.maxZ)
				return ForgeDirection.NORTH;
			else
				return ForgeDirection.SOUTH;
		
		if(x && z)
			if(this.minY > box.maxY)
				return ForgeDirection.DOWN;
			else
				return ForgeDirection.UP;
		
		if(y && z)
			if(this.minX > box.maxX)
				return ForgeDirection.WEST;
			else
				return ForgeDirection.EAST;
		
		return ForgeDirection.UNKNOWN;
	}

	public boolean hasTwoSideIntersection(LittleTileBox box) {
		boolean x = !(this.minX > box.maxX || box.minX > this.minX);
		boolean y = !(this.minY > box.maxY || box.minY > this.minY);
		boolean z = !(this.minZ > box.maxZ || box.minZ > this.minZ);
		if(x && y && z)
			return false;
		return x && y || x && z || y && z;
	}
	
	/**:D**/
	public boolean isParallel(LittleTileBox box) {
		return true;
	}

	public void applyDirection(ForgeDirection direction) {
		switch(direction)
		{
		
		case EAST:
			minX += 16;
			maxX += 16;
			break;
		case WEST:
			minX -= 16;
			maxX -= 16;
			break;
		case UP:
			minY += 16;
			maxY += 16;
			break;
		case DOWN:
			minY -= 16;
			maxY -= 16;
			break;
		case SOUTH:
			minZ += 16;
			maxZ += 16;
			break;
		case NORTH:
			minZ -= 16;
			maxZ -= 16;
			break;
		default:
			break;
		}
	}
	
}
