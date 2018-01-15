package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.client.tiles.LittleCorner;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.combine.BasicCombiner;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedBox;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;
import com.google.common.annotations.VisibleForTesting;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.collection.generic.BitOperations.Int;

public class LittleTileBox {
	
	//================Data================
	
	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;
	
	//================Constructors================
	
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
	
	public LittleTileBox(CubeObject cube)
	{
		this((int)Math.ceil(cube.minX*LittleTile.gridSize), (int)Math.ceil(cube.minY*LittleTile.gridSize), (int)Math.ceil(cube.minZ*LittleTile.gridSize), (int)Math.ceil(cube.maxX*LittleTile.gridSize), (int)Math.ceil(cube.maxY*LittleTile.gridSize), (int)Math.ceil(cube.maxZ*LittleTile.gridSize));
	}
	
	public LittleTileBox(AxisAlignedBB box)
	{
		this((int)(box.minX*LittleTile.gridSize), (int)(box.minY*LittleTile.gridSize), (int)(box.minZ*LittleTile.gridSize), (int)(box.maxX*LittleTile.gridSize), (int)(box.maxY*LittleTile.gridSize), (int)(box.maxZ*LittleTile.gridSize));
	}
	
	public LittleTileBox(LittleTileBox... boxes)
	{
		this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		for (int i = 0; i < boxes.length; i++) {
			this.minX = Math.min(boxes[i].minX, this.minX);
			this.minY = Math.min(boxes[i].minY, this.minY);
			this.minZ = Math.min(boxes[i].minZ, this.minZ);
			this.maxX = Math.max(boxes[i].maxX, this.maxX);
			this.maxY = Math.max(boxes[i].maxY, this.maxY);
			this.maxZ = Math.max(boxes[i].maxZ, this.maxZ);
		}
	}
	
	public LittleTileBox(LittleTileVec min, LittleTileVec max)
	{
		this(min.x, min.y, min.z, max.x, max.y, max.z);
	}
	
	public LittleTileBox(LittleTileVec min)
	{
		this(min.x, min.y, min.z, min.x+1, min.y+1, min.z+1);
	}
	
