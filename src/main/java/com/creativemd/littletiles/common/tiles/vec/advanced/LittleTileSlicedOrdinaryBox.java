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
import com.creativemd.littletiles.client.tiles.LittleSlicedOrdinaryRenderingCube;
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

public class LittleTileSlicedOrdinaryBox extends LittleTileBox {
	
	public LittleSlice slice;
	
	// ================Constructors================
	
	public LittleTileSlicedOrdinaryBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, LittleSlice slice) {
		super(minX, minY, minZ, maxX, maxY, maxZ);
		this.slice = slice;
	}
	
	public LittleTileSlicedOrdinaryBox(LittleTileBox box, LittleSlice slice) {
		this(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, slice);
	}
	
	// ================Conversions================
	
	@Override
	public AxisAlignedBB getSelectionBox(LittleGridContext context, BlockPos pos) {
		return super.getBox(context, pos);
	}
	
	@Override
	public AxisAlignedBBOrdinarySliced getBox(LittleGridContext context, BlockPos offset) {
		return new AxisAlignedBBOrdinarySliced(context.toVanillaGrid(minX) + offset.getX(), context.toVanillaGrid(minY) + offset.getY(), context.toVanillaGrid(minZ) + offset.getZ(), context.toVanillaGrid(maxX) + offset.getX(), context.toVanillaGrid(maxY) + offset.getY(), context.toVanillaGrid(maxZ) + offset.getZ(), slice);
	}
	
	@Override
	public AxisAlignedBBOrdinarySliced getBox(LittleGridContext context) {
		return new AxisAlignedBBOrdinarySliced(context.toVanillaGrid(minX), context.toVanillaGrid(minY), context.toVanillaGrid(minZ), context.toVanillaGrid(maxX), context.toVanillaGrid(maxY), context.toVanillaGrid(maxZ), slice);
	}
	
	// ================Save================
	
	@Override
	public int[] getArray() {
		return new int[] { minX, minY, minZ, maxX, maxY, maxZ, slice.getSliceID() };
	}
	
	// ================Size & Volume================
	
	@Override
	public boolean isCompletelyFilled() {
		return false;
	}
	
	@Override
	public double getVolume() {
		return super.getVolume() * 0.5;
	}
	
	// ================Block Integration================
	
	@Override
	public boolean doesFillEntireBlock(LittleGridContext context) {
		return false;
	}
	
	@Override
	public LittleTileBox createOutsideBlockBox(LittleGridContext context, EnumFacing facing) {
		if (facing == slice.emptySideOne || facing == slice.emptySideTwo)
			return null;
		
		if (facing.getAxis() == slice.axis) {
			LittleTileSlicedOrdinaryBox box = this.copy();
			
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
	 * @Override public LittleTileSlicedOrdinaryBox createInsideBlockBox(EnumFacing
	 * facing) { Vec3i vec = facing.getDirectionVec(); return new
	 * LittleTileSlicedOrdinaryBox(minX - vec.getX() * LittleTile.gridSize, minY -
	 * vec.getY() * LittleTile.gridSize, minZ - vec.getZ() * LittleTile.gridSize,
	 * maxX - vec.getX() * LittleTile.gridSize, maxY - vec.getY() *
	 * LittleTile.gridSize, maxZ - vec.getZ() * LittleTile.gridSize, slice); }
	 */
	
	// ================Box to box================
	
	@Override
	public LittleTileBox combineBoxes(LittleTileBox box, BasicCombiner combiner) {
		if (box instanceof LittleTileSlicedOrdinaryBox && ((LittleTileSlicedOrdinaryBox) box).isOrdinary() && ((LittleTileSlicedOrdinaryBox) box).slice == slice) {
			EnumFacing facing = sharedBoxFace(box);
			if (facing != null) {
				if (facing.getAxis() == slice.axis) {
					LittleTileBox newBox = copy();
					if (facing.getAxisDirection() != AxisDirection.POSITIVE)
						newBox.setMax(slice.axis, box.getMax(slice.axis));
					else
						newBox.setMin(slice.axis, box.getMin(slice.axis));
					return newBox;
				}
			} else if (getMin(slice.axis) == box.getMin(slice.axis) && getMax(slice.axis) == box.getMax(slice.axis)) {
				Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
				Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
				
				boolean shareOnePostive = getMax(one) == box.getMin(one);
				boolean shareOneNegative = getMin(one) == box.getMax(one);
				boolean shareTwoPostive = getMax(two) == box.getMin(two);
				boolean shareTwoNegative = getMin(two) == box.getMax(two);
				
				if ((shareOnePostive ^ shareOneNegative) && (shareTwoPostive ^ shareTwoNegative)) {
					boolean postiveOne = slice.isFacingPositive(one);
					boolean postiveTwo = slice.isFacingPositive(two);
					
					if ((postiveOne == shareOnePostive && postiveTwo == shareTwoPostive) || (postiveOne != shareOnePostive && postiveTwo != shareTwoPostive))
						return null;
					
					if (getSliceAngle(one, two) != ((LittleTileSlicedOrdinaryBox) box).getSliceAngle(one, two))
						return null;
					
					LittleTileBox boxInBetween = new LittleTileBox(this);
					if (shareOnePostive != postiveOne) {
						boxInBetween.setMin(one, box.getMin(one));
						boxInBetween.setMax(one, box.getMax(one));
					}
					
					if (shareTwoPostive != postiveTwo) {
						boxInBetween.setMin(two, box.getMin(two));
						boxInBetween.setMax(two, box.getMax(two));
					}
					
					if (combiner.cutOut(boxInBetween)) {
						LittleTileBox newBox = this.copy();
						if (shareOnePostive)
							newBox.setMax(one, box.getMax(one));
						else
							newBox.setMin(one, box.getMin(one));
						
						if (shareTwoPostive)
							newBox.setMax(two, box.getMax(two));
						else
							newBox.setMin(two, box.getMin(two));
						
						return newBox;
					}
				}
			}
		}
		return null;
	}
	
	public boolean isVecInsideBoxNoEdge(Vec3d vec) {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		double posOne = RotationUtils.get(one, vec);
		double posTwo = RotationUtils.get(two, vec);
		
		if (posOne >= getMin(one) && posOne < getMax(one) && posTwo >= getMin(two) && posTwo < getMax(two)) {
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getCornerValue(corner, one) - posOne);
			double difTwo = Math.abs(getCornerValue(corner, two) - posTwo);
			double sizeOne = getSize(one);
			double sizeTwo = getSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne > difOne && sizeTwo > difTwo && diff < 1;
		}
		return false;
	}
	
	protected boolean intersectsWithBetweenSliceAndBox(LittleTileBox box) {
		if (box instanceof LittleTileSlicedOrdinaryBox)
			return intersectsWithBetweenSliceAndBox2(box) && ((LittleTileSlicedOrdinaryBox) box).intersectsWithBetweenSliceAndBox2(this);
		return intersectsWithBetweenSliceAndBox2(box);
	}
	
	protected boolean intersectsWithBetweenSliceAndBox2(LittleTileBox box) {
		EnumFacing ignoreFace = RotationUtils.getFacing(slice.axis);
		
		Axis axisOne = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis axisTwo = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		// cube vectors
		BoxCorner cornerMin = BoxCorner.getCornerUnsorted(ignoreFace, slice.emptySideOne.getOpposite(), slice.emptySideTwo.getOpposite());
		BoxCorner cornerMax = BoxCorner.getCornerUnsorted(ignoreFace, slice.emptySideOne, slice.emptySideTwo);
		
		// vec triangle
		int pointOne = getValueOfFacing(slice.getEmptySide(axisOne).getOpposite());
		int pointTwo = getValueOfFacing(slice.getEmptySide(axisTwo).getOpposite());
		
		LittleTileVec minVec = box.getCorner(cornerMin);
		LittleTileVec maxVec = box.getCorner(cornerMax);
		
		minVec.set(slice.axis, getValueOfFacing(ignoreFace.getOpposite()));
		maxVec.set(slice.axis, getValueOfFacing(ignoreFace.getOpposite()));
		
		// check if point is inside triangle (both)
		if (isVecInsideBoxNoEdge(box.getExactCorner(cornerMin)))
			return true;
		
		if (isVecInsideBoxNoEdge(box.getExactCorner(cornerMax)))
			return true;
		
		// pointing positive
		if (slice.isFacingPositive(axisOne)) {
			// check axis one
			if (minVec.get(axisOne) <= pointOne)
				return true;
		}
		// pointing negative
		else {
			// check axis one
			if (minVec.get(axisOne) >= pointOne)
				return true;
		}
		
		// pointing positive
		if (slice.isFacingPositive(axisTwo)) {
			// check axis one
			if (minVec.get(axisTwo) <= pointTwo)
				return true;
		}
		// pointing negative
		else {
			// check axis one
			if (minVec.get(axisTwo) >= pointTwo)
				return true;
		}
		
		return false;
	}
	
	@Override
	protected boolean intersectsWith(LittleTileBox box) {
		if (!super.intersectsWith(box))
			return false;
		
		if (intersectsWithBetweenSliceAndBox(box)) {
			if (box.getClass() == LittleTileBox.class)
				return true;
			
			if (box instanceof LittleTileSlicedOrdinaryBox) {
				LittleTileSlicedOrdinaryBox sliceBox = (LittleTileSlicedOrdinaryBox) box;
				if (sliceBox.slice.axis != slice.axis || sliceBox.slice.getOpposite() != slice)
					return true;
				
				Vector3d vec = this.getSliceLine().intersect(((LittleTileSlicedOrdinaryBox) box).getSliceLine(), getMin(slice.axis));
				return (sliceBox.slice.getOpposite() == slice) == (vec == null ? false : isVecInsideBoxRelative(new Vec3d(vec.x, vec.y, vec.z)));
			}
			
		}
		return false;
	}
	
	// ================Vectors================
	
	public boolean isVecInsideBoxRelative(Vec3d vec) {
		if (vec.x >= minX && vec.x < maxX && vec.y >= minY && vec.y < maxY && vec.z >= minZ && vec.z < maxZ) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getCornerValue(corner, one) - RotationUtils.get(one, vec));
			double difTwo = Math.abs(getCornerValue(corner, two) - RotationUtils.get(two, vec));
			int sizeOne = getSize(one);
			int sizeTwo = getSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff <= 1;
		}
		return false;
	}
	
	@Override
	public boolean isVecInsideBox(int x, int y, int z) {
		if (super.isVecInsideBox(x, y, z)) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			BoxCorner corner = slice.getFilledCorner();
			
			int posOne = RotationUtils.get(one, x, y, z);
			int posTwo = RotationUtils.get(two, x, y, z);
			
			if (!slice.isFacingPositive(one))
				posOne = posOne + 1;
			
			if (!slice.isFacingPositive(two))
				posTwo = posTwo + 1;
			
			int difOne = Math.abs(getCornerValue(corner, one) - posOne);
			int difTwo = Math.abs(getCornerValue(corner, two) - posTwo);
			int sizeOne = getSize(one);
			int sizeTwo = getSize(two);
			
			double diff = (double) difOne / sizeOne + (double) difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && (diff < 1 || LittleUtils.equals(diff, 1));
		}
		return false;
	}
	
	@Override
	public boolean isVecInsideBox(LittleTileBox box, LittleTileVec vec) {
		if (box.isCompletelyFilled())
			return isVecInsideBox(vec.x, vec.y, vec.z);
		
		LittleTileBox box2 = extractBox(vec.x, vec.y, vec.z);
		if (box2 != null)
			return LittleTileBox.intersectsWith(box, box2);
		return false;
	}
	
	@Override
	public boolean canFaceBeCombined(LittleTileBox other) {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		return (other instanceof LittleTileSlicedOrdinaryBox) && ((LittleTileSlicedOrdinaryBox) other).getSliceAngle(one, two) == getSliceAngle(one, two);
	}
	
	@Override
	public boolean intersectsWithFace(EnumFacing facing, LittleTileVec vec, boolean completely) {
		if (!super.intersectsWithFace(facing, vec, completely))
			return false;
		
		if (facing.getAxis() == slice.axis) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			LittleTileVec copy = vec.copy();
			
			if (completely == slice.isFacingPositive(one))
				copy.set(one, copy.get(one) + 1);
			
			if (completely == slice.isFacingPositive(two))
				copy.set(two, copy.get(two) + 1);
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getCornerValue(corner, one) - copy.get(one));
			double difTwo = Math.abs(getCornerValue(corner, two) - copy.get(two));
			double sizeOne = getSize(one);
			double sizeTwo = getSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne > difOne && sizeTwo > difTwo && diff < 1;
		}
		return slice.getEmptySide(facing.getAxis()) != facing;
	}
	
	@Override
	public boolean intersectsWithAxis(LittleGridContext context, Axis axis, Vec3d vec) {
		if (!super.intersectsWithAxis(context, axis, vec))
			return false;
		
		if (slice.axis == axis) {
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(context.toVanillaGrid(getCornerValue(corner, one)) - RotationUtils.get(one, vec));
			double difTwo = Math.abs(context.toVanillaGrid(getCornerValue(corner, two)) - RotationUtils.get(two, vec));
			double sizeOne = getSize(one) / (double) context.size;
			double sizeTwo = getSize(two) / (double) context.size;
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff <= 1;
		}
		return true;
	}
	
	public static Vec3d linePlaneIntersection(Vec3d planeOrigin, Vec3d planeNormal, Vec3d rayOrigin, Vec3d rayDirection) {
		if (planeNormal.dotProduct(rayDirection) == 0)
			return null;
		
		return rayOrigin.add(rayDirection.scale(planeNormal.dotProduct(planeOrigin.subtract(rayOrigin)) / planeNormal.dotProduct(rayDirection)));
	}
	
	@Override
	@Nullable
	public RayTraceResult calculateIntercept(LittleGridContext context, BlockPos pos, Vec3d vecA, Vec3d vecB) {
		vecA = vecA.subtract(pos.getX(), pos.getY(), pos.getZ());
		vecB = vecB.subtract(pos.getX(), pos.getY(), pos.getZ());
		
		Vec3d collision = null;
		EnumFacing collided = null;
		
		for (EnumFacing facing : EnumFacing.VALUES) {
			if (slice.emptySideOne != facing && slice.emptySideTwo != facing) {
				Vec3d temp = collideWithPlane(context, facing.getAxis(), (double) getValueOfFacing(facing) / context.size, vecA, vecB);
				if (temp != null && isClosest(vecA, collision, temp)) {
					collided = facing;
					collision = temp;
				}
			}
		}
		
		EnumFacing diagonal = slice.getPreferedSide(getSize());
		Vec3d temp = linePlaneIntersection(getCorner(slice.start).getVec(context), getSliceNormal(), vecA, vecB.subtract(vecA));
		if (temp != null && intersectsWithAxis(context, diagonal.getAxis(), temp) && isClosest(vecA, collision, temp)) {
			collision = temp;
			collided = diagonal;
		}
		
		if (collision == null)
			return null;
		
		return new RayTraceResult(collision.addVector(pos.getX(), pos.getY(), pos.getZ()), collided, pos);
	}
	
	// ================Rotation & Flip================
	
	@Override
	public void rotateBox(Rotation rotation, LittleTileVec doubledCenter) {
		super.rotateBox(rotation, doubledCenter);
		this.slice = this.slice.rotate(rotation);
	}
	
	@Override
	public void flipBox(Axis axis, LittleTileVec doubledCenter) {
		super.flipBox(axis, doubledCenter);
		this.slice = this.slice.flip(axis);
	}
	
	// ================Basic Object Overrides================
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof LittleTileSlicedOrdinaryBox)
			return super.equals(object) && ((LittleTileSlicedOrdinaryBox) object).slice == slice;
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "," + slice.name() + "]";
	}
	
	// ================Special methods================
	
	@Override
	public LittleTileSlicedOrdinaryBox copy() {
		return new LittleTileSlicedOrdinaryBox(minX, minY, minZ, maxX, maxY, maxZ, slice);
	}
	
	public double getSliceAngle(Axis one, Axis two) {
		return getSize(one) / (double) getSize(two);
	}
	
	public Vec3d getSliceNormal() {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		Vector3d vec = new Vector3d();
		RotationUtils.setValue(vec, getSize(one) * slice.getDirectionScale(one), one);
		RotationUtils.setValue(vec, getSize(two) * slice.getDirectionScale(two), two);
		
		RotationUtils.rotateVec(vec, Rotation.getRotation(slice.axis, slice.isRight));
		vec.normalize();
		return new Vec3d(vec.x, vec.y, vec.z);
	}
	
	public Ray2d getSliceLine() {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		return new Ray2d(one, two, getCorner(slice.start), getSize(one) * slice.getDirectionScale(one), getSize(two) * slice.getDirectionScale(two));
	}
	
	@Override
	public LittleTileBox extractBox(int x, int y, int z) {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		Ray2d line = getSliceLine();
		
		int minOne = RotationUtils.get(one, x, y, z);
		int minTwo = RotationUtils.get(two, x, y, z);
		int maxOne = minOne + 1;
		int maxTwo = minTwo + 1;
		
		double startOne = line.get(two, slice.start.isFacingPositive(two) ? maxTwo : minTwo);
		double startTwo = line.get(one, slice.start.isFacingPositive(one) ? maxOne : minOne);
		double endOne = line.get(two, slice.start.isFacingPositive(two) ? minTwo : maxTwo);
		double endTwo = line.get(one, slice.start.isFacingPositive(one) ? minOne : maxOne);
		
		boolean startOneIntersection = startOne >= minOne && startOne <= maxOne;
		boolean startTwoIntersection = startTwo >= minTwo && startTwo <= maxTwo;
		boolean endOneIntersection = endOne >= minOne && endOne <= maxOne;
		boolean endTwoIntersection = endTwo >= minTwo && endTwo <= maxTwo;
		
		if ((startOneIntersection || startTwoIntersection) && (endOneIntersection || endTwoIntersection)) {
			if (startOneIntersection)
				startTwo = slice.start.isFacingPositive(two) ? maxTwo : minTwo; // line.get(one, startOne);
			else
				startOne = slice.start.isFacingPositive(one) ? maxOne : minOne; // line.get(two, startTwo);
				
			if (endOneIntersection)
				endTwo = slice.end.isFacingPositive(two) ? maxTwo : minTwo; // line.get(one, endOne);
			else
				endOne = slice.end.isFacingPositive(one) ? maxOne : minOne; // line.get(two, endTwo);
				
			int minBoxOne = Math.min((int) Math.floor(LittleUtils.round(startOne)), (int) Math.floor(LittleUtils.round(endOne)));
			int minBoxTwo = Math.min((int) Math.floor(LittleUtils.round(startTwo)), (int) Math.floor(LittleUtils.round(endTwo)));
			
			int maxBoxOne = Math.max((int) Math.ceil(LittleUtils.round(startOne)), (int) Math.ceil(LittleUtils.round(endOne)));
			int maxBoxTwo = Math.max((int) Math.ceil(LittleUtils.round(startTwo)), (int) Math.ceil(LittleUtils.round(endTwo)));
			
			startOne -= minBoxOne;
			endOne -= minBoxOne;
			startTwo -= minBoxTwo;
			endTwo -= minBoxTwo;
			
			LittleTileSlicedOrdinaryBox slicedBox;
			if (Math.min(startOne, endOne) == 0 && Math.min(startTwo, endTwo) == 0 && Math.max(startOne, endOne) == maxBoxOne - minBoxOne && Math.max(startTwo, endTwo) == maxBoxTwo - minBoxTwo)
				slicedBox = new LittleTileSlicedOrdinaryBox(x, y, z, x + 1, y + 1, z + 1, slice);
			else
				slicedBox = new LittleTileSlicedBox(x, y, z, x + 1, y + 1, z + 1, slice, (float) startOne, (float) startTwo, (float) endOne, (float) endTwo);
			
			slicedBox.setMin(one, minBoxOne);
			slicedBox.setMin(two, minBoxTwo);
			slicedBox.setMax(one, maxBoxOne);
			slicedBox.setMax(two, maxBoxTwo);
			
			if (slicedBox.isValidBox()) {
				return slicedBox;
			} else if (slice.isRight == (line.isCoordinateOnLine(minOne, minTwo) ? line.isCoordinateToTheRight(maxOne, maxTwo) : line.isCoordinateToTheRight(minOne, minTwo)))
				return new LittleTileBox(x, y, z, x + 1, y + 1, z + 1);
			
			return null;
		} else {
			// Slice does not intersect with extracted Box.
			// Now try to figure out if the box is inside the filled part or not
			if (slice.isRight == (line.isCoordinateOnLine(minOne, minTwo) ? line.isCoordinateToTheRight(maxOne, maxTwo) : line.isCoordinateToTheRight(minOne, minTwo))) {
				// It's inside the filled part, therefore create an ordinary box
				return new LittleTileBox(x, y, z, x + 1, y + 1, z + 1);
			}
		}
		
		return null;
	}
	
	@Override
	public List<LittleTileBox> extractBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, List<LittleTileBox> boxes) {
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		Ray2d line = getSliceLine();
		
		int minOne = RotationUtils.get(one, minX, minY, minZ);
		int minTwo = RotationUtils.get(two, minX, minY, minZ);
		int maxOne = RotationUtils.get(one, maxX, maxY, maxZ);
		int maxTwo = RotationUtils.get(two, maxX, maxY, maxZ);
		
		double startOne = line.get(two, slice.start.isFacingPositive(two) ? maxTwo : minTwo);
		double startTwo = line.get(one, slice.start.isFacingPositive(one) ? maxOne : minOne);
		double endOne = line.get(two, slice.start.isFacingPositive(two) ? minTwo : maxTwo);
		double endTwo = line.get(one, slice.start.isFacingPositive(one) ? minOne : maxOne);
		
		boolean startOneIntersection = startOne >= minOne && startOne <= maxOne;
		boolean startTwoIntersection = startTwo >= minTwo && startTwo <= maxTwo;
		boolean endOneIntersection = endOne >= minOne && endOne <= maxOne;
		boolean endTwoIntersection = endTwo >= minTwo && endTwo <= maxTwo;
		
		if ((startOneIntersection || startTwoIntersection) && (endOneIntersection || endTwoIntersection)) {
			if (startOneIntersection)
				startTwo = line.get(one, startOne);
			else
				startOne = line.get(two, startTwo);
			
			if (endOneIntersection)
				endTwo = line.get(one, endOne);
			else
				endOne = line.get(two, endTwo);
			
			int minBoxOne = Math.min((int) Math.floor(LittleUtils.round(startOne)), (int) Math.floor(LittleUtils.round(endOne)));
			int minBoxTwo = Math.min((int) Math.floor(LittleUtils.round(startTwo)), (int) Math.floor(LittleUtils.round(endTwo)));
			
			int maxBoxOne = Math.max((int) Math.ceil(LittleUtils.round(startOne)), (int) Math.ceil(LittleUtils.round(endOne)));
			int maxBoxTwo = Math.max((int) Math.ceil(LittleUtils.round(startTwo)), (int) Math.ceil(LittleUtils.round(endTwo)));
			
			startOne -= minBoxOne;
			endOne -= minBoxOne;
			startTwo -= minBoxTwo;
			endTwo -= minBoxTwo;
			
			LittleTileSlicedOrdinaryBox slicedBox;
			if (Math.min(startOne, endOne) == 0 && Math.min(startTwo, endTwo) == 0 && Math.max(startOne, endOne) == maxBoxOne - minBoxOne && Math.max(startTwo, endTwo) == maxBoxTwo - minBoxTwo)
				slicedBox = new LittleTileSlicedOrdinaryBox(minX, minY, minZ, maxX, maxY, maxZ, slice);
			else
				slicedBox = new LittleTileSlicedBox(minX, minY, minZ, maxX, maxY, maxZ, slice, (float) startOne, (float) startTwo, (float) endOne, (float) endTwo);
			
			slicedBox.setMin(one, minBoxOne);
			slicedBox.setMin(two, minBoxTwo);
			slicedBox.setMax(one, maxBoxOne);
			slicedBox.setMax(two, maxBoxTwo);
			
			if (slicedBox.isValidBox())
				boxes.add(slicedBox);
			else {
				if (slice.isRight != (line.isCoordinateOnLine(minOne, minTwo) ? line.isCoordinateToTheRight(maxOne, maxTwo) : line.isCoordinateToTheRight(minOne, minTwo)))
					return boxes;
			}
			
			boolean postiveOne = slice.isFacingPositive(one);
			boolean postiveTwo = slice.isFacingPositive(two);
			
			boolean hasAdditionalBoxOne = postiveTwo == slice.start.isFacingPositive(two) ? !startTwoIntersection : !endTwoIntersection;
			boolean hasAdditionalBoxTwo = postiveOne == slice.start.isFacingPositive(one) ? !startOneIntersection : !endOneIntersection;
			
			int minAdditionalBoxOne = Math.min(postiveOne ? minOne : slicedBox.getMax(one), postiveOne ? slicedBox.getMin(one) : maxOne);
			int minAdditionalBoxTwo = Math.min(postiveTwo ? minTwo : slicedBox.getMax(two), postiveTwo ? slicedBox.getMin(two) : maxTwo);
			int maxAdditionalBoxOne = Math.max(postiveOne ? minOne : slicedBox.getMax(one), postiveOne ? slicedBox.getMin(one) : maxOne);
			int maxAdditionalBoxTwo = Math.max(postiveTwo ? minTwo : slicedBox.getMax(two), postiveTwo ? slicedBox.getMin(two) : maxTwo);
			
			if (hasAdditionalBoxOne && minAdditionalBoxOne >= maxAdditionalBoxOne)
				hasAdditionalBoxOne = false;
			
			if (hasAdditionalBoxTwo && minAdditionalBoxTwo >= maxAdditionalBoxTwo)
				hasAdditionalBoxTwo = false;
			
			if (hasAdditionalBoxOne) {
				LittleTileBox additionalBoxOne = new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
				additionalBoxOne.setMin(one, minAdditionalBoxOne);
				additionalBoxOne.setMax(one, maxAdditionalBoxOne);
				if (!hasAdditionalBoxTwo) {
					if (postiveTwo)
						additionalBoxOne.setMin(two, minAdditionalBoxTwo);
					else
						additionalBoxOne.setMax(two, maxAdditionalBoxTwo);
				}
				if (additionalBoxOne.isValidBox())
					boxes.add(additionalBoxOne);
			}
			
			if (hasAdditionalBoxTwo) {
				LittleTileBox additionalBoxTwo = new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
				additionalBoxTwo.setMin(two, minAdditionalBoxTwo);
				additionalBoxTwo.setMax(two, maxAdditionalBoxTwo);
				
				if (postiveOne)
					additionalBoxTwo.setMin(one, maxAdditionalBoxOne);
				else
					additionalBoxTwo.setMax(one, minAdditionalBoxOne);
				if (additionalBoxTwo.isValidBox())
					boxes.add(additionalBoxTwo);
			}
			
		} else {
			// Slice does not intersect with extracted Box.
			// Now try to figure out if the box is inside the filled part or not
			/*
			 * if((slice.axis == Axis.Z ? slice.isRight : !slice.isRight) ==
			 * (line.isCoordinateOnLine(minOne, minTwo) ?
			 * line.isCoordinateToTheRight(maxOne, maxTwo) :
			 * line.isCoordinateToTheRight(minOne, minTwo))) { //It's inside the filled
			 * part, therefore create an ordinary box boxes.add(new LittleTileBox(minX,
			 * minY, minZ, maxX, maxY, maxZ)); }
			 */
			if (slice.isRight == (line.isCoordinateOnLine(minOne, minTwo) ? line.isCoordinateToTheRight(maxOne, maxTwo) : line.isCoordinateToTheRight(minOne, minTwo))) {
				// It's inside the filled part, therefore create an ordinary box
				boxes.add(new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ));
			}
		}
		
		return boxes;
	}
	
	/*
	 * @Override public LittleTileBox createNeighbourBox(EnumFacing facing) {
	 * if(facing == slice.emptySideOne || facing == slice.emptySideSecond) return
	 * null;
	 * 
	 * if(facing.getAxis() == slice.axis) { LittleTileSlicedOrdinaryBox newBox =
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
		return new LittleSlicedOrdinaryRenderingCube(cube, this, block, meta);
	}
	
	// ================Faces================
	
	@Override
	@Nullable
	public LittleTileFace getFace(LittleGridContext context, EnumFacing facing) {
		if (facing == slice.emptySideOne || facing == slice.emptySideTwo)
			return null;
		
		return super.getFace(context, facing);
	}
	
	// ================Sliced================
	
	public boolean isOrdinary() {
		return true;
	}
	
	// ================Identifier================
	
	@Override
	public int[] getIdentifier() {
		BoxCorner corner = slice.getFilledCorner();
		return new int[] { getCornerX(corner), getCornerY(corner), getCornerZ(corner), slice.ordinal() };
	}
	
	@Override
	public boolean is(int[] identifier) {
		if (identifier.length == 4 && identifier[3] == slice.ordinal()) {
			BoxCorner corner = slice.getFilledCorner();
			return identifier[0] == getCornerX(corner) && identifier[1] == getCornerY(corner) && identifier[2] == getCornerZ(corner);
		}
		return false;
	}
	
}
