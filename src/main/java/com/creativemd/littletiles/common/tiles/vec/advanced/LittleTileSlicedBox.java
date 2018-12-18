package com.creativemd.littletiles.common.tiles.vec.advanced;

import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils.BoxCorner;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.math.vec.Ray2d;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.client.tiles.LittleSlicedRenderingCube;
import com.creativemd.littletiles.common.tiles.combine.BasicCombiner;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.tiles.vec.LittleUtils;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileSlicedBox extends LittleTileSlicedOrdinaryBox {
	
	/** start vec (startOne/startTwo) of the slice. Relative to the min vec of the
	 * box. One is the first different axis from slice.axis */
	public float startOne;
	/** start vec (startOne/startTwo) of the slice. Relative to the min vec of the
	 * box. Two is the second different axis from slice.axis */
	public float startTwo;
	/** end vec (endOne/endTwo) of the slice. Relative to the min vec of the box. One
	 * is the first different axis from slice.axis */
	public float endOne;
	/** end vec (endOne/endTwo) of the slice. Relative to the min vec of the box. Two
	 * is the second different axis from slice.axis */
	public float endTwo;
	
	// ================Constructors================
	
	public LittleTileSlicedBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, LittleSlice slice, float startOne, float startTwo, float endOne, float endTwo) {
		super(minX, minY, minZ, maxX, maxY, maxZ, slice);
		this.startOne = startOne;
		this.startTwo = startTwo;
		this.endOne = endOne;
		this.endTwo = endTwo;
	}
	
	public LittleTileSlicedBox(LittleTileBox box, LittleSlice slice, float startOne, float startTwo, float endOne, float endTwo) {
		this(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, slice, startOne, startTwo, endOne, endTwo);
	}
	
	// ================Conversions================
	
	@Override
	public void addCollisionBoxes(LittleGridContext context, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, BlockPos offset) {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		Vector3d min = new Vector3d(minX, minY, minZ);
		Vector3d max = new Vector3d(maxX, maxY, maxZ);
		
		RotationUtils.setValue(min, getMinSlice(one), one);
		RotationUtils.setValue(min, getMinSlice(two), two);
		
		RotationUtils.setValue(max, getMaxSlice(one), one);
		RotationUtils.setValue(max, getMaxSlice(two), two);
		
		AxisAlignedBB bb = new AxisAlignedBBOrdinarySliced(context.toVanillaGrid(min.x) + offset.getX(), context.toVanillaGrid(min.y) + offset.getY(), context.toVanillaGrid(min.z) + offset.getZ(), context.toVanillaGrid(max.x) + offset.getX(), context.toVanillaGrid(max.y) + offset.getY(), context.toVanillaGrid(max.z) + offset.getZ(), slice);
		
		if (entityBox.intersects(bb))
			collidingBoxes.add(bb);
		
		boolean boxTwo = hasAdditionalBoxTwo();
		
		if (hasAdditionalBoxOne()) {
			if (slice.isFacingPositive(one)) {
				RotationUtils.setValue(min, getMin(one), one);
				RotationUtils.setValue(max, getMinSlice(one), one);
			} else {
				RotationUtils.setValue(min, getMaxSlice(one), one);
				RotationUtils.setValue(max, getMax(one), one);
			}
			
			if (slice.isFacingPositive(two)) {
				RotationUtils.setValue(min, boxTwo ? getMin(two) : getMinSlice(two), two);
				RotationUtils.setValue(max, getMaxSlice(two), two);
			} else {
				RotationUtils.setValue(min, getMinSlice(two), two);
				RotationUtils.setValue(max, boxTwo ? getMax(two) : getMaxSlice(two), two);
			}
			
			bb = new AxisAlignedBB(context.toVanillaGrid(min.x) + offset.getX(), context.toVanillaGrid(min.y) + offset.getY(), context.toVanillaGrid(min.z) + offset.getZ(), context.toVanillaGrid(max.x) + offset.getX(), context.toVanillaGrid(max.y) + offset.getY(), context.toVanillaGrid(max.z) + offset.getZ());
			if (entityBox.intersects(bb))
				collidingBoxes.add(bb);
		}
		
		if (boxTwo) {
			if (slice.isFacingPositive(one)) {
				RotationUtils.setValue(min, getMinSlice(one), one);
				RotationUtils.setValue(max, getMaxSlice(one), one);
			} else {
				RotationUtils.setValue(min, getMinSlice(one), one);
				RotationUtils.setValue(max, getMaxSlice(one), one);
			}
			
			if (slice.isFacingPositive(two)) {
				RotationUtils.setValue(min, getMin(two), two);
				RotationUtils.setValue(max, getMinSlice(two), two);
			} else {
				RotationUtils.setValue(min, getMaxSlice(two), two);
				RotationUtils.setValue(max, getMax(two), two);
			}
			
			bb = new AxisAlignedBB(context.toVanillaGrid(min.x) + offset.getX(), context.toVanillaGrid(min.y) + offset.getY(), context.toVanillaGrid(min.z) + offset.getZ(), context.toVanillaGrid(max.x) + offset.getX(), context.toVanillaGrid(max.y) + offset.getY(), context.toVanillaGrid(max.z) + offset.getZ());
			if (entityBox.intersects(bb))
				collidingBoxes.add(bb);
		}
	}
	
	// ================Save================
	
	@Override
	public int[] getArray() {
		return new int[] { minX, minY, minZ, maxX, maxY, maxZ, slice.getSliceID(), Float.floatToIntBits(startOne), Float.floatToIntBits(startTwo), Float.floatToIntBits(endOne), Float.floatToIntBits(endTwo) };
	}
	
	// ================Size & Volume================
	
	@Override
	public void convertTo(LittleGridContext from, LittleGridContext to) {
		super.convertTo(from, to);
		if (from.size > to.size) {
			int ratio = from.size / to.size;
			startOne /= ratio;
			startTwo /= ratio;
			endOne /= ratio;
			endTwo /= ratio;
		} else {
			int ratio = to.size / from.size;
			startOne *= ratio;
			startTwo *= ratio;
			endOne *= ratio;
			endTwo *= ratio;
		}
	}
	
	@Override
	public void convertTo(int from, int to) {
		super.convertTo(from, to);
		if (from > to) {
			int ratio = from / to;
			startOne /= ratio;
			startTwo /= ratio;
			endOne /= ratio;
			endTwo /= ratio;
		} else {
			int ratio = to / from;
			startOne *= ratio;
			startTwo *= ratio;
			endOne *= ratio;
			endTwo *= ratio;
		}
	}
	
	@Override
	public double getVolume() {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		return getSize(slice.axis) * (slice.isFacingPositive(one) ? getMaxSlice(one) - getMin(one) : getMax(one) - getMinSlice(one)) * (slice.isFacingPositive(two) ? getMaxSlice(two) - getMin(two) : getMax(two) - getMinSlice(two)) - (Math.abs(startOne - endOne) * Math.abs(startTwo - endTwo) * getSize(slice.axis)) / 2D;
	}
	
	@Override
	public Vec3d getExactCorner(BoxCorner corner) {
		return new Vec3d(slice.isFacingPositive(Axis.X) == corner.isFacingPositive(Axis.X) ? getSliceCornerValue(corner, Axis.X) : getCornerX(corner), slice.isFacingPositive(Axis.Y) == corner.isFacingPositive(Axis.Y) ? getSliceCornerValue(corner, Axis.Y) : getCornerY(corner), slice.isFacingPositive(Axis.Z) == corner.isFacingPositive(Axis.Z) ? getSliceCornerValue(corner, Axis.Z) : getCornerZ(corner));
	}
	
	// ================Block Integration================
	
	@Override
	public LittleTileBox createOutsideBlockBox(LittleGridContext context, EnumFacing facing) {
		if (facing == slice.emptySideOne || facing == slice.emptySideTwo)
			return null;
		
		if (facing.getAxis() == slice.axis) {
			LittleTileSlicedBox box = this.copy();
			
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
		
		return super.createOutsideBlockBox(context, facing);
	}
	
	/*
	 * @Override public LittleTileSlicedBox createInsideBlockBox(EnumFacing facing)
	 * { Vec3i vec = facing.getDirectionVec(); return new LittleTileSlicedBox(minX -
	 * vec.getX() * LittleTile.gridSize, minY - vec.getY() * LittleTile.gridSize,
	 * minZ - vec.getZ() * LittleTile.gridSize, maxX - vec.getX() *
	 * LittleTile.gridSize, maxY - vec.getY() * LittleTile.gridSize, maxZ -
	 * vec.getZ() * LittleTile.gridSize, slice, startOne, startTwo, endOne, endTwo);
	 * }
	 */
	
	// ================Box to box================
	
	@Override
	public LittleTileBox combineBoxes(LittleTileBox box, BasicCombiner combiner) {
		if (box instanceof LittleTileSlicedOrdinaryBox && ((LittleTileSlicedOrdinaryBox) box).slice == slice) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			if (!LittleUtils.equals(getSliceAngle(one, two), ((LittleTileSlicedOrdinaryBox) box).getSliceAngle(one, two)))
				return null;
			
			EnumFacing facing = sharedBoxFace(box);
			if (facing != null) {
				Axis axis = facing.getAxis();
				
				if (((LittleTileSlicedOrdinaryBox) box).isOrdinary())
					return null;
				
				if (axis == slice.axis) {
					LittleTileSlicedBox slicedBox = (LittleTileSlicedBox) box;
					
					if (!LittleUtils.equals(getMaxSlice(one), slicedBox.getMaxSlice(one)) || !LittleUtils.equals(getMinSlice(one), slicedBox.getMinSlice(one)) || !LittleUtils.equals(getMaxSlice(two), slicedBox.getMaxSlice(two)) || !LittleUtils.equals(getMinSlice(two), slicedBox.getMinSlice(two)))
						return null;
					LittleTileBox newBox = copy();
					if (facing.getAxisDirection() != AxisDirection.POSITIVE)
						newBox.setMax(slice.axis, box.getMax(slice.axis));
					else
						newBox.setMin(slice.axis, box.getMin(slice.axis));
					return newBox;
				}
				
				LittleTileSlicedBox sliceBox = (LittleTileSlicedBox) box;
				
				Axis other = axis == one ? two : one;
				boolean facePositive = facing.getAxisDirection() == AxisDirection.POSITIVE;
				// if(facePositive ? LittleUtils.equals(getMaxSlice(axis), getMax(axis)) :
				// LittleUtils.equals(getMinSlice(axis), getMin(axis)) &&
				// !facePositive ? LittleUtils.equals(sliceBox.getMaxSlice(axis),
				// sliceBox.getMax(axis)) : LittleUtils.equals(sliceBox.getMinSlice(axis),
				// sliceBox.getMin(axis)))
				if (!facePositive ? LittleUtils.equals(getMaxSlice(axis), sliceBox.getMinSlice(axis)) : LittleUtils.equals(getMinSlice(axis), sliceBox.getMaxSlice(axis))) // &&
				// !facePositive ? LittleUtils.equals(sliceBox.getMaxSlice(axis),
				// sliceBox.getMax(axis)) : LittleUtils.equals(sliceBox.getMinSlice(axis),
				// sliceBox.getMin(axis)))
				{
					if (slice.start.isFacingPositive(axis) == facePositive) {
						if (!LittleUtils.equals((facePositive ? getStart(other) : getEnd(other)), (!facePositive ? sliceBox.getStart(other) : sliceBox.getEnd(other))))
							return null;
					} else {
						if (!LittleUtils.equals((!facePositive ? getStart(other) : getEnd(other)), (facePositive ? sliceBox.getStart(other) : sliceBox.getEnd(other))))
							return null;
					}
					
					double startOne = slice.start.isFacingPositive(one) ? Math.max(getMaxSlice(one), sliceBox.getMaxSlice(one)) : Math.min(getMinSlice(one), sliceBox.getMinSlice(one));
					double startTwo = slice.start.isFacingPositive(two) ? Math.max(getMaxSlice(two), sliceBox.getMaxSlice(two)) : Math.min(getMinSlice(two), sliceBox.getMinSlice(two));
					double endOne = slice.end.isFacingPositive(one) ? Math.max(getMaxSlice(one), sliceBox.getMaxSlice(one)) : Math.min(getMinSlice(one), sliceBox.getMinSlice(one));
					double endTwo = slice.end.isFacingPositive(two) ? Math.max(getMaxSlice(two), sliceBox.getMaxSlice(two)) : Math.min(getMinSlice(two), sliceBox.getMinSlice(two));
					
					LittleTileSlicedBox newBox = copy();
					/*
					 * if((facing.getAxis() == Axis.Y) == (facing.getAxisDirection() ==
					 * AxisDirection.POSITIVE)) newBox.setMax(axis, box.getMax(axis)); else
					 * newBox.setMin(axis, box.getMin(axis));
					 */
					newBox.minX = Math.min(newBox.minX, box.minX);
					newBox.minY = Math.min(newBox.minY, box.minY);
					newBox.minZ = Math.min(newBox.minZ, box.minZ);
					newBox.maxX = Math.max(newBox.maxX, box.maxX);
					newBox.maxY = Math.max(newBox.maxY, box.maxY);
					newBox.maxZ = Math.max(newBox.maxZ, box.maxZ);
					
					newBox.startOne = (float) (startOne - newBox.getMin(one));
					newBox.startTwo = (float) (startTwo - newBox.getMin(two));
					newBox.endOne = (float) (endOne - newBox.getMin(one));
					newBox.endTwo = (float) (endTwo - newBox.getMin(two));
					
					if (newBox.getMinSlice(one) == 0 && newBox.getMinSlice(two) == 0 && newBox.getMaxSlice(one) == newBox.getSize(one) && newBox.getMaxSlice(two) == newBox.getSize(two))
						return new LittleTileSlicedOrdinaryBox(newBox, slice);
					else
						return newBox;
				}
				
				return null;
			} else if (getMin(slice.axis) == box.getMin(slice.axis) && getMax(slice.axis) == box.getMax(slice.axis)) {
				LittleTileSlicedOrdinaryBox slicedBox = (LittleTileSlicedOrdinaryBox) box;
				
				double minSliceOne = slicedBox.isOrdinary() ? slicedBox.getMin(one) : ((LittleTileSlicedBox) slicedBox).getMinSlice(one);
				double minSliceTwo = slicedBox.isOrdinary() ? slicedBox.getMin(two) : ((LittleTileSlicedBox) slicedBox).getMinSlice(two);
				double maxSliceOne = slicedBox.isOrdinary() ? slicedBox.getMax(one) : ((LittleTileSlicedBox) slicedBox).getMaxSlice(one);
				double maxSliceTwo = slicedBox.isOrdinary() ? slicedBox.getMax(two) : ((LittleTileSlicedBox) slicedBox).getMaxSlice(two);
				
				boolean shareOnePostive = LittleUtils.equals(getMaxSlice(one), minSliceOne);
				boolean shareOneNegative = LittleUtils.equals(getMinSlice(one), maxSliceOne);
				boolean shareTwoPostive = LittleUtils.equals(getMaxSlice(two), minSliceTwo);
				boolean shareTwoNegative = LittleUtils.equals(getMinSlice(two), maxSliceTwo);
				
				if ((shareOnePostive ^ shareOneNegative) && (shareTwoPostive ^ shareTwoNegative)) {
					boolean postiveOne = slice.isFacingPositive(one);
					boolean postiveTwo = slice.isFacingPositive(two);
					
					if ((postiveOne == shareOnePostive && postiveTwo == shareTwoPostive) || (postiveOne != shareOnePostive && postiveTwo != shareTwoPostive))
						return null;
					
					/*
					 * if(shareOnePostive ? LittleUtils.equals(getMaxSlice(one), getMax(one)) :
					 * LittleUtils.equals(getMinSlice(one), getMin(one)) || shareTwoPostive ?
					 * !LittleUtils.equals(getMaxSlice(two), getMax(two)) :
					 * !LittleUtils.equals(getMinSlice(two), getMin(two))) return null;
					 * 
					 * if(slicedBox.isOrdinary() || (!shareOnePostive ?
					 * !LittleUtils.equals(maxSliceOne, box.getMax(one)) :
					 * !LittleUtils.equals(minSliceOne, box.getMin(one))) || !shareTwoPostive ?
					 * !LittleUtils.equals(maxSliceTwo, box.getMax(two)) :
					 * !LittleUtils.equals(minSliceTwo, box.getMin(two))) return null;
					 */
					
					/*
					 * if(slicedBox.isOrdinary() || (shareOnePostive ?
					 * LittleUtils.equals(getMaxSlice(one), getMax(one)) :
					 * LittleUtils.equals(getMinSlice(one), getMin(one)) && !shareOnePostive ?
					 * LittleUtils.equals(maxSliceOne, box.getMax(one)) :
					 * LittleUtils.equals(minSliceOne, box.getMin(one))) && shareTwoPostive ?
					 * LittleUtils.equals(getMaxSlice(two), getMax(two)) :
					 * LittleUtils.equals(getMinSlice(two), getMin(two)) && !shareTwoPostive ?
					 * LittleUtils.equals(maxSliceTwo, box.getMax(two)) :
					 * LittleUtils.equals(minSliceTwo, box.getMin(two))) return null;
					 */
					
					LittleTileBox boxInBetween = new LittleTileBox(this);
					if (shareOnePostive != postiveOne) {
						boxInBetween.setMin(one, box.getMin(one));
						boxInBetween.setMax(one, box.getMax(one));
					} else {
						if (shareOnePostive)
							boxInBetween.setMax(one, box.getMin(one));
						else
							boxInBetween.setMin(one, box.getMax(one));
					}
					
					if (shareTwoPostive != postiveTwo) {
						boxInBetween.setMin(two, box.getMin(two));
						boxInBetween.setMax(two, box.getMax(two));
					} else {
						if (shareTwoPostive)
							boxInBetween.setMax(two, box.getMin(two));
						else
							boxInBetween.setMin(two, box.getMax(two));
					}
					
					if (combiner.cutOut(boxInBetween)) {
						LittleTileSlicedBox newBox = this.copy();
						newBox.minX = Math.min(newBox.minX, box.minX);
						newBox.minY = Math.min(newBox.minY, box.minY);
						newBox.minZ = Math.min(newBox.minZ, box.minZ);
						newBox.maxX = Math.max(newBox.maxX, box.maxX);
						newBox.maxY = Math.max(newBox.maxY, box.maxY);
						newBox.maxZ = Math.max(newBox.maxZ, box.maxZ);
						
						double startOne = slice.start.isFacingPositive(one) ? Math.max(getMaxSlice(one), maxSliceOne) : Math.min(getMinSlice(one), minSliceOne);
						double startTwo = slice.start.isFacingPositive(two) ? Math.max(getMaxSlice(two), maxSliceTwo) : Math.min(getMinSlice(two), minSliceTwo);
						double endOne = slice.end.isFacingPositive(one) ? Math.max(getMaxSlice(one), maxSliceOne) : Math.min(getMinSlice(one), minSliceOne);
						double endTwo = slice.end.isFacingPositive(two) ? Math.max(getMaxSlice(two), maxSliceTwo) : Math.min(getMinSlice(two), minSliceTwo);
						
						newBox.startOne = (float) (startOne - newBox.getMin(one));
						newBox.startTwo = (float) (startTwo - newBox.getMin(two));
						newBox.endOne = (float) (endOne - newBox.getMin(one));
						newBox.endTwo = (float) (endTwo - newBox.getMin(two));
						
						if (newBox.getMinSlice(one) == 0 && newBox.getMinSlice(two) == 0 && newBox.getMaxSlice(one) == newBox.getSize(one) && newBox.getMaxSlice(two) == newBox.getSize(two))
							return new LittleTileSlicedOrdinaryBox(newBox, slice);
						else
							return newBox;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	@Nullable
	public EnumFacing sharedBoxFace(LittleTileBox box) {
		boolean x = slice.axis == Axis.X ? this.minX == box.minX && this.maxX == box.maxX : slice.isFacingPositive(Axis.X) ? this.minX == box.minX : this.maxX == box.maxX;
		boolean y = slice.axis == Axis.Y ? this.minY == box.minY && this.maxY == box.maxY : slice.isFacingPositive(Axis.Y) ? this.minY == box.minY : this.maxY == box.maxY;
		boolean z = slice.axis == Axis.Z ? this.minZ == box.minZ && this.maxZ == box.maxZ : slice.isFacingPositive(Axis.Z) ? this.minZ == box.minZ : this.maxZ == box.maxZ;
		
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
	
	@Override
	public boolean isVecInsideBoxNoEdge(Vec3d vec) {
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;
		if (x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			double posOne = RotationUtils.get(one, x, y, z);
			double posTwo = RotationUtils.get(two, x, y, z);
			
			if (slice.isFacingPositive(one)) {
				if (posOne <= getMinSlice(one))
					return true;
			} else {
				if (posOne >= getMaxSlice(one))
					return true;
			}
			
			if (slice.isFacingPositive(two)) {
				if (posTwo <= getMinSlice(two))
					return true;
			} else {
				if (posTwo >= getMaxSlice(two))
					return true;
			}
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getSliceCornerValue(corner, one) - posOne);
			double difTwo = Math.abs(getSliceCornerValue(corner, two) - posTwo);
			float sizeOne = getSliceSize(one);
			float sizeTwo = getSliceSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff <= 1;
		}
		return false;
	}
	
	@Override
	protected boolean intersectsWithBetweenSliceAndBox(LittleTileBox box) {
		EnumFacing ignoreFace = RotationUtils.getFacing(slice.axis);
		
		Axis axisOne = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis axisTwo = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		// cube vectors
		BoxCorner cornerMin = BoxCorner.getCornerUnsorted(ignoreFace, slice.emptySideOne.getOpposite(), slice.emptySideTwo.getOpposite());
		BoxCorner cornerMax = BoxCorner.getCornerUnsorted(ignoreFace, slice.emptySideOne, slice.emptySideTwo);
		
		// vec triangle
		double pointOne = getSliceValueOfFacing(slice.getEmptySide(axisOne).getOpposite());
		double pointTwo = getSliceValueOfFacing(slice.getEmptySide(axisTwo).getOpposite());
		
		Vec3d minVec = box.getExactCorner(cornerMin);
		Vec3d maxVec = box.getExactCorner(cornerMax);
		
		// minVec.setAxis(slice.axis, getValueOfFacing(ignoreFace.getOpposite()));
		// maxVec.setAxis(slice.axis, getValueOfFacing(ignoreFace.getOpposite()));
		
		// check if point is inside triangle (both)
		if (isVecInsideBoxNoEdge(minVec))
			return true;
		
		if (isVecInsideBoxNoEdge(maxVec))
			return true;
		
		// pointing positive
		if (slice.getNormal()[axisOne.ordinal()] > 0) {
			// check axis one
			if (RotationUtils.get(axisOne, minVec) <= pointOne)
				return true;
		}
		// pointing negative
		else {
			// check axis one
			if (RotationUtils.get(axisOne, minVec) >= pointOne)
				return true;
		}
		
		// pointing positive
		if (slice.getNormal()[axisTwo.ordinal()] > 0) {
			// check axis one
			if (RotationUtils.get(axisTwo, minVec) <= pointTwo)
				return true;
		}
		// pointing negative
		else {
			// check axis one
			if (RotationUtils.get(axisTwo, minVec) >= pointTwo)
				return true;
		}
		
		return false;
	}
	
	// ================Vectors================
	
	@Override
	public boolean isVecInsideBoxRelative(Vec3d vec) {
		if (vec.x >= minX && vec.x < maxX && vec.y >= minY && vec.y < maxY && vec.z >= minZ && vec.z < maxZ) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			double posOne = RotationUtils.get(one, vec);
			double posTwo = RotationUtils.get(two, vec);
			
			if (slice.isFacingPositive(one)) {
				if (posOne <= getMinSlice(one))
					return true;
			} else {
				if (posOne >= getMaxSlice(one))
					return true;
			}
			
			if (slice.isFacingPositive(two)) {
				if (posTwo <= getMinSlice(two))
					return true;
			} else {
				if (posTwo >= getMaxSlice(two))
					return true;
			}
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getSliceCornerValue(corner, one) - posOne);
			double difTwo = Math.abs(getSliceCornerValue(corner, two) - posTwo);
			float sizeOne = getSliceSize(one);
			float sizeTwo = getSliceSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff <= 1;
		}
		return false;
	}
	
	@Override
	public boolean isVecInsideBox(int x, int y, int z) {
		if (x >= minX && x < maxX && y >= minY && y < maxY && z >= minZ && z < maxZ) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			int posOne = RotationUtils.get(one, x, y, z);
			int posTwo = RotationUtils.get(two, x, y, z);
			
			if (!slice.isFacingPositive(one))
				posOne = posOne + 1;
			
			if (!slice.isFacingPositive(two))
				posTwo = posTwo + 1;
			
			if (slice.isFacingPositive(one)) {
				if (posOne <= getMinSlice(one))
					return true;
			} else {
				if (posOne >= getMaxSlice(one))
					return true;
			}
			
			if (slice.isFacingPositive(two)) {
				if (posTwo <= getMinSlice(two))
					return true;
			} else {
				if (posTwo >= getMaxSlice(two))
					return true;
			}
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getSliceCornerValue(corner, one) - posOne);
			double difTwo = Math.abs(getSliceCornerValue(corner, two) - posTwo);
			double sizeOne = getSliceSize(one);
			double sizeTwo = getSliceSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			
			return sizeOne >= difOne && sizeTwo >= difTwo && (diff < 1 || LittleUtils.equals(diff, 1));
		}
		return false;
	}
	
	@Override
	public boolean intersectsWithFace(EnumFacing facing, LittleTileVec vec, boolean completely) {
		if (!super.intersectsWithFace(facing, vec, completely))
			return false;
		
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		LittleTileVec copy = vec.copy();
		
		if (facing.getAxis() == slice.axis) {
			if (completely == slice.isFacingPositive(one))
				copy.set(one, copy.get(one) + 1);
			
			if (completely == slice.isFacingPositive(two))
				copy.set(two, copy.get(two) + 1);
		} else {
			// Axis different = one == facing.getAxis() ? two : one;
			// if(completely == slice.isFacingPositive(different))
			// copy.setAxis(one, copy.getAxis(different)+1);
			
			// if(completely == slice.isFacingPositive(two))
			// copy.setAxis(two, copy.getAxis(two)+1);
			if (completely == slice.isFacingPositive(one))
				copy.set(one, copy.get(one) + 1);
			
			if (completely == slice.isFacingPositive(two))
				copy.set(two, copy.get(two) + 1);
		}
		return intersectsWithFaceRelative(facing, new Vec3d(copy.x, copy.y, copy.z));
	}
	
	public boolean intersectsWithFaceRelative(EnumFacing facing, Vec3d vec) {
		Axis axis = facing.getAxis();
		
		if (slice.axis == axis) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			double posOne = RotationUtils.get(one, vec);
			double posTwo = RotationUtils.get(two, vec);
			
			if (slice.isFacingPositive(one)) {
				if (posOne <= getMinSlice(one))
					return true;
			} else {
				if (posOne > getMaxSlice(one))
					return true;
			}
			
			if (slice.isFacingPositive(two)) {
				if (posTwo <= getMinSlice(two))
					return true;
			} else {
				if (posTwo > getMaxSlice(two))
					return true;
			}
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getSliceCornerValue(corner, one) - posOne);
			double difTwo = Math.abs(getSliceCornerValue(corner, two) - posTwo);
			double sizeOne = getSliceSize(one);
			double sizeTwo = getSliceSize(two);
			
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff <= 1;
		} else {
			Axis other = RotationUtils.getDifferentAxisFirst(slice.axis) == axis ? RotationUtils.getDifferentAxisSecond(slice.axis) : RotationUtils.getDifferentAxisFirst(slice.axis);
			
			if (slice.isFacingPositive(axis) == (facing.getAxisDirection() == AxisDirection.POSITIVE)) {
				if (slice.isFacingPositive(other))
					return RotationUtils.get(other, vec) <= getMinSlice(other);
				else
					return RotationUtils.get(other, vec) >= getMaxSlice(other);
			} else {
				if (slice.isFacingPositive(other))
					return RotationUtils.get(other, vec) <= getMaxSlice(other);
				else
					return RotationUtils.get(other, vec) >= getMinSlice(other);
			}
		}
		
	}
	
	public boolean intersectsWithFace(LittleGridContext context, EnumFacing facing, Vec3d vec) {
		Axis axis = facing.getAxis();
		
		if (slice.axis == axis) {
			
			switch (axis) {
			case X:
				if (!intersectsWithYZ(context, vec))
					return false;
				break;
			case Y:
				if (!intersectsWithXZ(context, vec))
					return false;
				break;
			case Z:
				if (!intersectsWithXY(context, vec))
					return false;
				break;
			}
			
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			double posOne = RotationUtils.get(one, vec);
			double posTwo = RotationUtils.get(two, vec);
			
			if (slice.isFacingPositive(one)) {
				if (posOne <= getMinSlice(one) / context.size)
					return true;
			} else {
				if (posOne > getMaxSlice(one) / context.size)
					return true;
			}
			
			if (slice.isFacingPositive(two)) {
				if (posTwo <= getMinSlice(two) / context.size)
					return true;
			} else {
				if (posTwo > getMaxSlice(two) / context.size)
					return true;
			}
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getSliceCornerValue(corner, one) / context.size - posOne);
			double difTwo = Math.abs(getSliceCornerValue(corner, two) / context.size - posTwo);
			double sizeOne = getSliceSize(one) / (double) context.size;
			double sizeTwo = getSliceSize(two) / (double) context.size;
			
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff <= 1;
		} else {
			if (!super.intersectsWithAxis(context, axis, vec))
				return false;
			
			Axis other = RotationUtils.getDifferentAxisFirst(slice.axis) == axis ? RotationUtils.getDifferentAxisSecond(slice.axis) : RotationUtils.getDifferentAxisFirst(slice.axis);
			
			if (slice.isFacingPositive(axis) == (facing.getAxisDirection() == AxisDirection.POSITIVE)) {
				if (slice.isFacingPositive(other))
					return RotationUtils.get(other, vec) <= getMinSlice(other) / context.size;
				else
					return RotationUtils.get(other, vec) > getMaxSlice(other) / context.size;
			} else {
				if (slice.isFacingPositive(other))
					return RotationUtils.get(other, vec) < getMaxSlice(other) / context.size;
				else
					return RotationUtils.get(other, vec) >= getMinSlice(other) / context.size;
			}
		}
		
	}
	
	@Nullable
	protected Vec3d collideWithPlane(LittleGridContext context, EnumFacing facing, double value, Vec3d vecA, Vec3d vecB) {
		Axis axis = facing.getAxis();
		Vec3d vec3d = axis != Axis.X ? axis != Axis.Y ? vecA.getIntermediateWithZValue(vecB, value) : vecA.getIntermediateWithYValue(vecB, value) : vecA.getIntermediateWithXValue(vecB, value);
		
		return vec3d != null && intersectsWithFace(context, facing, vec3d) ? vec3d : null;
	}
	
	@Override
	@Nullable
	public RayTraceResult calculateIntercept(LittleGridContext context, BlockPos pos, Vec3d vecA, Vec3d vecB) {
		vecA = vecA.subtract(pos.getX(), pos.getY(), pos.getZ());
		vecB = vecB.subtract(pos.getX(), pos.getY(), pos.getZ());
		
		Vec3d collision = null;
		EnumFacing collided = null;
		
		for (EnumFacing facing : EnumFacing.VALUES) {
			Vec3d temp = collideWithPlane(context, facing, (double) getValueOfFacing(facing) / context.size, vecA, vecB);
			if (temp != null && isClosest(vecA, collision, temp)) {
				collided = facing;
				collision = temp;
			}
		}
		
		EnumFacing diagonal = slice.getPreferedSide(getSizeOfSlice());
		Vector3d sliceCorner = getSliceCorner(slice.start);
		Vec3d temp = linePlaneIntersection(new Vec3d(sliceCorner.x / context.size, sliceCorner.y / context.size, sliceCorner.z / context.size), getSliceNormal(), vecA, vecB.subtract(vecA));
		if (temp != null) {
			boolean inside = false; /*
			                         * temp.x >= LittleUtils.toGrid(minX) && temp.x < LittleUtils.toGrid(maxX) &&
			                         * temp.y >= LittleUtils.toGrid(minY) && temp.y < LittleUtils.toGrid(maxY) &&
			                         * temp.z >= LittleUtils.toGrid(minZ) && temp.z < LittleUtils.toGrid(maxZ);
			                         */
			switch (diagonal.getAxis()) {
			case X:
				inside = intersectsWithYZ(context, temp);
				break;
			case Y:
				inside = intersectsWithXZ(context, temp);
				break;
			case Z:
				inside = intersectsWithXY(context, temp);
				break;
			}
			// if(temp != null && intersectsWithAxis(diagonal.getAxis(), temp) &&
			// isClosest(vecA, collision, temp))
			if (inside && isClosest(vecA, collision, temp)) {
				collision = temp;
				collided = diagonal;
			}
		}
		
		if (collision == null)
			return null;
		
		return new RayTraceResult(collision.addVector(pos.getX(), pos.getY(), pos.getZ()), collided, pos);
	}
	
	// ================Rotation & Flip================
	
	@Override
	public void rotateBox(Rotation rotation, LittleTileVec doubledCenter) {
		Axis beforeOne = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis beforeTwo = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		Vector3d start = new Vector3d(0, 0, 0);
		Vector3d end = new Vector3d(0, 0, 0);
		
		RotationUtils.setValue(start, (startOne + getMin(beforeOne)) * 2, beforeOne);
		RotationUtils.setValue(start, (startTwo + getMin(beforeTwo)) * 2, beforeTwo);
		RotationUtils.setValue(end, (endOne + getMin(beforeOne)) * 2, beforeOne);
		RotationUtils.setValue(end, (endTwo + getMin(beforeTwo)) * 2, beforeTwo);
		
		LittleSlice before = slice;
		
		super.rotateBox(rotation, doubledCenter);
		
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		start.x -= doubledCenter.x;
		start.y -= doubledCenter.y;
		start.z -= doubledCenter.z;
		
		end.x -= doubledCenter.x;
		end.y -= doubledCenter.y;
		end.z -= doubledCenter.z;
		
		RotationUtils.rotate(start, rotation);
		RotationUtils.rotate(end, rotation);
		beforeOne = RotationUtils.rotate(beforeOne, rotation);
		beforeTwo = RotationUtils.rotate(beforeTwo, rotation);
		
		start.x += doubledCenter.x;
		start.y += doubledCenter.y;
		start.z += doubledCenter.z;
		
		end.x += doubledCenter.x;
		end.y += doubledCenter.y;
		end.z += doubledCenter.z;
		
		RotationUtils.setValue(start, RotationUtils.get(beforeOne, start) - getMin(beforeOne) * 2, beforeOne);
		RotationUtils.setValue(start, RotationUtils.get(beforeTwo, start) - getMin(beforeTwo) * 2, beforeTwo);
		RotationUtils.setValue(end, RotationUtils.get(beforeOne, end) - getMin(beforeOne) * 2, beforeOne);
		RotationUtils.setValue(end, RotationUtils.get(beforeTwo, end) - getMin(beforeTwo) * 2, beforeTwo);
		
		/*
		 * startOne = startCorner.isFacingPositive(one) ==
		 * slice.start.isFacingPositive(one) ? RotationUtils.get(one, start) :
		 * RotationUtils.get(one, end); startTwo = startCorner.isFacingPositive(two) ==
		 * slice.start.isFacingPositive(two) ? RotationUtils.get(two, start) :
		 * RotationUtils.get(two, end); endOne = startCorner.isFacingPositive(one) ==
		 * slice.start.isFacingPositive(one) ? RotationUtils.get(one, end) :
		 * RotationUtils.get(one, start); endTwo = startCorner.isFacingPositive(two) ==
		 * slice.start.isFacingPositive(two) ? RotationUtils.get(two, end) :
		 * RotationUtils.get(two, start);
		 */
		startOne = (float) (slice.start.isFacingPositive(one) ? Math.max(RotationUtils.get(one, start), RotationUtils.get(one, end)) : Math.min(RotationUtils.get(one, start), RotationUtils.get(one, end))) / 2F;
		startTwo = (float) (slice.start.isFacingPositive(two) ? Math.max(RotationUtils.get(two, start), RotationUtils.get(two, end)) : Math.min(RotationUtils.get(two, start), RotationUtils.get(two, end))) / 2F;
		endOne = (float) (!slice.start.isFacingPositive(one) ? Math.max(RotationUtils.get(one, start), RotationUtils.get(one, end)) : Math.min(RotationUtils.get(one, start), RotationUtils.get(one, end))) / 2F;
		endTwo = (float) (!slice.start.isFacingPositive(two) ? Math.max(RotationUtils.get(two, start), RotationUtils.get(two, end)) : Math.min(RotationUtils.get(two, start), RotationUtils.get(two, end))) / 2F;
	}
	
	@Override
	public void flipBox(Axis axis, LittleTileVec doubledCenter) {
		if (axis == slice.axis) {
			super.flipBox(axis, doubledCenter);
			return;
		}
		
		float startBefore = (getMax(axis) - getStart(axis)) * 2;
		float endBefore = (getMax(axis) - getEnd(axis)) * 2;
		
		super.flipBox(axis, doubledCenter);
		
		Axis other = axis == RotationUtils.getDifferentAxisFirst(slice.axis) ? RotationUtils.getDifferentAxisSecond(slice.axis) : RotationUtils.getDifferentAxisFirst(slice.axis);
		
		setStartRelative(axis, (float) (slice.start.isFacingPositive(axis) ? Math.max(startBefore, endBefore) : Math.min(startBefore, endBefore)) / 2);
		setEndRelative(axis, (float) (!slice.start.isFacingPositive(axis) ? Math.max(startBefore, endBefore) : Math.min(startBefore, endBefore)) / 2);
		
		startBefore = (float) (slice.start.isFacingPositive(other) ? Math.max(getStartRelative(other), getEndRelative(other)) : Math.min(getStartRelative(other), getEndRelative(other)));
		setEndRelative(other, (float) (!slice.start.isFacingPositive(other) ? Math.max(getStartRelative(other), getEndRelative(other)) : Math.min(getStartRelative(other), getEndRelative(other))));
		setStartRelative(other, startBefore);
	}
	
	// ================Basic Object Overrides================
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof LittleTileSlicedBox)
			return super.equals(object) && LittleUtils.equals(((LittleTileSlicedBox) object).startOne, startOne) && LittleUtils.equals(((LittleTileSlicedBox) object).startTwo, startTwo) && LittleUtils.equals(((LittleTileSlicedBox) object).endOne, endOne) && LittleUtils.equals(((LittleTileSlicedBox) object).endTwo, endTwo);
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "," + slice.name() + "," + startOne + "," + startTwo + "," + endOne + "," + endTwo + "]";
	}
	
	// ================Special methods================
	
	@Override
	public LittleTileSlicedBox copy() {
		return new LittleTileSlicedBox(minX, minY, minZ, maxX, maxY, maxZ, slice, startOne, startTwo, endOne, endTwo);
	}
	
	@Override
	public Vec3d getSliceNormal() {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		Vector3d vec = new Vector3d();
		RotationUtils.setValue(vec, getSliceSize(one) * slice.getDirectionScale(one), one);
		RotationUtils.setValue(vec, getSliceSize(two) * slice.getDirectionScale(two), two);
		
		RotationUtils.rotate(vec, Rotation.getRotation(slice.axis, slice.isRight));
		vec.normalize();
		return new Vec3d(vec.x, vec.y, vec.z);
	}
	
	@Override
	public Ray2d getSliceLine() {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		return new Ray2d(one, two, getSliceCorner(slice.start), getSliceSize(one) * slice.getDirectionScale(one), getSliceSize(two) * slice.getDirectionScale(two));
	}
	
	@Override
	public LittleTileBox grow(EnumFacing direction) {
		LittleTileSlicedBox box = (LittleTileSlicedBox) super.grow(direction);
		
		double delta = (double) box.getSize(direction.getAxis()) / (double) getSize(direction.getAxis());
		if (direction.getAxis() == RotationUtils.getDifferentAxisFirst(slice.axis)) {
			box.startOne *= delta;
			box.endOne *= delta;
		} else if (direction.getAxis() == RotationUtils.getDifferentAxisSecond(slice.axis)) {
			box.startTwo *= delta;
			box.endTwo *= delta;
		}
		return box;
	}
	
	@Override
	public LittleTileBox shrink(EnumFacing direction, boolean toLimit) {
		LittleTileSlicedBox box = (LittleTileSlicedBox) super.shrink(direction, toLimit);
		
		double delta = (double) box.getSize(direction.getAxis()) / (double) getSize(direction.getAxis());
		if (direction.getAxis() == RotationUtils.getDifferentAxisFirst(slice.axis)) {
			box.startOne *= delta;
			box.endOne *= delta;
		} else if (direction.getAxis() == RotationUtils.getDifferentAxisSecond(slice.axis)) {
			box.startTwo *= delta;
			box.endTwo *= delta;
		}
		return box;
	}
	
	/*
	 * @Override public LittleTileBox createNeighbourBox(EnumFacing facing) {
	 * if(facing == slice.emptySideOne || facing == slice.emptySideSecond) return
	 * null;
	 * 
	 * if(facing.getAxis() == slice.axis) { LittleTileSlicedBox newBox =
	 * this.copy();
	 * 
	 * switch(facing) { case EAST: newBox.minX = this.maxX; newBox.maxX++; break;
	 * case WEST: newBox.maxX = this.minX; newBox.minX--; break; case UP:
	 * newBox.minY = this.maxY; newBox.maxY++; break; case DOWN: newBox.maxY =
	 * this.minY; newBox.minY--; break; case SOUTH: newBox.minZ = this.maxZ;
	 * newBox.maxZ++; break; case NORTH: newBox.maxZ = this.minZ; newBox.minZ--;
	 * break; } return newBox; }
	 * 
	 * return super.createNeighbourBox(facing); }
	 */
	
	// ================Rendering================
	
	@Override
	@SideOnly(Side.CLIENT)
	public LittleRenderingCube getRenderingCube(LittleGridContext context, CubeObject cube, Block block, int meta) {
		return new LittleSlicedRenderingCube(context, cube, this, block, meta);
	}
	
	// ================Sliced================
	
	@Override
	public double getSliceAngle(Axis one, Axis two) {
		return getSliceSize(one) / getSliceSize(two);
	}
	
	public double getSliceValueOfFacing(EnumFacing facing) {
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			return getMaxSlice(facing.getAxis());
		return getMinSlice(facing.getAxis());
	}
	
	public double getSliceCornerValue(BoxCorner corner, Axis axis) {
		return getSliceValueOfFacing(corner.getFacing(axis));
	}
	
	public Vector3d getSliceCorner(BoxCorner corner) {
		return new Vector3d(corner.x.getAxisDirection() == AxisDirection.POSITIVE ? getMaxSlice(Axis.X) : getMinSlice(Axis.X), corner.y.getAxisDirection() == AxisDirection.POSITIVE ? getMaxSlice(Axis.Y) : getMinSlice(Axis.Y), corner.z.getAxisDirection() == AxisDirection.POSITIVE ? getMaxSlice(Axis.Z) : getMinSlice(Axis.Z));
	}
	
	public boolean hasAdditionalBoxOne() {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		if (slice.isFacingPositive(one))
			return getMinSlice(one) > getMin(one);
		return getMaxSlice(one) < getSize(one) + getMin(one);
	}
	
	public boolean hasAdditionalBoxTwo() {
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		if (slice.isFacingPositive(two))
			return getMinSlice(two) > getMin(two);
		return getMaxSlice(two) < getSize(two) + getMin(two);
	}
	
	public void setStartRelative(Axis axis, float value) {
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			startOne = value;
		else if (axis == RotationUtils.getDifferentAxisSecond(slice.axis))
			startTwo = value;
	}
	
	public void setEndRelative(Axis axis, float value) {
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			endOne = value;
		else if (axis == RotationUtils.getDifferentAxisSecond(slice.axis))
			endTwo = value;
	}
	
	public float getStartRelative(Axis axis) {
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			return startOne;
		return startTwo;
	}
	
	public float getEndRelative(Axis axis) {
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			return endOne;
		return endTwo;
	}
	
	public float getStart(Axis axis) {
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			return getMin(axis) + startOne;
		return getMin(axis) + startTwo;
	}
	
	public float getEnd(Axis axis) {
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			return getMin(axis) + endOne;
		return getMin(axis) + endTwo;
	}
	
	public float getMinSliceRelative(Axis axis) {
		if (slice.axis == axis)
			return 0;
		
		boolean axisOne = axis == RotationUtils.getDifferentAxisFirst(slice.axis);
		
		if (!slice.start.isFacingPositive(axis))
			if (axisOne)
				return startOne;
			else
				return startTwo;
		else if (axisOne)
			return endOne;
		else
			return endTwo;
	}
	
	public float getMaxSliceRelative(Axis axis) {
		if (slice.axis == axis)
			return 0;
		
		boolean axisOne = axis == RotationUtils.getDifferentAxisFirst(slice.axis);
		
		if (slice.start.isFacingPositive(axis))
			if (axisOne)
				return startOne;
			else
				return startTwo;
		else if (axisOne)
			return endOne;
		else
			return endTwo;
	}
	
	public double getMinSlice(Axis axis) {
		if (slice.axis == axis)
			return super.getMin(axis);
		
		boolean axisOne = axis == RotationUtils.getDifferentAxisFirst(slice.axis);
		
		if (!slice.start.isFacingPositive(axis))
			if (axisOne)
				return startOne + getMin(axis);
			else
				return startTwo + getMin(axis);
		else if (axisOne)
			return endOne + getMin(axis);
		else
			return endTwo + getMin(axis);
	}
	
	public double getMaxSlice(Axis axis) {
		if (slice.axis == axis)
			return super.getMax(axis);
		
		boolean axisOne = axis == RotationUtils.getDifferentAxisFirst(slice.axis);
		
		if (slice.start.isFacingPositive(axis))
			if (axisOne)
				return startOne + getMin(axis);
			else
				return startTwo + getMin(axis);
		else if (axisOne)
			return endOne + getMin(axis);
		else
			return endTwo + getMin(axis);
	}
	
	public float getSliceSize(Axis axis) {
		if (slice.axis == axis)
			return super.getSize(axis);
		
		if (axis == RotationUtils.getDifferentAxisFirst(slice.axis))
			return Math.abs(startOne - endOne);
		return Math.abs(startTwo - endTwo);
	}
	
	public Vector3d getSizeOfSlice() {
		Vector3d vec = new Vector3d(maxX - minX, maxY - minY, maxZ - minZ);
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		RotationUtils.setValue(vec, getSliceSize(one), one);
		RotationUtils.setValue(vec, getSliceSize(two), two);
		return vec;
	}
	
	public CubeObject getSlicedCube(LittleGridContext context) {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		CubeObject cube = super.getCube(context);
		cube.setMin(one, context.toVanillaGrid((float) getMinSlice(one)));
		cube.setMax(one, context.toVanillaGrid((float) getMaxSlice(one)));
		cube.setMin(two, context.toVanillaGrid((float) getMinSlice(two)));
		cube.setMax(two, context.toVanillaGrid((float) getMaxSlice(two)));
		return cube;
	}
	
	@Override
	public boolean isOrdinary() {
		return false;
	}
	
}