	public LittleTileBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		set(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	//================Conversions================
	
	public void addCollisionBoxes(AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, BlockPos offset)
	{
		AxisAlignedBB axisalignedbb = getBox(offset);

        if (entityBox.intersectsWith(axisalignedbb))
        {
            collidingBoxes.add(axisalignedbb);
        }
	}
	
	public AxisAlignedBB getSelectionBox(BlockPos pos)
	{
		return getBox(pos);
	}
	
	public AxisAlignedBB getBox(BlockPos offset)
	{
		return new AxisAlignedBB(minX/(double)LittleTile.gridSize + offset.getX(), minY/(double)LittleTile.gridSize + offset.getY(), minZ/(double)LittleTile.gridSize + offset.getZ(),
				maxX/(double)LittleTile.gridSize + offset.getX(), maxY/(double)LittleTile.gridSize + offset.getY(), maxZ/(double)LittleTile.gridSize + offset.getZ());
	}
	
	public AxisAlignedBB getBox()
	{
		return new AxisAlignedBB(minX/(double)LittleTile.gridSize, minY/(double)LittleTile.gridSize, minZ/(double)LittleTile.gridSize, maxX/(double)LittleTile.gridSize, maxY/(double)LittleTile.gridSize, maxZ/(double)LittleTile.gridSize);
	}
	
	public CubeObject getCube()
	{
		return new CubeObject(minX/(float)LittleTile.gridSize, minY/(float)LittleTile.gridSize, minZ/(float)LittleTile.gridSize, maxX/(float)LittleTile.gridSize, maxY/(float)LittleTile.gridSize, maxZ/(float)LittleTile.gridSize);
	}
	
	//================Save================
	
	public int[] getArray()
	{
		return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
	}
	
	public NBTTagIntArray getNBTIntArray()
	{
		return new NBTTagIntArray(getArray());
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		nbt.setIntArray(name, getArray());
	}
	
	//================Size & Volume================
	
	public boolean isCompletelyFilled()
	{
		return true;
	}
	
	public Vec3d getSizeVec()
	{
		return new Vec3d((maxX - minX)*LittleTile.gridMCLength, (maxY - minY)*LittleTile.gridMCLength, (maxZ - minZ)*LittleTile.gridMCLength);
	}
	
	public LittleTileSize getSize()
	{
		return new LittleTileSize((int)(maxX - minX), (int)(maxY - minY), (int)(maxZ - minZ));
	}
	
	public double getVolume()
	{
		return (int)(maxX - minX) * (int)(maxY - minY) * (int)(maxZ - minZ);
	}
	
	/**@return the volume in percent to a size of a normal block*/
	public double getPercentVolume()
	{
		return (double) getVolume() / (double) (LittleTile.maxTilesPerBlock);
	}
	
	public int getValueOfFacing(EnumFacing facing)
	{
		switch(facing)
		{
		case EAST:
			return maxX;
		case WEST:
			return minX;
		case UP:
			return maxY;
		case DOWN:
			return minY;
		case SOUTH:
			return maxZ;
		case NORTH:
			return minZ;
		
		}
		return 0;
	}
	
	public LittleTileVec getCorner(LittleCorner corner)
	{
		return new LittleTileVec(getCornerX(corner), getCornerY(corner), getCornerZ(corner));
	}
	
	public Vec3d getExactCorner(LittleCorner corner)
	{
		return new Vec3d(getCornerX(corner), getCornerY(corner), getCornerZ(corner));
	}
	
	public int getCornerValue(LittleCorner corner, Axis axis)
	{
		return getValueOfFacing(corner.getFacing(axis));
	}
	
	public int getCornerX(LittleCorner corner)
	{
		return getValueOfFacing(corner.x);
	}
	
	public int getCornerY(LittleCorner corner)
	{
		return getValueOfFacing(corner.y);
	}
	
	public int getCornerZ(LittleCorner corner)
	{
		return getValueOfFacing(corner.z);
	}
	
	public int getSize(Axis axis)
	{
		switch (axis)
		{
		case X:
			return maxX - minX;
		case Y:
			return maxY - minY;
		case Z:
			return maxZ - minZ;
		}
		return 0;
	}
	
	public void setMin(Axis axis, int value)
	{
		switch (axis)
		{
		case X:
			minX = value;
			break;
		case Y:
			minY = value;
			break;
		case Z:
			minZ = value;
			break;
		}
	}
	
	public int getMin(Axis axis)
	{
		switch (axis)
		{
		case X:
			return minX;
		case Y:
			return minY;
		case Z:
			return minZ;
		}
		return 0;
	}
	
	public void setMax(Axis axis, int value)
	{
		switch (axis)
		{
		case X:
			maxX = value;
			break;
		case Y:
			maxY = value;
			break;
		case Z:
			maxZ = value;
			break;
		}
	}
	
	public int getMax(Axis axis)
	{
		switch (axis)
		{
		case X:
			return maxX;
		case Y:
			return maxY;
		case Z:
			return maxZ;
		}
		return 0;
	}
	
	//================Block Integration================
	
	public boolean isValidBox()
	{
		return maxX > minX && maxY > minY && maxZ > minZ;
	}
	
	public boolean needsMultipleBlocks()
	{
		int x = minX/LittleTile.gridSize;
		int y = minY/LittleTile.gridSize;
		int z = minZ/LittleTile.gridSize;
		
		return maxX-x*LittleTile.gridSize<=LittleTile.maxPos && maxY-y*LittleTile.gridSize<=LittleTile.maxPos && maxZ-z*LittleTile.gridSize<=LittleTile.maxPos;
	}
	
	public boolean isBoxInsideBlock()
	{
		return minX >= LittleTile.minPos && maxX <= LittleTile.maxPos && minY >= LittleTile.minPos && maxY <= LittleTile.maxPos && minZ >= LittleTile.minPos && maxZ <= LittleTile.maxPos;
	}
	
	public void split(HashMapList<BlockPos, LittleTileBox> boxes)
	{
		LittleTileSize size = getSize();
		
		int minOffX = LittleUtils.toBlockOffset(minX);
		int minOffY = LittleUtils.toBlockOffset(minY);
		int minOffZ = LittleUtils.toBlockOffset(minZ);
		
		int maxOffX = LittleUtils.toBlockOffset(maxX);
		int maxOffY = LittleUtils.toBlockOffset(maxY);
		int maxOffZ = LittleUtils.toBlockOffset(maxZ);
		
		List<LittleTileBox> tempBoxes = new ArrayList<>();
		
		for (int x = minOffX; x <= maxOffX; x++) {
			for (int y = minOffY; y <= maxOffY; y++) {
				for (int z = minOffZ; z <= maxOffZ; z++) {
					int minX = Math.max(this.minX, x*LittleTile.gridSize);
					int minY = Math.max(this.minY, y*LittleTile.gridSize);
					int minZ = Math.max(this.minZ, z*LittleTile.gridSize);
					int maxX = Math.min(this.maxX, x*LittleTile.gridSize + LittleTile.gridSize);
					int maxY = Math.min(this.maxY, y*LittleTile.gridSize + LittleTile.gridSize);
					int maxZ = Math.min(this.maxZ, z*LittleTile.gridSize + LittleTile.gridSize);
					
					if(maxX > minX && maxY > minY && maxZ > minZ)
					{
						tempBoxes.clear();
						
						BlockPos pos = new BlockPos(x, y, z);
						int offsetX = x*LittleTile.gridSize;
						int offsetY = y*LittleTile.gridSize;
						int offsetZ = z*LittleTile.gridSize;
						
						extractBox(minX, minY, minZ, maxX, maxY, maxZ, tempBoxes);
						for (LittleTileBox box : tempBoxes) {
							
							box.minX -= offsetX;
							box.maxX -= offsetX;
							
							box.minY -= offsetY;
							box.maxY -= offsetY;
							
							box.minZ -= offsetZ;
							box.maxZ -= offsetZ;
							
							boxes.add(pos, box);
						}
					}
				}
			}
		}
	}

	public boolean doesFillEntireBlock()
	{
		return minX == 0 && minY == 0 && minZ == 0 && maxX == LittleTile.gridSize && maxY == LittleTile.gridSize && maxZ == LittleTile.gridSize;
	}
	
	public LittleTileBox createOutsideBlockBox(EnumFacing facing)
	{
		LittleTileBox box = this.copy();
		switch(facing)
		{
		case EAST:
			box.minX = 0;
			box.maxX -= LittleTile.gridSize;
			break;
		case WEST:
			box.minX += LittleTile.gridSize;
			box.maxX = LittleTile.gridSize;
			break;
		case UP:
			box.minY = 0;
			box.maxY -= LittleTile.gridSize;
			break;
		case DOWN:
			box.minY += LittleTile.gridSize;
			box.maxY = LittleTile.gridSize;
			break;
		case SOUTH:
			box.minZ = 0;
			box.maxZ -= LittleTile.gridSize;
			break;
		case NORTH:
			box.minZ += LittleTile.gridSize;
			box.maxZ = LittleTile.gridSize;
			break;
		}
		return box;
	}
	
	/*public LittleTileBox createInsideBlockBox(EnumFacing facing)
	{
		Vec3i vec = facing.getDirectionVec();
		return new LittleTileBox(minX - vec.getX() * LittleTile.gridSize, minY - vec.getY() * LittleTile.gridSize, minZ - vec.getZ() * LittleTile.gridSize,
				maxX - vec.getX() * LittleTile.gridSize, maxY - vec.getY() * LittleTile.gridSize, maxZ - vec.getZ() * LittleTile.gridSize);
	}*/
	
	//================Box to box================
	
	public LittleTileBox combineBoxes(LittleTileBox box, BasicCombiner combinator)
	{
		if(box.getClass() != LittleTileBox.class)
			return null;
		
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
	
	@Nullable
	public EnumFacing sharedBoxFace(LittleTileBox box)
	{
		boolean x = this.minX == box.minX && this.maxX == box.maxX;
		boolean y = this.minY == box.minY && this.maxY == box.maxY;
		boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
		
		if(x && y && z)
		{
			return null;
		}
		if(x && y)
		{
			if(this.minZ == box.maxZ)
				return EnumFacing.SOUTH;
			else if(this.maxZ == box.minZ)
				return EnumFacing.NORTH;
		}
		if(x && z)
		{
			if(this.minY == box.maxY)
				return EnumFacing.UP;
			else if(this.maxY == box.minY)
				return EnumFacing.DOWN;
		}
		if(y && z)
		{
			if(this.minX == box.maxX)
				return EnumFacing.EAST;
			else if(this.maxX == box.minX)
				return EnumFacing.WEST;
		}
		return null;
	}
	
	/**
	 * @param cutout a list of boxes which have been cut out.
	 * @return all remaining boxes or null if the box remains as it is
	 */
	public List<LittleTileBox> cutOut(List<LittleTileBox> boxes, List<LittleTileBox> cutout)
	{
		ArrayList<LittleTileBox> newBoxes = new ArrayList<>();
		
		for (int littleX = minX; littleX < maxX; littleX++) {
			for (int littleY = minY; littleY < maxY; littleY++) {
				for (int littleZ = minZ; littleZ < maxZ; littleZ++) {
					boolean isInside = false;
					for (int i = 0; i < boxes.size(); i++) {
						if(boxes.get(i).isVecInsideBox(littleX, littleY, littleZ))
						{
							isInside = true;
							break;
						}
					}
					
					LittleTileBox box = extractBox(littleX, littleY, littleZ);
					if(box != null)
						if(isInside)
							cutout.add(box);
						else
							newBoxes.add(box);
				}
			}
		}
		
		BasicCombiner.combineBoxes(newBoxes);
		
		if(newBoxes.size() == 1 && newBoxes.get(0).equals(this))
			return null;
		
		BasicCombiner.combineBoxes(cutout);
		
		return newBoxes;
	}
	
	/**
	 * @return all remaining boxes or null if the box remains as it is
	 */
	public List<LittleTileBox> cutOut(LittleTileBox box)
	{
		if(intersectsWith(box))
		{
			ArrayList<LittleTileBox> boxes = new ArrayList<>();
			for (int littleX = minX; littleX < maxX; littleX++) {
				for (int littleY = minY; littleY < maxY; littleY++) {
					for (int littleZ = minZ; littleZ < maxZ; littleZ++) {
						if(!box.isVecInsideBox(littleX, littleY, littleZ))
							extractBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1, boxes);
					}
				}
			}
			
			BasicCombiner.combineBoxes(boxes);
			
			return boxes;
		}
		
		return null;
	}
	
	protected boolean intersectsWith(LittleTileBox box)
    {
        return box.maxX > this.minX && box.minX < this.maxX && box.maxY > this.minY && box.minY < this.maxY && box.maxZ > this.minZ && box.minZ < this.maxZ;
    }
	
	public boolean containsBox(LittleTileBox box)
	{
		return this.minX <= box.minX && this.maxX >= box.maxX && this.minY <= box.minY && this.maxY >= box.maxY && this.minZ <= box.minZ && this.maxZ >= box.maxZ;
	}
	
	//================Vectors================
	
	public void addOffset(Vec3i vec)
	{
		minX += vec.getX() * LittleTile.gridSize;
		minY += vec.getY() * LittleTile.gridSize;
		minZ += vec.getZ() * LittleTile.gridSize;
		maxX += vec.getX() * LittleTile.gridSize;
		maxY += vec.getY() * LittleTile.gridSize;
		maxZ += vec.getZ() * LittleTile.gridSize;
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
	
	public void subOffset(Vec3i vec)
	{
		minX -= vec.getX() * LittleTile.gridSize;
		minY -= vec.getY() * LittleTile.gridSize;
		minZ -= vec.getZ() * LittleTile.gridSize;
		maxX -= vec.getX() * LittleTile.gridSize;
		maxY -= vec.getY() * LittleTile.gridSize;
		maxZ -= vec.getZ() * LittleTile.gridSize;
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
	
	public LittleTileVec getMinVec()
	{
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public LittleTileVec getMaxVec()
	{
		return new LittleTileVec(maxX, maxY, maxZ);
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
	
	public boolean isVecInsideBox(int x, int y, int z)
	{
		return x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ;
	}
	
	public boolean isVecInsideBox(LittleTileVec vec)
	{
		return vec.x >= minX && vec.x < maxX && vec.y >= minY && vec.y < maxY && vec.z >= minZ && vec.z < maxZ;
	}
	
	public boolean intersectsWithFace(EnumFacing facing, LittleTileVec vec, boolean completely)
	{
		Axis one = RotationUtils.getDifferentAxisFirst(facing.getAxis());
		Axis two = RotationUtils.getDifferentAxisFirst(facing.getAxis());
		return vec.getAxis(one) >= getMin(one) && vec.getAxis(one) <= getMax(one) && vec.getAxis(two) >= getMin(two) && vec.getAxis(two) <= getMax(two);
	}
	
	public boolean intersectsWithAxis(Axis axis, Vec3d vec)
	{
		switch(axis)
		{
		case X:
			return intersectsWithYZ(vec);
		case Y:
			return intersectsWithXZ(vec);
		case Z:
			return intersectsWithXY(vec);
		}
		return false;
	}
	
    public boolean intersectsWithYZ(Vec3d vec)
    {
        return vec.yCoord >= LittleUtils.toVanillaGrid(this.minY) && vec.yCoord < LittleUtils.toVanillaGrid(this.maxY) && vec.zCoord >= LittleUtils.toVanillaGrid(this.minZ) && vec.zCoord < LittleUtils.toVanillaGrid(this.maxZ);
    }

    public boolean intersectsWithXZ(Vec3d vec)
    {
        return vec.xCoord >= LittleUtils.toVanillaGrid(this.minX) && vec.xCoord < LittleUtils.toVanillaGrid(this.maxX) && vec.zCoord >= LittleUtils.toVanillaGrid(this.minZ) && vec.zCoord < LittleUtils.toVanillaGrid(this.maxZ);
    }

    public boolean intersectsWithXY(Vec3d vec)
    {
        return vec.xCoord >= LittleUtils.toVanillaGrid(this.minX) && vec.xCoord < LittleUtils.toVanillaGrid(this.maxX) && vec.yCoord >= LittleUtils.toVanillaGrid(this.minY) && vec.yCoord < LittleUtils.toVanillaGrid(this.maxY);
    }
	
	public LittleTileVec getCenter()
	{
		return new LittleTileVec((maxX + minX)/2, (maxY + minY)/2, (maxZ + minZ)/2);
	}
	
	@Nullable
    protected Vec3d collideWithPlane(Axis axis, double value, Vec3d vecA, Vec3d vecB)
    {
        Vec3d vec3d = axis != Axis.X ? axis != Axis.Y ? vecA.getIntermediateWithZValue(vecB, value) : vecA.getIntermediateWithYValue(vecB, value) : vecA.getIntermediateWithXValue(vecB, value);
        return vec3d != null && intersectsWithAxis(axis, vec3d) ? vec3d : null;
    }
	
	@Nullable
	public RayTraceResult calculateIntercept(BlockPos pos, Vec3d vecA, Vec3d vecB)
    {
		vecA = vecA.subtract(pos.getX(), pos.getY(), pos.getZ());
		vecB = vecB.subtract(pos.getX(), pos.getY(), pos.getZ());
		
		Vec3d collision = null;
		EnumFacing collided = null;
		
		for (EnumFacing facing : EnumFacing.VALUES) {
			Vec3d temp = collideWithPlane(facing.getAxis(), (double) getValueOfFacing(facing)/LittleTile.gridSize, vecA, vecB);
			if(temp != null && isClosest(vecA, collision, temp))
			{
				collided = facing;
				collision = temp;
			}
		}
		
		if(collision == null)
			return null;
		
        return new RayTraceResult(collision.addVector(pos.getX(), pos.getY(), pos.getZ()), collided, pos);
    }
	
	//================Rotation & Flip================
	
	/**
	 * 
	 * @param rotation
	 * @param doubledCenter coordinates are doubled, meaning in order to get the correct coordinates they have to be divided by two. This allows to rotate around even axis.
	 */
	public void rotateBox(Rotation rotation, LittleTileVec doubledCenter)
	{
		long tempMinX = minX*2 - doubledCenter.x;
		long tempMinY = minY*2 - doubledCenter.y;
		long tempMinZ = minZ*2 - doubledCenter.z;
		long tempMaxX = maxX*2 - doubledCenter.x;
		long tempMaxY = maxY*2 - doubledCenter.y;
		long tempMaxZ = maxZ*2 - doubledCenter.z;
		resort((int) ((rotation.getMatrix().getX(tempMinX, tempMinY, tempMinZ) + doubledCenter.x) / 2),
				(int) ((rotation.getMatrix().getY(tempMinX, tempMinY, tempMinZ) + doubledCenter.y) / 2),
				(int) ((rotation.getMatrix().getZ(tempMinX, tempMinY, tempMinZ) + doubledCenter.z) / 2),
				(int) ((rotation.getMatrix().getX(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.x) / 2),
				(int) ((rotation.getMatrix().getY(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.y) / 2),
				(int) ((rotation.getMatrix().getZ(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.z) / 2));
	}
	
	/*public void rotateBox(Rotation rotation)
	{
		int tempMinX = minX;
		int tempMinY = minY;
		int tempMinZ = minZ;
		int tempMaxX = maxX;
		int tempMaxY = maxY;
		int tempMaxZ = maxZ;
		resort(rotation.getMatrix().getX(tempMinX, tempMinY, tempMinZ), rotation.getMatrix().getY(tempMinX, tempMinY, tempMinZ), rotation.getMatrix().getZ(tempMinX, tempMinY, tempMinZ),
				rotation.getMatrix().getX(tempMaxX, tempMaxY, tempMaxZ), rotation.getMatrix().getY(tempMaxX, tempMaxY, tempMaxZ), rotation.getMatrix().getZ(tempMaxX, tempMaxY, tempMaxZ));
	}*/
	
	/**
	 * 
	 * @param axis
	 * @param doubledCenter coordinates are doubled, meaning in order to get the correct coordinates they have to be divided by two. This allows to flip around even axis.
	 */
	public void flipBox(Axis axis, LittleTileVec doubledCenter)
	{
		long tempMin = getMin(axis)*2 - doubledCenter.getAxis(axis);
		long tempMax = getMax(axis)*2 - doubledCenter.getAxis(axis);
		int min = (int) ((doubledCenter.getAxis(axis) - tempMin) / 2);
		int max = (int) ((doubledCenter.getAxis(axis) - tempMax) / 2);
		setMin(axis, Math.min(min, max));
		setMax(axis, Math.max(min, max));
	}
	
	/*public void flipBox(Axis axis)
	{
		switch(axis)
		{
		case X:
			minX = -minX;
			maxX = -maxX;
			break;
		case Y:
			minY = -minY;
			maxY = -maxY;
			break;
		case Z:
			minZ = -minZ;
			maxZ = -maxZ;
			break;
		default:
			break;
		}
		
		resort();
	}*/
	
	//================Basic Object Overrides================
	
	@Override
	public int hashCode() {
		return minX + minY + minZ + maxX + maxY + maxZ;
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
	
	//================Special methods================
	
	public LittleTileBox extractBox(int x, int y, int z)
	{
		return new LittleTileBox(x, y, z, x+1, y+1, z+1);
	}
	
	public List<LittleTileBox> extractBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, List<LittleTileBox> boxes)
	{
		boxes.add(new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ));
		return boxes;
	}
	
	public LittleTileBox copy()
	{
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public boolean isFaceAtEdge(EnumFacing facing)
	{
		if(facing.getAxisDirection() == AxisDirection.POSITIVE)
			return getMax(facing.getAxis()) == LittleTile.gridSize;
		else
			return getMin(facing.getAxis()) == LittleTile.minPos;
	}
	
	public LittleTileBox grow(EnumFacing facing)
	{
		Axis axis = facing.getAxis();
		LittleTileBox result = this.copy();
		if(facing.getAxisDirection() == AxisDirection.POSITIVE)
			result.setMax(axis, getMax(axis) + 1);
		else
			result.setMin(axis, getMin(axis) - 1);
		return result;
	}
	
	public LittleTileBox shrink(EnumFacing facing, boolean toLimit)
	{
		Axis axis = facing.getAxis();
		if(getSize(axis) > 1)
		{
			LittleTileBox result = this.copy();
			if(facing.getAxisDirection() == AxisDirection.POSITIVE)
				result.setMax(axis, toLimit ? getMin(axis) + 1 : getMax(axis) - 1);
			else
				result.setMin(axis, toLimit ? getMax(axis) - 1 : getMin(axis) + 1);
			return result;
		}
		return null;
	}

	public void resort()
	{
		set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}
	
	public void resort(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}

	/*public LittleTileBox createNeighbourBox(EnumFacing facing)
	{
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
	}*/
	
	public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public void assignCube(CubeObject cube)
	{
		this.minX = (int)(cube.minX*LittleTile.gridSize);
		this.minY = (int)(cube.minY*LittleTile.gridSize);
		this.minZ = (int)(cube.minZ*LittleTile.gridSize);
		this.maxX = (int)(cube.maxX*LittleTile.gridSize);
		this.maxY = (int)(cube.maxY*LittleTile.gridSize);
		this.maxZ = (int)(cube.maxZ*LittleTile.gridSize);
	}
	
	//================Rendering================
	
	@SideOnly(Side.CLIENT)
	public LittleRenderingCube getRenderingCube(Block block, int meta)
	{
		return getRenderingCube(this.getCube(), block, meta);
	}
	
	@SideOnly(Side.CLIENT)
	public LittleRenderingCube getRenderingCube(CubeObject cube, Block block, int meta)
	{
		return new LittleRenderingCube(cube, this, block, meta);
	}
	
	//================Faces================
	
	@Nullable
	public LittleTileFace getFace(EnumFacing facing)
	{
		Axis one = RotationUtils.getDifferentAxisFirst(facing.getAxis());
		Axis two = RotationUtils.getDifferentAxisSecond(facing.getAxis());
		
		return new LittleTileFace(facing, getMin(one), getMin(two), getMax(one), getMax(two), facing.getAxisDirection() == AxisDirection.POSITIVE ? getMax(facing.getAxis()) : getMin(facing.getAxis()));
	}
	
	public boolean intersectsWith(LittleTileFace face)
	{
		return (face.face.getAxisDirection() == AxisDirection.POSITIVE ? getMin(face.face.getAxis()) : getMax(face.face.getAxis())) == face.origin && 
				face.maxOne > getMin(face.one) && face.minOne < getMax(face.one) &&
				face.maxTwo > getMin(face.two) && face.minTwo < getMax(face.two);
	}
	
	public boolean canFaceBeCombined(LittleTileBox other)
	{
		return true;
	}
	
	public void fill(LittleTileFace face)
	{
		if(intersectsWith(face))
		{
			int minOne = Math.max(getMin(face.one), face.minOne);
			int maxOne = Math.min(getMax(face.one), face.maxOne);
			int minTwo = Math.max(getMin(face.two), face.minTwo);
			int maxTwo = Math.min(getMax(face.two), face.maxTwo);
			if(isCompletelyFilled())
			{
				for (int one = minOne; one < maxOne; one++) {
					for (int two = minTwo; two < maxTwo; two++) {
						face.filled[one-face.minOne][two-face.minTwo] = true;
					}
				}
			}else{
				boolean completely = !canFaceBeCombined(face.getBox());
				int min = getValueOfFacing(face.face.getOpposite());
				if(face.face.getAxisDirection() == AxisDirection.NEGATIVE)
					min--;
				LittleTileVec vec = new LittleTileVec(min, min, min);
				for (int one = minOne; one < maxOne; one++) {
					for (int two = minTwo; two < maxTwo; two++) {
						vec.setAxis(face.one, one);
						vec.setAxis(face.two, two);
						if(intersectsWithFace(face.face.getOpposite(), vec, completely)) //isVecInsideBox(vec))
							face.filled[one-face.minOne][two-face.minTwo] = true;
					}
				}
			}
		}
	}
	
	public class LittleTileFace {
		
		public Axis one;
		public Axis two;
		public EnumFacing face;
		public int minOne;
		public int minTwo;
		public int maxOne;
		public int maxTwo;
		public int origin;
		public int oldOrigin;
		
		public boolean[][] filled;
		
		public LittleTileFace(EnumFacing face, int minOne, int minTwo, int maxOne, int maxTwo, int origin) {
			this.face = face;
			this.one = RotationUtils.getDifferentAxisFirst(face.getAxis());
			this.two = RotationUtils.getDifferentAxisSecond(face.getAxis());
			this.minOne = minOne;
			this.minTwo = minTwo;
			this.maxOne = maxOne;
			this.maxTwo = maxTwo;
			this.origin = origin;
			this.oldOrigin = origin;
			filled = new boolean[maxOne-minOne][maxTwo-minTwo];
		}
		
		public boolean isFilled()
		{
			int min = oldOrigin;
			if(face.getAxisDirection() == AxisDirection.POSITIVE)
				min--;
			LittleTileVec vec = new LittleTileVec(min, min, min);
			for (int one = 0; one < filled.length; one++) {
				for (int two = 0; two < filled[one].length; two++) {
					vec.setAxis(this.one, minOne + one);
					vec.setAxis(this.two, minTwo + two);
					if(!filled[one][two] && LittleTileBox.this.intersectsWithFace(face, vec, false)) //&& LittleTileBox.this.isVecInsideBox(vec))
						return false;
				}
			}
			return true;
		}
		
		public LittleTileBox getBox()
		{
			return LittleTileBox.this;
		}
		
		public boolean isFaceInsideBlock()
		{
			return origin > LittleTile.minPos && origin < LittleTile.maxPos;
		}
		
		public void move(EnumFacing facing)
		{
			origin = face.getAxisDirection() == AxisDirection.POSITIVE ? LittleTile.minPos : LittleTile.maxPos;
		}
	}
	
	//================Identifier================
	
	public int[] getIdentifier()
	{
		return new int[]{minX, minY, minZ};
	}
	
	public boolean is(int[] identifier)
	{
		if(identifier.length == 3)
			return identifier[0] == minX && identifier[1] == minY && identifier[2] == minZ;
		return false;
	}
	
	//================Static Helpers================
	
	public static LittleTileBox loadBox(String name, NBTTagCompound nbt)
	{
		if(nbt.getTag(name + "minX") instanceof NBTTagByte) //very old pre 1.0.0
		{
			LittleTileBox box = new LittleTileBox((byte) nbt.getByte(name+"minX"), (byte) nbt.getByte(name+"minY"), (byte) nbt.getByte(name+"minZ"), (byte) nbt.getByte(name+"maxX"), (byte) nbt.getByte(name+"maxY"), (byte) nbt.getByte(name+"maxZ"));
			nbt.removeTag(name+"minX");
			nbt.removeTag(name+"minY");
			nbt.removeTag(name+"minZ");
			nbt.removeTag(name+"maxX");
			nbt.removeTag(name+"maxY");
			nbt.removeTag(name+"maxZ");
			box.writeToNBT(name, nbt);
			return box;
		}
		else if(nbt.getTag(name + "minX") instanceof NBTTagInt) //old pre 1.3.0
		{
			LittleTileBox box = new LittleTileBox(nbt.getInteger(name+"minX"), nbt.getInteger(name+"minY"), nbt.getInteger(name+"minZ"), nbt.getInteger(name+"maxX"), nbt.getInteger(name+"maxY"), nbt.getInteger(name+"maxZ"));
			nbt.removeTag(name+"minX");
			nbt.removeTag(name+"minY");
			nbt.removeTag(name+"minZ");
			nbt.removeTag(name+"maxX");
			nbt.removeTag(name+"maxY");
			nbt.removeTag(name+"maxZ");
			box.writeToNBT(name, nbt);
			return box;
		}else if(nbt.getTag(name) instanceof NBTTagIntArray){ //New
			return createBox(nbt.getIntArray(name));
		}else if(nbt.getTag(name) instanceof NBTTagString){ //Not used anymore pre 1.5.0
			String[] coords = nbt.getString(name).split("\\.");
			try{
				return new LittleTileBox(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]), Integer.parseInt(coords[4]), Integer.parseInt(coords[5]));
			}catch(Exception e){
				
			}
		}
		return new LittleTileBox(0, 0, 0, 0, 0, 0);
	}
	
	public static LittleTileBox createBox(int[] array)
	{
		switch(array.length)
		{
		case 6:
			return new LittleTileBox(array[0], array[1], array[2], array[3], array[4], array[5]);
		case 7:
			return new LittleTileSlicedOrdinaryBox(array[0], array[1], array[2], array[3], array[4], array[5], LittleSlice.getSliceByID(array[6]));
		case 11:
			return new LittleTileSlicedBox(array[0], array[1], array[2], array[3], array[4], array[5], LittleSlice.getSliceByID(array[6]), Float.intBitsToFloat(array[7]), Float.intBitsToFloat(array[8]), Float.intBitsToFloat(array[9]), Float.intBitsToFloat(array[10]));
		default: 
			throw new InvalidParameterException("No valid coords given " + Arrays.toString(array));
		}
	}
	
	public static void combineBoxesBlocks(List<LittleTileBox> boxes)
	{
		HashMapList<BlockPos, LittleTileBox> chunked = new HashMapList<>();
		for (int i = 0; i < boxes.size(); i++) {
			chunked.add(boxes.get(i).getMinVec().getBlockPos(), boxes.get(i));
		}
		boxes.clear();
		for (Iterator<ArrayList<LittleTileBox>> iterator = chunked.getValues().iterator(); iterator.hasNext();) {
			ArrayList<LittleTileBox> list = iterator.next();
			BasicCombiner.combineBoxes(list);
			boxes.addAll(list);
		}
	}
	
	public static boolean isClosest(Vec3d from, @Nullable Vec3d optional, Vec3d toCheck)
    {
        return optional == null || from.squareDistanceTo(toCheck) < from.squareDistanceTo(optional);
    }
	
	public static boolean intersectsWith(LittleTileBox box, LittleTileBox box2)
	{
		if(box.getClass() == LittleTileBox.class)
			return box2.intersectsWith(box);
		return box.intersectsWith(box2);
	}
}
