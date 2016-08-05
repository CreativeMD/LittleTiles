package com.creativemd.littletiles.common.utils.small;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class LittleTileBox {
	
	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;
	
	public LittleTileBox(LittleTileVec center, LittleTileSize size)
	{
		LittleTileVec offset = size.calculateCenter();
		minX = (int) (center.x-offset.x);
		minY = (int) (center.y-offset.y);
		minZ = (int) (center.z-offset.z);
		maxX = (int) (minX+size.sizeX);
		maxY = (int) (minY+size.sizeY);
		maxZ = (int) (minZ+size.sizeZ);
	}
	
	public LittleTileBox(String name, NBTTagCompound nbt)
	{
		if(nbt.getTag(name + "minX") instanceof NBTTagByte)
		{
			set((byte) nbt.getByte(name+"minX"), (byte) nbt.getByte(name+"minY"), (byte) nbt.getByte(name+"minZ"), (byte) nbt.getByte(name+"maxX"), (byte) nbt.getByte(name+"maxY"), (byte) nbt.getByte(name+"maxZ"));
			writeToNBT(name, nbt);
		}
		else
			set(nbt.getInteger(name+"minX"), nbt.getInteger(name+"minY"), nbt.getInteger(name+"minZ"), nbt.getInteger(name+"maxX"), nbt.getInteger(name+"maxY"), nbt.getInteger(name+"maxZ"));
	}
	
	public LittleTileBox(CubeObject cube)
	{
		this((int)(cube.minX*16), (int)(cube.minY*16), (int)(cube.minZ*16), (int)(cube.maxX*16), (int)(cube.maxY*16), (int)(cube.maxZ*16));
	}
	
	public LittleTileBox(AxisAlignedBB box)
	{
		this((int)(box.minX*16), (int)(box.minY*16), (int)(box.minZ*16), (int)(box.maxX*16), (int)(box.maxY*16), (int)(box.maxZ*16));
	}
	
	public LittleTileBox(LittleTileVec min, LittleTileVec max)
	{
		this(min.x, min.y, min.z, max.x, max.y, max.z);
	}
	
	public LittleTileBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		set(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public AxisAlignedBB getBox()
	{
		return new AxisAlignedBB(minX/16D, minY/16D, minZ/16D, maxX/16D, maxY/16D, maxZ/16D);
	}
	
	public CubeObject getCube()
	{
		return new CubeObject(minX/16F, minY/16F, minZ/16F, maxX/16F, maxY/16F, maxZ/16F);
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		nbt.setInteger(name+"minX", minX);
		nbt.setInteger(name+"minY", minY);
		nbt.setInteger(name+"minZ", minZ);
		nbt.setInteger(name+"maxX", maxX);
		nbt.setInteger(name+"maxY", maxY);
		nbt.setInteger(name+"maxZ", maxZ);
	}
	
	public Vec3d getSizeD()
	{
		return new Vec3d((maxX - minX)*1/16D, (maxY - minY)*1/16D, (maxZ - minZ)*1/16D);
	}
	
	public LittleTileSize getSize()
	{
		return new LittleTileSize((int)(maxX - minX), (int)(maxY - minY), (int)(maxZ - minZ));
	}
	
	public LittleTileBox copy()
	{
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean isValidBox() {
		return maxX > minX && maxY > minY && maxZ > minZ;
	}
	
	public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public boolean needsMultipleBlocks() {
		int x = minX/16;
		int y = minY/16;
		int z = minZ/16;
		
		return maxX-x*16<=LittleTile.maxPos && maxY-y*16<=LittleTile.maxPos && maxZ-z*16<=LittleTile.maxPos;
	}
	
	public LittleTileBox combineBoxes(LittleTileBox box)
	{
		boolean x = this.minX == box.minX && this.maxX == box.maxX;
		boolean y = this.minY == box.minY && this.maxY == box.maxY;
		boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
		
		if(x && y && z)
		{
			return this;
		}
		if(x && y)
		{
			if(this.minZ == box.maxZ)
				return new LittleTileBox(minX, minY, box.minZ, maxX, maxY, maxZ);
			else if(this.maxZ == box.minZ)
				return new LittleTileBox(minX, minY, minZ, maxX, maxY, box.maxZ);
		}
		if(x && z)
		{
			if(this.minY == box.maxY)
				return new LittleTileBox(minX, box.minY, minZ, maxX, maxY, maxZ);
			else if(this.maxY == box.minY)
				return new LittleTileBox(minX, minY, minZ, maxX, box.maxY, maxZ);
		}
		if(y && z)
		{
			if(this.minX == box.maxX)
				return new LittleTileBox(box.minX, minY, minZ, maxX, maxY, maxZ);
			else if(this.maxX == box.minX)
				return new LittleTileBox(minX, minY, minZ, box.maxX, maxY, maxZ);
		}
		return null;
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
	
	public void subOffset(LittleTileVec vec)
	{
		minX -= vec.x;
		minY -= vec.y;
		minZ -= vec.z;
		maxX -= vec.x;
		maxY -= vec.y;
		maxZ -= vec.z;
	}
	
	public void assignCube(CubeObject cube)
	{
		this.minX = (int)(cube.minX*16);
		this.minY = (int)(cube.minY*16);
		this.minZ = (int)(cube.minZ*16);
		this.maxX = (int)(cube.maxX*16);
		this.maxY = (int)(cube.maxY*16);
		this.maxZ = (int)(cube.maxZ*16);
	}
	
	public LittleTileVec getMinVec()
	{
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public LittleTileVec getMaxVec()
	{
		return new LittleTileVec(maxX, maxY, maxZ);
	}
	
	/*public void rotateBoxby(EnumFacing direction)
	{
		switch(direction)
		{
		case SOUTH:
		case NORTH:
			set(minZ, minY, minX, maxZ, maxY, maxX);
			break;
		case UP:
			set(minX, minZ, minY, maxX, maxZ, maxY);
			break;
		case DOWN:
			set(minY, minX, minZ, maxY, maxX, maxZ);
			break;
		default:
			break;
		}
	}*/
	
	public void rotateBoxWithCenter(Rotation direction, Vec3d center)
	{
		CubeObject cube = this.getCube();
		cube = CubeObject.rotateCube(cube, direction, center);
		/*this.minX = (int) Math.round(cube.minX*16);
		this.minY = (int) Math.round(cube.minY*16);
		this.minZ = (int) Math.round(cube.minZ*16);
		this.maxX = (int) Math.round(cube.maxX*16);
		this.maxY = (int) Math.round(cube.maxY*16);
		this.maxZ = (int) Math.round(cube.maxZ*16);*/
		this.minX = (int) (cube.minX*16);
		this.minY = (int) (cube.minY*16);
		this.minZ = (int) (cube.minZ*16);
		this.maxX = (int) (cube.maxX*16);
		this.maxY = (int) (cube.maxY*16);
		this.maxZ = (int) (cube.maxZ*16);
		//assignCube(cube);
	}
	
	public void flipBox(EnumFacing direction)
	{
		switch(direction)
		{
		case EAST:
		case WEST:
			minX = -minX;
			maxX = -maxX;
			break;
		case UP:
		case DOWN:
			minY = -minY;
			maxY = -maxY;
			break;
		case SOUTH:
		case NORTH:
			minZ = -minZ;
			maxZ = -maxZ;
			break;
		default:
			break;
		}
		
		resort();
	}
	
	public void flipBoxWithCenter(EnumFacing direction, LittleTileVec center)
	{
		if(center == null)
			center = new LittleTileVec(8, 8, 8);
		subOffset(center);
		flipBox(direction);
		addOffset(center);		
	}
	
	public void rotateBox(EnumFacing direction)
	{
		CubeObject cube = this.getCube();
		/*int x = (int) cube.minX;
		cube.minX -= x;
		cube.maxX -= x;
		int y = (int) cube.minY;
		cube.minY -= y;
		cube.maxY -= y;
		int z = (int) cube.minZ;
		cube.minZ -= z;
		cube.maxZ -= z;*/
		cube = CubeObject.rotateCube(cube, direction);
		/*cube.minX += x;
		cube.maxX += x;
		cube.minY += y;
		cube.maxY += y;
		cube.minZ += z;
		cube.maxZ += z;*/
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
		int x = minX;
		if(vec.x >= minX || vec.x <= maxX)
			x = vec.x;
		if(Math.abs(minX-x) > Math.abs(maxX-x))
			x = maxX;
		
		int y = minY;
		if(vec.y >= minY || vec.y <= maxY)
			y = vec.y;
		if(Math.abs(minY-y) > Math.abs(maxY-y))
			y = maxY;
		
		int z = minZ;
		if(vec.z >= minZ || vec.z <= maxZ)
			z = vec.z;
		if(Math.abs(minZ-z) > Math.abs(maxZ-z))
			z = maxZ;
		
		return new LittleTileVec(x, y, z);
	}
	
	public LittleTileVec getNearstedPointTo(LittleTileBox box)
	{
		int x = 0;
		if(minX >= box.minX && minX <= box.maxX)
			x = minX;
		else if(box.minX >= minX && box.minX <= box.maxX)
			x = box.minX;
		else
			if(Math.abs(minX-box.maxX) > Math.abs(maxX - box.minX))
				x = maxX;
			else
				x = minX;
		
		int y = 0;
		if(minY >= box.minY && minY <= box.maxY)
			y = minY;
		else if(box.minY >= minY && box.minY <= box.maxY)
			y = box.minY;
		else
			if(Math.abs(minY-box.maxY) > Math.abs(maxY - box.minY))
				y = maxY;
			else
				y = minY;
		
		int z = 0;
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
	
	public boolean intersectsWith(LittleTileBox box)
    {
        return box.maxX > this.minX && box.minX < this.maxX ? (box.maxY > this.minY && box.minY < this.maxY ? box.maxZ > this.minZ && box.minZ < this.maxZ : false) : false;
    }
	
	public EnumFacing faceTo(LittleTileBox box) {
		
		boolean x = !(this.minX >= box.maxX || box.minX >= this.maxX);
		boolean y = !(this.minY >= box.maxY || box.minY >= this.maxY);
		boolean z = !(this.minZ >= box.maxZ || box.minZ >= this.maxZ);
		
		if(x && y && z)
			return EnumFacing.EAST;
		
		if(x && y)
			if(this.minZ > box.maxZ)
				return EnumFacing.NORTH;
			else
				return EnumFacing.SOUTH;
		
		if(x && z)
			if(this.minY > box.maxY)
				return EnumFacing.DOWN;
			else
				return EnumFacing.UP;
		
		if(y && z)
			if(this.minX > box.maxX)
				return EnumFacing.WEST;
			else
				return EnumFacing.EAST;
		
		return EnumFacing.EAST;
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
	
	public boolean isBoxInsideBlock()
	{
		return minX >= LittleTile.minPos && maxX <= LittleTile.maxPos && minY >= LittleTile.minPos && maxY <= LittleTile.maxPos && minZ >= LittleTile.minPos && maxZ <= LittleTile.maxPos;
	}
	
	public LittleTileBox expand(EnumFacing direction)
	{
		LittleTileBox result = this.copy();
		switch(direction)
		{
		
		case EAST:
			result.maxX++;
			break;
		case WEST:
			result.minX--;
			break;
		case UP:
			result.maxY++;
			break;
		case DOWN:
			result.minY--;
			break;
		case SOUTH:
			result.maxZ++;
			break;
		case NORTH:
			result.minZ--;
			break;
		default:
			break;
		}
		return result;
	}
	
	public LittleTileBox shrink(EnumFacing direction)
	{
		LittleTileBox result = this.copy();
		switch(direction)
		{
		
		case EAST:
			result.maxX--;
			break;
		case WEST:
			result.minX++;
			break;
		case UP:
			result.maxY--;
			break;
		case DOWN:
			result.minY++;
			break;
		case SOUTH:
			result.maxZ--;
			break;
		case NORTH:
			result.minZ++;
			break;
		default:
			break;
		}
		return result;
	}
	
	public void applyDirection(EnumFacing direction) {
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

	public void resort() {
		set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}

	public LittleTileBox getSideOfBox(EnumFacing facing) {
		LittleTileBox newBox = this.copy();
		switch(facing)
		{
		case EAST:
			newBox.minX = this.maxX;
			newBox.maxX++;
			break;
		case WEST:
			newBox.maxX = this.minX;
			newBox.minX--;
			break;
		case UP:
			newBox.minY = this.maxY;
			newBox.maxY++;
			break;
		case DOWN:
			newBox.maxY = this.minY;
			newBox.minY--;
			break;
		case SOUTH:
			newBox.minZ = this.maxZ;
			newBox.maxZ++;
			break;
		case NORTH:
			newBox.maxZ = this.minZ;
			newBox.minZ--;
			break;
		}
		return newBox;
	}
	
	
}
