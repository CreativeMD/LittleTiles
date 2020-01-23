package com.creativemd.littletiles.common.tile.math.box;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.RangedBitSet;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils.BoxCorner;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.client.render.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.box.slice.LittleSlice;
import com.creativemd.littletiles.common.tile.math.box.slice.LittleSlicedBox;
import com.creativemd.littletiles.common.tile.math.box.slice.LittleSlicedOrdinaryBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.vec.SplitRangeBoxes;
import com.creativemd.littletiles.common.utils.vec.SplitRangeBoxes.SplitRangeBox;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleBox {
	
	public static final int secondMethodVolume = 256;
	
	// ================Data================
	
	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;
	
	// ================Constructors================
	
	public LittleBox(LittleVec center, int sizeX, int sizeY, int sizeZ) {
		LittleVec offset = new LittleVec(sizeX, sizeY, sizeZ).calculateCenter();
		minX = center.x - offset.x;
		minY = center.y - offset.y;
		minZ = center.z - offset.z;
		maxX = minX + sizeX;
		maxY = minY + sizeY;
		maxZ = minZ + sizeZ;
	}
	
	public LittleBox(LittleGridContext context, CubeObject cube) {
		this(context.toGrid(cube.minX), context.toGrid(cube.minY), context.toGrid(cube.minZ), context.toGrid(cube.maxX), context.toGrid(cube.maxY), context.toGrid(cube.maxZ));
	}
	
	public LittleBox(LittleGridContext context, AxisAlignedBB box) {
		this(context.toGrid(box.minX), context.toGrid(box.minY), context.toGrid(box.minZ), context.toGrid(box.maxX), context.toGrid(box.maxY), context.toGrid(box.maxZ));
	}
	
	public LittleBox(LittleBox... boxes) {
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
	
	public LittleBox(LittleVec min, LittleVec max) {
		this(min.x, min.y, min.z, max.x, max.y, max.z);
	}
	
	public LittleBox(LittleVec min) {
		this(min.x, min.y, min.z, min.x + 1, min.y + 1, min.z + 1);
	}
	
	public LittleBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		set(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	// ================Conversions================
	
	public void addCollisionBoxes(LittleGridContext context, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, BlockPos offset) {
		AxisAlignedBB axisalignedbb = getBox(context, offset);
		
		if (entityBox.intersects(axisalignedbb)) {
			collidingBoxes.add(axisalignedbb);
		}
	}
	
	public AxisAlignedBB getSelectionBox(LittleGridContext context, BlockPos pos) {
		return getBox(context, pos);
	}
	
	public AxisAlignedBB getBox(LittleGridContext context, BlockPos offset) {
		return new AxisAlignedBB(context.toVanillaGrid(minX) + offset.getX(), context.toVanillaGrid(minY) + offset.getY(), context.toVanillaGrid(minZ) + offset.getZ(), context.toVanillaGrid(maxX) + offset.getX(), context.toVanillaGrid(maxY) + offset.getY(), context.toVanillaGrid(maxZ) + offset.getZ());
	}
	
	public AxisAlignedBB getBox(LittleGridContext context) {
		return new AxisAlignedBB(context.toVanillaGrid(minX), context.toVanillaGrid(minY), context.toVanillaGrid(minZ), context.toVanillaGrid(maxX), context.toVanillaGrid(maxY), context.toVanillaGrid(maxZ));
	}
	
	public CubeObject getCube(LittleGridContext context) {
		return new CubeObject((float) context.toVanillaGrid(minX), (float) context.toVanillaGrid(minY), (float) context.toVanillaGrid(minZ), (float) context.toVanillaGrid(maxX), (float) context.toVanillaGrid(maxY), (float) context.toVanillaGrid(maxZ));
	}
	
	// ================Save================
	
	public int[] getArray() {
		return new int[] { minX, minY, minZ, maxX, maxY, maxZ };
	}
	
	public NBTTagIntArray getNBTIntArray() {
		return new NBTTagIntArray(getArray());
	}
	
	public void writeToNBT(String name, NBTTagCompound nbt) {
		nbt.setIntArray(name, getArray());
	}
	
	// ================Size & Volume================
	
	public int getSmallestContext(LittleGridContext context) {
		int size = LittleGridContext.minSize;
		size = Math.max(size, context.getMinGrid(minX));
		size = Math.max(size, context.getMinGrid(minY));
		size = Math.max(size, context.getMinGrid(minZ));
		size = Math.max(size, context.getMinGrid(maxX));
		size = Math.max(size, context.getMinGrid(maxY));
		size = Math.max(size, context.getMinGrid(maxZ));
		return size;
	}
	
	public void convertTo(LittleGridContext from, LittleGridContext to) {
		if (from.size > to.size) {
			int ratio = from.size / to.size;
			minX /= ratio;
			minY /= ratio;
			minZ /= ratio;
			maxX /= ratio;
			maxY /= ratio;
			maxZ /= ratio;
		} else {
			int ratio = to.size / from.size;
			minX *= ratio;
			minY *= ratio;
			minZ *= ratio;
			maxX *= ratio;
			maxY *= ratio;
			maxZ *= ratio;
		}
	}
	
	public void convertTo(int from, int to) {
		if (from > to) {
			int ratio = from / to;
			minX /= ratio;
			minY /= ratio;
			minZ /= ratio;
			maxX /= ratio;
			maxY /= ratio;
			maxZ /= ratio;
		} else {
			int ratio = to / from;
			minX *= ratio;
			minY *= ratio;
			minZ *= ratio;
			maxX *= ratio;
			maxY *= ratio;
			maxZ *= ratio;
		}
	}
	
	public boolean isCompletelyFilled() {
		return true;
	}
	
	public int getLongestSide() {
		return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
	}
	
	public Vec3d getSizeVec(LittleGridContext context) {
		return new Vec3d(context.toVanillaGrid(maxX - minX), context.toVanillaGrid(maxY - minY), context.toVanillaGrid(maxZ - minZ));
	}
	
	public LittleVec getSize() {
		return new LittleVec(maxX - minX, maxY - minY, maxZ - minZ);
	}
	
	public double getVolume() {
		return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
	}
	
	/** @return the volume in percent to a size of a normal block */
	public double getPercentVolume(LittleGridContext context) {
		return getVolume() / (context.maxTilesPerBlock);
	}
	
	public int getValueOfFacing(EnumFacing facing) {
		switch (facing) {
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
	
	public LittleVec getCorner(BoxCorner corner) {
		return new LittleVec(getCornerX(corner), getCornerY(corner), getCornerZ(corner));
	}
	
	public Vec3d getExactCorner(BoxCorner corner) {
		return new Vec3d(getCornerX(corner), getCornerY(corner), getCornerZ(corner));
	}
	
	public int getCornerValue(BoxCorner corner, Axis axis) {
		return getValueOfFacing(corner.getFacing(axis));
	}
	
	public int getCornerX(BoxCorner corner) {
		return getValueOfFacing(corner.x);
	}
	
	public int getCornerY(BoxCorner corner) {
		return getValueOfFacing(corner.y);
	}
	
	public int getCornerZ(BoxCorner corner) {
		return getValueOfFacing(corner.z);
	}
	
	public int getSize(Axis axis) {
		switch (axis) {
		case X:
			return maxX - minX;
		case Y:
			return maxY - minY;
		case Z:
			return maxZ - minZ;
		}
		return 0;
	}
	
	public void setMin(Axis axis, int value) {
		switch (axis) {
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
	
	public int getMin(Axis axis) {
		switch (axis) {
		case X:
			return minX;
		case Y:
			return minY;
		case Z:
			return minZ;
		}
		return 0;
	}
	
	public void setMax(Axis axis, int value) {
		switch (axis) {
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
	
	public int getMax(Axis axis) {
		switch (axis) {
		case X:
			return maxX;
		case Y:
			return maxY;
		case Z:
			return maxZ;
		}
		return 0;
	}
	
	// ================Block Integration================
	
	public boolean isValidBox() {
		return maxX > minX && maxY > minY && maxZ > minZ;
	}
	
	public boolean needsMultipleBlocks(LittleGridContext context) {
		int x = minX / context.size;
		int y = minY / context.size;
		int z = minZ / context.size;
		
		return maxX - x * context.size <= context.maxPos && maxY - y * context.size <= context.maxPos && maxZ - z * context.size <= context.maxPos;
	}
	
	public boolean isBoxInsideBlock(LittleGridContext context) {
		return minX >= 0 && maxX <= context.maxPos && minY >= 0 && maxY <= context.maxPos && minZ >= 0 && maxZ <= context.maxPos;
	}
	
	public void split(LittleGridContext context, BlockPos offset, HashMapList<BlockPos, LittleBox> boxes) {
		int minOffX = context.toBlockOffset(minX);
		int minOffY = context.toBlockOffset(minY);
		int minOffZ = context.toBlockOffset(minZ);
		
		int maxOffX = context.toBlockOffset(maxX);
		int maxOffY = context.toBlockOffset(maxY);
		int maxOffZ = context.toBlockOffset(maxZ);
		
		List<LittleBox> tempBoxes = new ArrayList<>();
		
		for (int x = minOffX; x <= maxOffX; x++) {
			for (int y = minOffY; y <= maxOffY; y++) {
				for (int z = minOffZ; z <= maxOffZ; z++) {
					int minX = Math.max(this.minX, x * context.size);
					int minY = Math.max(this.minY, y * context.size);
					int minZ = Math.max(this.minZ, z * context.size);
					int maxX = Math.min(this.maxX, x * context.size + context.size);
					int maxY = Math.min(this.maxY, y * context.size + context.size);
					int maxZ = Math.min(this.maxZ, z * context.size + context.size);
					
					if (maxX > minX && maxY > minY && maxZ > minZ) {
						tempBoxes.clear();
						
						BlockPos pos = new BlockPos(x + offset.getX(), y + offset.getY(), z + offset.getZ());
						int offsetX = x * context.size;
						int offsetY = y * context.size;
						int offsetZ = z * context.size;
						
						extractBox(minX, minY, minZ, maxX, maxY, maxZ, tempBoxes);
						for (LittleBox box : tempBoxes) {
							
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
	
	public boolean doesFillEntireBlock(LittleGridContext context) {
		return minX == 0 && minY == 0 && minZ == 0 && maxX == context.size && maxY == context.size && maxZ == context.size;
	}
	
	public LittleBox createOutsideBlockBox(LittleGridContext context, EnumFacing facing) {
		LittleBox box = this.copy();
		switch (facing) {
		case EAST:
			box.minX = 0;
			box.maxX -= context.size;
			break;
		case WEST:
			box.minX += context.size;
			box.maxX = context.size;
			break;
		case UP:
			box.minY = 0;
			box.maxY -= context.size;
			break;
		case DOWN:
			box.minY += context.size;
			box.maxY = context.size;
			break;
		case SOUTH:
			box.minZ = 0;
			box.maxZ -= context.size;
			break;
		case NORTH:
			box.minZ += context.size;
			box.maxZ = context.size;
			break;
		}
		return box;
	}
	
	/* public LittleTileBox createInsideBlockBox(EnumFacing facing) { Vec3i vec =
	 * facing.getDirectionVec(); return new LittleTileBox(minX - vec.getX() *
	 * LittleTile.gridSize, minY - vec.getY() * LittleTile.gridSize, minZ -
	 * vec.getZ() * LittleTile.gridSize, maxX - vec.getX() * LittleTile.gridSize,
	 * maxY - vec.getY() * LittleTile.gridSize, maxZ - vec.getZ() *
	 * LittleTile.gridSize); } */
	
	// ================Box to box================
	
	public LittleBox combineBoxes(LittleBox box, BasicCombiner combinator) {
		if (box.getClass() != LittleBox.class)
			return null;
		
		boolean x = this.minX == box.minX && this.maxX == box.maxX;
		boolean y = this.minY == box.minY && this.maxY == box.maxY;
		boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
		
		if (x && y && z) {
			return this;
		}
		if (x && y) {
			if (this.minZ == box.maxZ)
				return new LittleBox(minX, minY, box.minZ, maxX, maxY, maxZ);
			else if (this.maxZ == box.minZ)
				return new LittleBox(minX, minY, minZ, maxX, maxY, box.maxZ);
		}
		if (x && z) {
			if (this.minY == box.maxY)
				return new LittleBox(minX, box.minY, minZ, maxX, maxY, maxZ);
			else if (this.maxY == box.minY)
				return new LittleBox(minX, minY, minZ, maxX, box.maxY, maxZ);
		}
		if (y && z) {
			if (this.minX == box.maxX)
				return new LittleBox(box.minX, minY, minZ, maxX, maxY, maxZ);
			else if (this.maxX == box.minX)
				return new LittleBox(minX, minY, minZ, box.maxX, maxY, maxZ);
		}
		return null;
	}
	
	@Nullable
	public EnumFacing sharedBoxFace(LittleBox box) {
		boolean x = this.minX == box.minX && this.maxX == box.maxX;
		boolean y = this.minY == box.minY && this.maxY == box.maxY;
		boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
		
		if (x && y && z) {
			return null;
		}
		if (x && y) {
			if (this.minZ == box.maxZ)
				return EnumFacing.SOUTH;
			else if (this.maxZ == box.minZ)
				return EnumFacing.NORTH;
		}
		if (x && z) {
			if (this.minY == box.maxY)
				return EnumFacing.UP;
			else if (this.maxY == box.minY)
				return EnumFacing.DOWN;
		}
		if (y && z) {
			if (this.minX == box.maxX)
				return EnumFacing.EAST;
			else if (this.maxX == box.minX)
				return EnumFacing.WEST;
		}
		return null;
	}
	
	public SplitRangeBoxes split(List<LittleBox> boxes) {
		RangedBitSet x = split(Axis.X, boxes);
		RangedBitSet y = split(Axis.Y, boxes);
		RangedBitSet z = split(Axis.Z, boxes);
		if (x != null && y != null && z != null)
			return new SplitRangeBoxes(x, y, z);
		return null;
	}
	
	protected RangedBitSet split(Axis axis, List<LittleBox> boxes) {
		int min = getMin(axis);
		int max = getMax(axis);
		RangedBitSet set = new RangedBitSet(min, max);
		
		for (LittleBox box : boxes) {
			
			if (!box.isCompletelyFilled())
				return null;
			
			if (box.intersectsWith(this)) {
				set.add(box.getMin(axis));
				set.add(box.getMax(axis));
			}
		}
		
		return set;
	}
	
	/** @param cutout
	 *            a list of boxes which have been cut out.
	 * @return all remaining boxes or null if the box remains as it is */
	public List<LittleBox> cutOut(List<LittleBox> boxes, List<LittleBox> cutout) {
		List<LittleBox> newBoxes = new ArrayList<>();
		SplitRangeBoxes ranges;
		if (getVolume() > secondMethodVolume && (ranges = split(boxes)) != null) {
			List<LittleBox> tempBoxes = new ArrayList<>();
			for (SplitRangeBox range : ranges) {
				extractBox(range.x.min, range.y.min, range.z.min, range.x.max, range.y.max, range.z.max, tempBoxes);
				
				boolean cutted = false;
				outer_loop: for (LittleBox box : tempBoxes) {
					for (LittleBox cutBox : boxes) {
						if (cutBox.intersectsWith(box)) // This should also work for slices, since it's compressed by
						                                // the range
						{
							cutted = true;
							break outer_loop;
						}
					}
				}
				if (cutted)
					cutout.addAll(tempBoxes);
				else
					newBoxes.addAll(tempBoxes);
				tempBoxes.clear();
			}
		} else {
			boolean[][][] filled = new boolean[getSize(Axis.X)][getSize(Axis.Y)][getSize(Axis.Z)];
			
			for (LittleBox box : boxes) {
				box.fillInSpace(this, filled);
			}
			
			boolean expected = filled[0][0][0];
			boolean continuous = true;
			
			loop: for (int x = 0; x < filled.length; x++) {
				for (int y = 0; y < filled[x].length; y++) {
					for (int z = 0; z < filled[x][y].length; z++) {
						if (filled[x][y][z] != expected) {
							continuous = false;
							break loop;
						}
					}
				}
			}
			
			if (continuous) {
				if (expected) {
					cutout.add(this.copy());
					return new ArrayList<>();
				}
				newBoxes.add(this.copy());
				return newBoxes;
			}
			
			for (int x = 0; x < filled.length; x++) {
				for (int y = 0; y < filled[x].length; y++) {
					for (int z = 0; z < filled[x][y].length; z++) {
						LittleBox box = extractBox(x + minX, y + minY, z + minZ);
						if (box != null) {
							if (filled[x][y][z])
								cutout.add(box);
							else
								newBoxes.add(box);
						}
					}
				}
			}
		}
		
		BasicCombiner.combineBoxes(newBoxes);
		BasicCombiner.combineBoxes(cutout);
		
		return newBoxes;
	}
	
	/** @return all remaining boxes or null if the box remains as it is */
	public List<LittleBox> cutOut(LittleBox box) {
		if (intersectsWith(box)) {
			List<LittleBox> boxes = new ArrayList<>();
			
			if (getVolume() > secondMethodVolume && box.isCompletelyFilled()) {
				List<LittleBox> splitting = new ArrayList<>();
				splitting.add(box);
				
				List<LittleBox> tempBoxes = new ArrayList<>();
				for (SplitRangeBox range : split(splitting)) {
					extractBox(range.x.min, range.y.min, range.z.min, range.x.max, range.y.max, range.z.max, tempBoxes);
					
					boolean cutted = false;
					for (LittleBox tempBox : tempBoxes) {
						if (box.intersectsWith(tempBox)) // This should also work for slices, since it's compressed by
						                                 // the range
						{
							cutted = true;
							break;
						}
					}
					if (!cutted)
						boxes.addAll(tempBoxes);
					tempBoxes.clear();
				}
				
				return boxes;
			} else {
				LittleVec vec = new LittleVec(0, 0, 0);
				for (int littleX = minX; littleX < maxX; littleX++) {
					for (int littleY = minY; littleY < maxY; littleY++) {
						for (int littleZ = minZ; littleZ < maxZ; littleZ++) {
							vec.set(littleX, littleY, littleZ);
							if (!box.isVecInsideBox(box, vec))
								boxes.add(extractBox(littleX, littleY, littleZ));
						}
					}
				}
			}
			
			BasicCombiner.combineBoxes(boxes);
			
			return boxes;
		}
		
		return null;
	}
	
	protected boolean intersectsWith(LittleBox box) {
		return box.maxX > this.minX && box.minX < this.maxX && box.maxY > this.minY && box.minY < this.maxY && box.maxZ > this.minZ && box.minZ < this.maxZ;
	}
	
	public boolean containsBox(LittleBox box) {
		return this.minX <= box.minX && this.maxX >= box.maxX && this.minY <= box.minY && this.maxY >= box.maxY && this.minZ <= box.minZ && this.maxZ >= box.maxZ;
	}
	
	public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled) {
		boolean changed = false;
		int minX = Math.max(this.minX, otherBox.minX);
		int maxX = Math.min(this.maxX, otherBox.maxX);
		int minY = Math.max(this.minY, otherBox.minY);
		int maxY = Math.min(this.maxY, otherBox.maxY);
		int minZ = Math.max(this.minZ, otherBox.minZ);
		int maxZ = Math.min(this.maxZ, otherBox.maxZ);
		if (isCompletelyFilled()) {
			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					for (int z = minZ; z < maxZ; z++) {
						filled[x - otherBox.minX][y - otherBox.minY][z - otherBox.minZ] = true;
						changed = true;
					}
				}
			}
		} else {
			LittleVec vec = new LittleVec(0, 0, 0);
			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					for (int z = minZ; z < maxZ; z++) {
						vec.set(x, y, z);
						if (isVecInsideBox(otherBox, vec)) {
							filled[x - otherBox.minX][y - otherBox.minY][z - otherBox.minZ] = true;
							changed = true;
						}
					}
				}
			}
		}
		return changed;
	}
	
	// ================Vectors================
	
	public void add(int x, int y, int z) {
		minX += x;
		minY += y;
		minZ += z;
		maxX += x;
		maxY += y;
		maxZ += z;
	}
	
	public void add(LittleVec vec) {
		minX += vec.x;
		minY += vec.y;
		minZ += vec.z;
		maxX += vec.x;
		maxY += vec.y;
		maxZ += vec.z;
	}
	
	public void sub(int x, int y, int z) {
		minX -= x;
		minY -= y;
		minZ -= z;
		maxX -= x;
		maxY -= y;
		maxZ -= z;
	}
	
	public void sub(LittleVec vec) {
		minX -= vec.x;
		minY -= vec.y;
		minZ -= vec.z;
		maxX -= vec.x;
		maxY -= vec.y;
		maxZ -= vec.z;
	}
	
	public LittleVec getMinVec() {
		return new LittleVec(minX, minY, minZ);
	}
	
	public LittleVec getMaxVec() {
		return new LittleVec(maxX, maxY, maxZ);
	}
	
	public LittleVec getNearstedPointTo(LittleVec vec) {
		int x = minX;
		if (vec.x >= minX || vec.x <= maxX)
			x = vec.x;
		if (Math.abs(minX - x) > Math.abs(maxX - x))
			x = maxX;
		
		int y = minY;
		if (vec.y >= minY || vec.y <= maxY)
			y = vec.y;
		if (Math.abs(minY - y) > Math.abs(maxY - y))
			y = maxY;
		
		int z = minZ;
		if (vec.z >= minZ || vec.z <= maxZ)
			z = vec.z;
		if (Math.abs(minZ - z) > Math.abs(maxZ - z))
			z = maxZ;
		
		return new LittleVec(x, y, z);
	}
	
	public LittleVec getNearstedPointTo(LittleBox box) {
		int x = 0;
		if (minX >= box.minX && minX <= box.maxX)
			x = minX;
		else if (box.minX >= minX && box.minX <= box.maxX)
			x = box.minX;
		else if (Math.abs(minX - box.maxX) > Math.abs(maxX - box.minX))
			x = maxX;
		else
			x = minX;
		
		int y = 0;
		if (minY >= box.minY && minY <= box.maxY)
			y = minY;
		else if (box.minY >= minY && box.minY <= box.maxY)
			y = box.minY;
		else if (Math.abs(minY - box.maxY) > Math.abs(maxY - box.minY))
			y = maxY;
		else
			y = minY;
		
		int z = 0;
		if (minZ >= box.minZ && minZ <= box.maxZ)
			z = minZ;
		else if (box.minZ >= minZ && box.minZ <= box.maxZ)
			z = box.minZ;
		else if (Math.abs(minZ - box.maxZ) > Math.abs(maxZ - box.minZ))
			z = maxZ;
		else
			z = minZ;
		
		return new LittleVec(x, y, z);
	}
	
	public double distanceTo(LittleBox box) {
		return distanceTo(box.getNearstedPointTo(this));
	}
	
	public double distanceTo(LittleVec vec) {
		return this.getNearstedPointTo(vec).distanceTo(vec);
	}
	
	public boolean isVecInsideBox(int x, int y, int z) {
		return x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ;
	}
	
	public boolean isVecInsideBox(LittleBox box, LittleVec vec) {
		return isVecInsideBox(vec.x, vec.y, vec.z);
	}
	
	public boolean intersectsWithFace(EnumFacing facing, LittleVec vec, boolean completely) {
		Axis one = RotationUtils.getDifferentAxisFirst(facing.getAxis());
		Axis two = RotationUtils.getDifferentAxisFirst(facing.getAxis());
		return vec.get(one) >= getMin(one) && vec.get(one) <= getMax(one) && vec.get(two) >= getMin(two) && vec.get(two) <= getMax(two);
	}
	
	public boolean intersectsWithAxis(LittleGridContext context, Axis axis, Vec3d vec) {
		switch (axis) {
		case X:
			return intersectsWithYZ(context, vec);
		case Y:
			return intersectsWithXZ(context, vec);
		case Z:
			return intersectsWithXY(context, vec);
		}
		return false;
	}
	
	public boolean intersectsWithYZ(LittleGridContext context, Vec3d vec) {
		return vec.y >= context.toVanillaGrid(this.minY) && vec.y < context.toVanillaGrid(this.maxY) && vec.z >= context.toVanillaGrid(this.minZ) && vec.z < context.toVanillaGrid(this.maxZ);
	}
	
	public boolean intersectsWithXZ(LittleGridContext context, Vec3d vec) {
		return vec.x >= context.toVanillaGrid(this.minX) && vec.x < context.toVanillaGrid(this.maxX) && vec.z >= context.toVanillaGrid(this.minZ) && vec.z < context.toVanillaGrid(this.maxZ);
	}
	
	public boolean intersectsWithXY(LittleGridContext context, Vec3d vec) {
		return vec.x >= context.toVanillaGrid(this.minX) && vec.x < context.toVanillaGrid(this.maxX) && vec.y >= context.toVanillaGrid(this.minY) && vec.y < context.toVanillaGrid(this.maxY);
	}
	
	public LittleVec getCenter() {
		return new LittleVec((maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2);
	}
	
	@Nullable
	protected Vec3d collideWithPlane(LittleGridContext context, Axis axis, double value, Vec3d vecA, Vec3d vecB) {
		Vec3d vec3d = axis != Axis.X ? axis != Axis.Y ? vecA.getIntermediateWithZValue(vecB, value) : vecA.getIntermediateWithYValue(vecB, value) : vecA.getIntermediateWithXValue(vecB, value);
		return vec3d != null && intersectsWithAxis(context, axis, vec3d) ? vec3d : null;
	}
	
	@Nullable
	public RayTraceResult calculateIntercept(LittleGridContext context, BlockPos pos, Vec3d vecA, Vec3d vecB) {
		vecA = vecA.subtract(pos.getX(), pos.getY(), pos.getZ());
		vecB = vecB.subtract(pos.getX(), pos.getY(), pos.getZ());
		
		Vec3d collision = null;
		EnumFacing collided = null;
		
		for (EnumFacing facing : EnumFacing.VALUES) {
			Vec3d temp = collideWithPlane(context, facing.getAxis(), (double) getValueOfFacing(facing) / context.size, vecA, vecB);
			if (temp != null && isClosest(vecA, collision, temp)) {
				collided = facing;
				collision = temp;
			}
		}
		
		if (collision == null)
			return null;
		
		return new RayTraceResult(collision.addVector(pos.getX(), pos.getY(), pos.getZ()), collided, pos);
	}
	
	// ================Rotation & Flip================
	
	/** @param rotation
	 * @param doubledCenter
	 *            coordinates are doubled, meaning in order to get the correct
	 *            coordinates they have to be divided by two. This allows to rotate
	 *            around even axis. */
	public void rotateBox(Rotation rotation, LittleVec doubledCenter) {
		long tempMinX = minX * 2 - doubledCenter.x;
		long tempMinY = minY * 2 - doubledCenter.y;
		long tempMinZ = minZ * 2 - doubledCenter.z;
		long tempMaxX = maxX * 2 - doubledCenter.x;
		long tempMaxY = maxY * 2 - doubledCenter.y;
		long tempMaxZ = maxZ * 2 - doubledCenter.z;
		resort((int) ((rotation.getMatrix().getX(tempMinX, tempMinY, tempMinZ) + doubledCenter.x) / 2), (int) ((rotation.getMatrix().getY(tempMinX, tempMinY, tempMinZ) + doubledCenter.y) / 2), (int) ((rotation.getMatrix().getZ(tempMinX, tempMinY, tempMinZ) + doubledCenter.z) / 2), (int) ((rotation.getMatrix().getX(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.x) / 2), (int) ((rotation.getMatrix().getY(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.y) / 2), (int) ((rotation.getMatrix().getZ(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.z) / 2));
	}
	
	/** @param axis
	 * @param doubledCenter
	 *            coordinates are doubled, meaning in order to get the correct
	 *            coordinates they have to be divided by two. This allows to flip
	 *            around even axis. */
	public void flipBox(Axis axis, LittleVec doubledCenter) {
		long tempMin = getMin(axis) * 2 - doubledCenter.get(axis);
		long tempMax = getMax(axis) * 2 - doubledCenter.get(axis);
		int min = (int) ((doubledCenter.get(axis) - tempMin) / 2);
		int max = (int) ((doubledCenter.get(axis) - tempMax) / 2);
		setMin(axis, Math.min(min, max));
		setMax(axis, Math.max(min, max));
	}
	
	// ================Basic Object Overrides================
	
	@Override
	public int hashCode() {
		return minX + minY + minZ + maxX + maxY + maxZ;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof LittleBox)
			return object.getClass() == this.getClass() && minX == ((LittleBox) object).minX && minY == ((LittleBox) object).minY && minZ == ((LittleBox) object).minZ && maxX == ((LittleBox) object).maxX && maxY == ((LittleBox) object).maxY && maxZ == ((LittleBox) object).maxZ;
		return super.equals(object);
	}
	
	@Override
	public String toString() {
		return "[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "]";
	}
	
	// ================Special methods================
	
	public LittleBox extractBox(int x, int y, int z) {
		return new LittleBox(x, y, z, x + 1, y + 1, z + 1);
	}
	
	public List<LittleBox> extractBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, List<LittleBox> boxes) {
		boxes.add(new LittleBox(minX, minY, minZ, maxX, maxY, maxZ));
		return boxes;
	}
	
	public LittleBox copy() {
		return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public boolean isFaceAtEdge(LittleGridContext context, EnumFacing facing) {
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			return getMax(facing.getAxis()) == context.size;
		else
			return getMin(facing.getAxis()) == 0;
	}
	
	public LittleBox grow(EnumFacing facing) {
		Axis axis = facing.getAxis();
		LittleBox result = this.copy();
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			result.setMax(axis, getMax(axis) + 1);
		else
			result.setMin(axis, getMin(axis) - 1);
		return result;
	}
	
	public LittleBox shrink(EnumFacing facing, boolean toLimit) {
		Axis axis = facing.getAxis();
		if (getSize(axis) > 1) {
			LittleBox result = this.copy();
			if (facing.getAxisDirection() == AxisDirection.POSITIVE)
				result.setMax(axis, toLimit ? getMin(axis) + 1 : getMax(axis) - 1);
			else
				result.setMin(axis, toLimit ? getMax(axis) - 1 : getMin(axis) + 1);
			return result;
		}
		return null;
	}
	
	public void resort() {
		set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}
	
	public void resort(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}
	
	public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	// ================Rendering================
	
	@SideOnly(Side.CLIENT)
	public LittleRenderingCube getRenderingCube(LittleGridContext context, Block block, int meta) {
		return getRenderingCube(context, this.getCube(context), block, meta);
	}
	
	@SideOnly(Side.CLIENT)
	public LittleRenderingCube getRenderingCube(LittleGridContext context, CubeObject cube, Block block, int meta) {
		return new LittleRenderingCube(cube, this, block, meta);
	}
	
	// ================Faces================
	
	@Nullable
	public LittleTileFace getFace(LittleGridContext context, EnumFacing facing) {
		Axis one = RotationUtils.getDifferentAxisFirst(facing.getAxis());
		Axis two = RotationUtils.getDifferentAxisSecond(facing.getAxis());
		
		return new LittleTileFace(this, context, facing, getMin(one), getMin(two), getMax(one), getMax(two), facing.getAxisDirection() == AxisDirection.POSITIVE ? getMax(facing.getAxis()) : getMin(facing.getAxis()));
	}
	
	public boolean intersectsWith(LittleTileFace face) {
		return (face.face.getAxisDirection() == AxisDirection.POSITIVE ? getMin(face.face.getAxis()) : getMax(face.face.getAxis())) == face.origin && face.maxOne > getMin(face.one) && face.minOne < getMax(face.one) && face.maxTwo > getMin(face.two) && face.minTwo < getMax(face.two);
	}
	
	public boolean isFaceSolid(EnumFacing facing) {
		return true;
	}
	
	public boolean canFaceBeCombined(LittleBox other) {
		return true;
	}
	
	public void fill(LittleTileFace face) {
		if (intersectsWith(face)) {
			int minOne = Math.max(getMin(face.one), face.minOne);
			int maxOne = Math.min(getMax(face.one), face.maxOne);
			int minTwo = Math.max(getMin(face.two), face.minTwo);
			int maxTwo = Math.min(getMax(face.two), face.maxTwo);
			if (isCompletelyFilled()) {
				for (int one = minOne; one < maxOne; one++) {
					for (int two = minTwo; two < maxTwo; two++) {
						face.filled[one - face.minOne][two - face.minTwo] = true;
					}
				}
			} else {
				boolean completely = !canFaceBeCombined(face.getBox());
				int min = getValueOfFacing(face.face.getOpposite());
				if (face.face.getAxisDirection() == AxisDirection.NEGATIVE)
					min--;
				LittleVec vec = new LittleVec(min, min, min);
				for (int one = minOne; one < maxOne; one++) {
					for (int two = minTwo; two < maxTwo; two++) {
						vec.set(face.one, one);
						vec.set(face.two, two);
						if (intersectsWithFace(face.face.getOpposite(), vec, completely)) // isVecInsideBox(vec))
							face.filled[one - face.minOne][two - face.minTwo] = true;
					}
				}
			}
		}
	}
	
	public static class LittleTileFace {
		public LittleGridContext context;
		public LittleBox box;
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
		
		public void ensureContext(LittleGridContext context) {
			if (context == this.context || this.context.size > context.size)
				return;
			
			int ratio = context.size / this.context.size;
			this.minOne *= ratio;
			this.minTwo *= ratio;
			this.maxOne *= ratio;
			this.maxTwo *= ratio;
			this.origin *= ratio;
			this.oldOrigin *= ratio;
			box = box.copy(); // Make sure the original one will not be modified
			box.convertTo(this.context, context);
			this.context = context;
			filled = new boolean[maxOne - minOne][maxTwo - minTwo];
		}
		
		public LittleTileFace(LittleBox box, LittleGridContext context, EnumFacing face, int minOne, int minTwo, int maxOne, int maxTwo, int origin) {
			this.box = box;
			this.context = context;
			this.face = face;
			this.one = RotationUtils.getDifferentAxisFirst(face.getAxis());
			this.two = RotationUtils.getDifferentAxisSecond(face.getAxis());
			this.minOne = minOne;
			this.minTwo = minTwo;
			this.maxOne = maxOne;
			this.maxTwo = maxTwo;
			this.origin = origin;
			this.oldOrigin = origin;
			filled = new boolean[maxOne - minOne][maxTwo - minTwo];
		}
		
		public boolean isFilled() {
			int min = oldOrigin;
			if (face.getAxisDirection() == AxisDirection.POSITIVE)
				min--;
			LittleVec vec = new LittleVec(min, min, min);
			for (int one = 0; one < filled.length; one++) {
				for (int two = 0; two < filled[one].length; two++) {
					vec.set(this.one, minOne + one);
					vec.set(this.two, minTwo + two);
					if (!filled[one][two] && box.intersectsWithFace(face, vec, false)) // &&
					                                                                   // LittleTileBox.this.isVecInsideBox(vec))
						return false;
				}
			}
			return true;
		}
		
		public LittleBox getBox() {
			return box;
		}
		
		public boolean isFaceInsideBlock() {
			return origin > 0 && origin < context.maxPos;
		}
		
		public void move(EnumFacing facing) {
			origin = face.getAxisDirection() == AxisDirection.POSITIVE ? 0 : context.maxPos;
		}
	}
	
	// ================Identifier================
	
	public int[] getIdentifier() {
		return new int[] { minX, minY, minZ };
	}
	
	public boolean is(int[] identifier) {
		if (identifier.length == 3)
			return identifier[0] == minX && identifier[1] == minY && identifier[2] == minZ;
		return false;
	}
	
	// ================Static Helpers================
	
	public static LittleBox loadBox(String name, NBTTagCompound nbt) {
		if (nbt.getTag(name + "minX") instanceof NBTTagByte) // very old pre 1.0.0
		{
			LittleBox box = new LittleBox(nbt.getByte(name + "minX"), nbt.getByte(name + "minY"), nbt.getByte(name + "minZ"), nbt.getByte(name + "maxX"), nbt.getByte(name + "maxY"), nbt.getByte(name + "maxZ"));
			nbt.removeTag(name + "minX");
			nbt.removeTag(name + "minY");
			nbt.removeTag(name + "minZ");
			nbt.removeTag(name + "maxX");
			nbt.removeTag(name + "maxY");
			nbt.removeTag(name + "maxZ");
			box.writeToNBT(name, nbt);
			return box;
		} else if (nbt.getTag(name + "minX") instanceof NBTTagInt) // old pre 1.3.0
		{
			LittleBox box = new LittleBox(nbt.getInteger(name + "minX"), nbt.getInteger(name + "minY"), nbt.getInteger(name + "minZ"), nbt.getInteger(name + "maxX"), nbt.getInteger(name + "maxY"), nbt.getInteger(name + "maxZ"));
			nbt.removeTag(name + "minX");
			nbt.removeTag(name + "minY");
			nbt.removeTag(name + "minZ");
			nbt.removeTag(name + "maxX");
			nbt.removeTag(name + "maxY");
			nbt.removeTag(name + "maxZ");
			box.writeToNBT(name, nbt);
			return box;
		} else if (nbt.getTag(name) instanceof NBTTagIntArray) { // New
			return createBox(nbt.getIntArray(name));
		} else if (nbt.getTag(name) instanceof NBTTagString) { // Not used anymore pre 1.5.0
			String[] coords = nbt.getString(name).split("\\.");
			try {
				return new LittleBox(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]), Integer.parseInt(coords[4]), Integer.parseInt(coords[5]));
			} catch (Exception e) {
				
			}
		}
		return new LittleBox(0, 0, 0, 0, 0, 0);
	}
	
	public static LittleBox createBox(int[] array) {
		switch (array.length) {
		case 6:
			return new LittleBox(array[0], array[1], array[2], array[3], array[4], array[5]);
		case 7:
			return new LittleSlicedOrdinaryBox(array[0], array[1], array[2], array[3], array[4], array[5], LittleSlice.getSliceByID(array[6]));
		case 11:
			return new LittleSlicedBox(array[0], array[1], array[2], array[3], array[4], array[5], LittleSlice.getSliceByID(array[6]), Float.intBitsToFloat(array[7]), Float.intBitsToFloat(array[8]), Float.intBitsToFloat(array[9]), Float.intBitsToFloat(array[10]));
		default:
			throw new InvalidParameterException("No valid coords given " + Arrays.toString(array));
		}
	}
	
	public static void combineBoxesBlocks(LittleBoxes boxes) {
		combineBoxesBlocks(boxes.context, boxes);
	}
	
	public static void combineBoxesBlocks(LittleGridContext context, List<LittleBox> boxes) {
		HashMapList<BlockPos, LittleBox> chunked = new HashMapList<>();
		for (int i = 0; i < boxes.size(); i++) {
			chunked.add(boxes.get(i).getMinVec().getBlockPos(context), boxes.get(i));
		}
		boxes.clear();
		BasicCombiner combiner = new BasicCombiner(new ArrayList<>());
		for (Iterator<ArrayList<LittleBox>> iterator = chunked.values().iterator(); iterator.hasNext();) {
			ArrayList<LittleBox> list = iterator.next();
			combiner.set(list);
			combiner.combine();
			boxes.addAll(list);
		}
	}
	
	public static boolean isClosest(Vec3d from, @Nullable Vec3d optional, Vec3d toCheck) {
		return optional == null || from.squareDistanceTo(toCheck) < from.squareDistanceTo(optional);
	}
	
	public static boolean intersectsWith(LittleBox box, LittleBox box2) {
		if (box.getClass() == LittleBox.class)
			return box2.intersectsWith(box);
		return box.intersectsWith(box2);
	}
}
