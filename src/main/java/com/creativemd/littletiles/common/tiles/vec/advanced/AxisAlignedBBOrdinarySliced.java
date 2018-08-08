package com.creativemd.littletiles.common.tiles.vec.advanced;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.CreativeAxisAlignedBB;
import com.creativemd.creativecore.common.utils.math.box.BoxUtils.BoxCorner;
import com.creativemd.creativecore.common.utils.math.vec.Ray2d;
import com.creativemd.littletiles.common.tiles.vec.LittleUtils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class AxisAlignedBBOrdinarySliced extends CreativeAxisAlignedBB {
	
	public final LittleSlice slice;
	
	public AxisAlignedBBOrdinarySliced(double x1, double y1, double z1, double x2, double y2, double z2, LittleSlice slice) {
		super(x1, y1, z1, x2, y2, z2);
		this.slice = slice;
	}
	
	@Override
	public AxisAlignedBBOrdinarySliced setMaxY(double y2)
    {
        return new AxisAlignedBBOrdinarySliced(this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ, slice);
    }
	
	@Override
	public boolean equals(Object object)
    {
        if(object.getClass() == AxisAlignedBBOrdinarySliced.class)
        {
        	return ((AxisAlignedBBOrdinarySliced) object).minX == this.minX &&
        			((AxisAlignedBBOrdinarySliced) object).minY == this.minY &&
        			((AxisAlignedBBOrdinarySliced) object).minZ == this.minZ &&
        			((AxisAlignedBBOrdinarySliced) object).maxX == this.maxX &&
        			((AxisAlignedBBOrdinarySliced) object).maxY == this.maxY &&
        			((AxisAlignedBBOrdinarySliced) object).maxZ == this.maxZ &&
        			((AxisAlignedBBOrdinarySliced) object).slice == this.slice;        			
        }
        return false;
    }
	
	@Override
	public AxisAlignedBBOrdinarySliced contract(double p_191195_1_, double p_191195_3_, double p_191195_5_)
    {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (p_191195_1_ < 0.0D)
        {
            d0 -= p_191195_1_;
        }
        else if (p_191195_1_ > 0.0D)
        {
            d3 -= p_191195_1_;
        }

        if (p_191195_3_ < 0.0D)
        {
            d1 -= p_191195_3_;
        }
        else if (p_191195_3_ > 0.0D)
        {
            d4 -= p_191195_3_;
        }

        if (p_191195_5_ < 0.0D)
        {
            d2 -= p_191195_5_;
        }
        else if (p_191195_5_ > 0.0D)
        {
            d5 -= p_191195_5_;
        }

        return new AxisAlignedBBOrdinarySliced(d0, d1, d2, d3, d4, d5, slice);
    }

    @Override
    public AxisAlignedBBOrdinarySliced expand(double x, double y, double z)
    {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D)
        {
            d0 += x;
        }
        else if (x > 0.0D)
        {
            d3 += x;
        }

        if (y < 0.0D)
        {
            d1 += y;
        }
        else if (y > 0.0D)
        {
            d4 += y;
        }

        if (z < 0.0D)
        {
            d2 += z;
        }
        else if (z > 0.0D)
        {
            d5 += z;
        }

        return new AxisAlignedBBOrdinarySliced(d0, d1, d2, d3, d4, d5, slice);
    }

    @Override
    public AxisAlignedBB grow(double x, double y, double z)
    {
        double d0 = this.minX - x;
        double d1 = this.minY - y;
        double d2 = this.minZ - z;
        double d3 = this.maxX + x;
        double d4 = this.maxY + y;
        double d5 = this.maxZ + z;
        return new AxisAlignedBBOrdinarySliced(d0, d1, d2, d3, d4, d5, slice);
    }
    
    @Override
    public AxisAlignedBBOrdinarySliced intersect(AxisAlignedBB p_191500_1_)
    {
        double d0 = Math.max(this.minX, p_191500_1_.minX);
        double d1 = Math.max(this.minY, p_191500_1_.minY);
        double d2 = Math.max(this.minZ, p_191500_1_.minZ);
        double d3 = Math.min(this.maxX, p_191500_1_.maxX);
        double d4 = Math.min(this.maxY, p_191500_1_.maxY);
        double d5 = Math.min(this.maxZ, p_191500_1_.maxZ);
        return new AxisAlignedBBOrdinarySliced(d0, d1, d2, d3, d4, d5, slice);
    }
    
    @Override
    public AxisAlignedBB offset(double x, double y, double z)
    {
        return new AxisAlignedBBOrdinarySliced(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z, slice);
    }
    
    @Override
    public AxisAlignedBB offset(BlockPos pos)
    {
        return new AxisAlignedBBOrdinarySliced(this.minX + pos.getX(), this.minY + pos.getY(), this.minZ + pos.getZ(), this.maxX + pos.getX(), this.maxY + pos.getY(), this.maxZ + pos.getZ(), slice);
    }
    
    public boolean intersectsTwoSides(Axis toIgnore, AxisAlignedBB other)
    {
    	if(slice.axis != toIgnore)
    	{
    		Axis axisOne = RotationUtils.getDifferentAxisFirst(toIgnore);
    		Axis axisTwo = RotationUtils.getDifferentAxisSecond(toIgnore);
    		
    		return (getMax(other, axisOne) > getMin(axisOne) && getMin(other, axisOne) < getMax(axisOne)) &&
    				(getMax(other, axisTwo) > getMin(axisTwo) && getMin(other, axisTwo) < getMax(axisTwo));
    	}
    	
    	return intersectsWithOrdinaryTwoAxis(other);
    }
    
    public double calculateYOffsetStepUp(AxisAlignedBB other, AxisAlignedBB otherY, double offset)
    {
    	if(!slice.isFacingPositive(Axis.Y))
    	{
    		Axis axis = Axis.Y;
    		BoxCorner filledCorner = slice.getFilledCorner();
        	Vec3d otherVec = getCorner(otherY, filledCorner);
        	Axis one = axis;
        	Axis two = RotationUtils.getDifferentAxisFirst(slice.axis) != axis ? RotationUtils.getDifferentAxisFirst(slice.axis) : RotationUtils.getDifferentAxisSecond(slice.axis);
        	
        	Ray2d line = getSliceLine();
        	double newPos = line.get(two, RotationUtils.get(two, otherVec));
        	newPos = MathHelper.clamp(newPos, getMin(axis), getMax(axis));
        	
        	double d0 = newPos - getMax(otherY, axis);
        	if (d0 < offset)
			{
	        	if(getMax(otherY, axis) < newPos || LittleUtils.equals(getMax(otherY, axis), newPos))
	        		return d0 / 2;
			}
    	}
    	return calculateYOffset(other, offset);
    }
    
    public double calculateAxisOffset(AxisAlignedBB other, Axis axis, double offset)
    {
    	if (intersectsTwoSides(axis, other))
        {
    		boolean isOrdinary = slice.axis == axis || (slice.isFacingPositive(axis) == (offset > 0)); //(slice.isFacingPositive(axis) != LittleUtils.smallerThanAndEquals(offset, 0));
    		if(isOrdinary)
    		{
	            if (offset > 0.0D && getMax(other, axis) <= getMin(axis)) //&& getMax(other, axis) <= getMin(axis))
	            {
            		double d1 = getMin(axis) - getMax(other, axis);

                    if (d1 < offset)
                    {
                        return d1;
                    }
                    return offset;
	            }
	            else if (offset < 0.0D && getMin(other, axis) >= getMax(axis)) //&& getMin(other, axis) >= getMax(axis))
	            {
            		double d0 = getMax(axis) - getMin(other, axis);

                    if (d0 > offset)
                    {
                    	return d0;
                    }
                    return offset;      	
	            }else
	            	return offset;
    		}
            
    		// For anyone who reads this, this was a story of a life time spend on the most complicated problem, while solved with brute force, it shows it elegance ingame. just enjoy ;) - n247s
    		BoxCorner filledCorner = slice.getFilledCorner();
        	Vec3d otherVec = getCorner(other, filledCorner);
        	Axis one = axis;
        	Axis two = RotationUtils.getDifferentAxisFirst(slice.axis) != axis ? RotationUtils.getDifferentAxisFirst(slice.axis) : RotationUtils.getDifferentAxisSecond(slice.axis);
        	
        	Ray2d line = getSliceLine();
        	double newPos = line.get(two, RotationUtils.get(two, otherVec));
        	newPos = MathHelper.clamp(newPos, getMin(axis), getMax(axis));
        	
    		//double d0 = newPos - RotationUtils.get(axis, otherVec);
    		
    		if (offset > 0.0D && (getMax(other, axis) < newPos || LittleUtils.equals(getMax(other, axis), newPos)))
            {
    			double d0 = newPos - getMax(other, axis);
    			if (d0 < offset)
    			{
    				return d0;
    			}
    			
    			return offset;
            }
            else if (offset < 0.0D && (getMin(other, axis) > newPos || LittleUtils.equals(getMin(other, axis), newPos)))
            {
            	double d0 = newPos - getMin(other, axis);
            	if (/*(d0 < 0 || LittleUtils.equals(d0, 0)) &&*/ d0 > offset)
            	{
            		return d0;
            	}
            	return offset; 
            }
    		return offset; 
        }
        return offset;
    }
    
    public Ray2d getSliceLine()
	{
		Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		Vec3d corner = getCorner(slice.start);
		return new Ray2d(one, two, new Vector3d(corner.x, corner.y, corner.z), getSize(one) * slice.getDirectionScale(one), getSize(two) * slice.getDirectionScale(two));
	}
    
    /**
     * if instance and the argument bounding boxes overlap in the Y and Z dimensions, calculate the offset between them
     * in the X dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX)
    {
        return calculateAxisOffset(other, Axis.X, offsetX);
    }

    /**
     * if instance and the argument bounding boxes overlap in the X and Z dimensions, calculate the offset between them
     * in the Y dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY)
    {
    	return calculateAxisOffset(other, Axis.Y, offsetY);
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ)
    {
    	return calculateAxisOffset(other, Axis.Z, offsetZ);
    }
    
    
    public boolean isVecInsideBoxEdgeTwoSides(Vec3d vec)
	{
    	Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
		
		if(RotationUtils.get(one, vec) >= getMin(one) && RotationUtils.get(one, vec) < getMax(one) &&
    			RotationUtils.get(two, vec) >= getMin(two) && RotationUtils.get(two, vec) < getMax(two))
		{
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getCornerValue(corner, one) - RotationUtils.get(one, vec));
			double difTwo = Math.abs(getCornerValue(corner, two) - RotationUtils.get(two, vec));
			double sizeOne = getSize(one);
			double sizeTwo = getSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff < 1;
			//return LittleUtils.greaterThanAndEquals(sizeOne, difOne) && LittleUtils.greaterThanAndEquals(sizeTwo, difTwo) && LittleUtils.smallerThanAndEquals(diff, 1);
		}
		return false;
	}
    
    private boolean intersectsWithOrdinaryTwoAxis(AxisAlignedBB other)
    {
    	EnumFacing ignoreFace = RotationUtils.getFacing(slice.axis);
		
		Axis axisOne = RotationUtils.getDifferentAxisFirst(slice.axis);
		Axis axisTwo = RotationUtils.getDifferentAxisSecond(slice.axis);
				
		if(!(getMax(other, axisOne) > getMin(axisOne) && getMin(other, axisOne) < getMax(axisOne) &&
				getMax(other, axisTwo) > getMin(axisTwo) && getMin(other, axisTwo) < getMax(axisTwo)))
			return false;
		
		BoxCorner cornerMin = BoxCorner.getCornerUnsorted(ignoreFace, slice.emptySideOne.getOpposite(), slice.emptySideTwo.getOpposite());
		BoxCorner cornerMax = BoxCorner.getCornerUnsorted(ignoreFace, slice.emptySideOne, slice.emptySideTwo);
		
		double pointOne = getValueOfFacing(slice.getEmptySide(axisOne).getOpposite());
		double pointTwo = getValueOfFacing(slice.getEmptySide(axisTwo).getOpposite());
		
		Vec3d minVec = getCorner(other, cornerMin);
		Vec3d maxVec = getCorner(other, cornerMax);
		
		//minVec = RotationUtils.setValue(minVec, getValueOfFacing(ignoreFace.getOpposite()), slice.axis);
		//maxVec = RotationUtils.setValue(maxVec, getValueOfFacing(ignoreFace.getOpposite()), slice.axis);
		
		if(isVecInsideBoxEdgeTwoSides(minVec))
			return true;
		
		if(isVecInsideBoxEdgeTwoSides(maxVec))
			return true;
		
		if(slice.isFacingPositive(axisOne))
		{
			if(RotationUtils.get(axisOne, minVec) <= pointOne)
				return true;
		}
		// pointing negative
		else
		{
			// check axis one
			if(RotationUtils.get(axisOne, minVec) >= pointOne)
				return true;
		}
		
		// pointing positive
		if(slice.isFacingPositive(axisTwo))
		{
			// check axis one
			if(RotationUtils.get(axisTwo, minVec) <= pointTwo)
				return true;
		}
		// pointing negative
		else
		{
			if(RotationUtils.get(axisTwo, minVec) >= pointTwo)
				return true;
		}
		
		return false;
    }
    
    @Override
    public boolean intersects(AxisAlignedBB other)
    {
    	if(!super.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ))
			return false;
    	
		if(other.getClass() == AxisAlignedBB.class)
	    	return intersectsWithOrdinaryTwoAxis(other);
    	
        return false;
    }
    
    @Override
    public boolean contains(Vec3d vec)
    {
    	if(super.contains(vec))
		{
			Axis one = RotationUtils.getDifferentAxisFirst(slice.axis);
			Axis two = RotationUtils.getDifferentAxisSecond(slice.axis);
			
			BoxCorner corner = slice.getFilledCorner();
			
			double difOne = Math.abs(getCornerValue(corner, one) - RotationUtils.get(one, vec));
			double difTwo = Math.abs(getCornerValue(corner, two) - RotationUtils.get(two, vec));
			double sizeOne = getSize(one);
			double sizeTwo = getSize(two);
			double diff = difOne / sizeOne + difTwo / sizeTwo;
			return sizeOne >= difOne && sizeTwo >= difTwo && diff < 1;
		}
		return false;
    }
    
    @Nullable
    protected Vec3d collideWithPlane(Axis axis, double value, Vec3d vecA, Vec3d vecB)
    {
        Vec3d vec3d = axis != Axis.X ? axis != Axis.Y ? vecA.getIntermediateWithZValue(vecB, value) : vecA.getIntermediateWithYValue(vecB, value) : vecA.getIntermediateWithXValue(vecB, value);
        return vec3d != null && intersectsWithAxis(axis, vec3d)  ? vec3d : null;
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
    
    @Nullable
    @Override
    public RayTraceResult calculateIntercept(Vec3d vecA, Vec3d vecB)
    {
    	Vec3d collision = null;
		EnumFacing collided = null;
		
		for (EnumFacing facing : EnumFacing.VALUES) {
			if(slice.emptySideOne != facing && slice.emptySideTwo != facing)
			{
				Vec3d temp = collideWithPlane(facing.getAxis(), (double) getValueOfFacing(facing), vecA, vecB);
				if(temp != null && isClosest(vecA, collision, temp))
				{
					collided = facing;
					collision = temp;
				}
			}
		}
		
		EnumFacing diagonal = slice.getPreferedSide(getSize());
		Vec3d temp = LittleTileSlicedOrdinaryBox.linePlaneIntersection(getCorner(BoxCorner.getCornerUnsorted(RotationUtils.getFacing(slice.axis), slice.emptySideOne, slice.emptySideTwo.getOpposite())), slice.getNormalVec(), vecA, vecB.subtract(vecA));
		if(temp != null && intersectsWithAxis(diagonal.getAxis(), temp) && isClosest(vecA, collision, temp))
		{
			collision = temp;
			collided = diagonal.getAxisDirection() == AxisDirection.POSITIVE ? diagonal.getOpposite() : diagonal;
		}
		
		if(collision == null)
			return null;
		
        return new RayTraceResult(collision, collided);
    }
    
    @Override
    public String toString()
    {
        return "slicedbox[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + ", " + this.slice + "]";
    }
}
