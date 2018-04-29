package com.creativemd.littletiles.common.entity;

import javax.annotation.Nullable;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.collision.CreativeAxisAlignedBB;
import com.creativemd.creativecore.common.utils.BoxUtils;
import com.creativemd.creativecore.common.utils.IVecOrigin;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.common.tiles.vec.lines.LittleTile2DLine;
import com.google.common.annotations.VisibleForTesting;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAABB extends CreativeAxisAlignedBB {
	
	public IVecOrigin origin;
	
	public EntityAABB(IVecOrigin origin, double x1, double y1, double z1, double x2, double y2, double z2) {
		super(x1, y1, z1, x2, y2, z2);
		this.origin = origin;
	}
	
	public EntityAABB(IVecOrigin origin, AxisAlignedBB bb) {
		super(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
		this.origin = origin;
	}
	
	public double getRealMinX()
	{
		return this.minX + origin.offX();
	}
	
	public double getRealMinY()
	{
		return this.minY + origin.offY();
	}
	
	public double getRealMinZ()
	{
		return this.minZ + origin.offZ();
	}
	
	public double getRealMaxX()
	{
		return this.maxX + origin.offX();
	}
	
	public double getRealMaxY()
	{
		return this.maxY + origin.offY();
	}
	
	public double getRealMaxZ()
	{
		return this.maxZ + origin.offZ();
	}
	
	@Override
    public EntityAABB setMaxY(double y2)
    {
        return new EntityAABB(origin, this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ);
    }

	@Override
    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (!(p_equals_1_ instanceof EntityAABB))
        {
            return false;
        }
        else
        {
        	EntityAABB axisalignedbb = (EntityAABB) p_equals_1_;
        	
        	if(axisalignedbb.origin != origin)
        	{
        		return false;
        	}
        	else if (Double.compare(axisalignedbb.minX, this.minX) != 0)
            {
                return false;
            }
            else if (Double.compare(axisalignedbb.minY, this.minY) != 0)
            {
                return false;
            }
            else if (Double.compare(axisalignedbb.minZ, this.minZ) != 0)
            {
                return false;
            }
            else if (Double.compare(axisalignedbb.maxX, this.maxX) != 0)
            {
                return false;
            }
            else if (Double.compare(axisalignedbb.maxY, this.maxY) != 0)
            {
                return false;
            }
            else
            {
                return Double.compare(axisalignedbb.maxZ, this.maxZ) == 0;
            }
        }
    }

    @Override
    public EntityAABB contract(double x, double y, double z)
    {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D)
        {
            d0 -= x;
        }
        else if (x > 0.0D)
        {
            d3 -= x;
        }

        if (y < 0.0D)
        {
            d1 -= y;
        }
        else if (y > 0.0D)
        {
            d4 -= y;
        }

        if (z < 0.0D)
        {
            d2 -= z;
        }
        else if (z > 0.0D)
        {
            d5 -= z;
        }

        return new EntityAABB(origin, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public EntityAABB addCoord(double x, double y, double z)
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

        return new EntityAABB(origin, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public EntityAABB expand(double x, double y, double z)
    {
        double d0 = this.minX - x;
        double d1 = this.minY - y;
        double d2 = this.minZ - z;
        double d3 = this.maxX + x;
        double d4 = this.maxY + y;
        double d5 = this.maxZ + z;
        return new EntityAABB(origin, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public EntityAABB expandXyz(double value)
    {
        return this.expand(value, value, value);
    }
    
    @Override
    public AxisAlignedBB intersect(AxisAlignedBB other)
    {
    	if(other instanceof EntityAABB)
    	{
    		EntityAABB otherBB = (EntityAABB) other;
    		if(otherBB.origin == origin)
    		{
    			double d0 = Math.max(this.minX, other.minX);
                double d1 = Math.max(this.minY, other.minY);
                double d2 = Math.max(this.minZ, other.minZ);
                double d3 = Math.min(this.maxX, other.maxX);
                double d4 = Math.min(this.maxY, other.maxY);
                double d5 = Math.min(this.maxZ, other.maxZ);
	            return new EntityAABB(origin, d0, d1, d2, d3, d4, d5);
    		}else{
    			double d0 = Math.max(this.getRealMinX(), otherBB.getRealMinX());
	            double d1 = Math.max(this.getRealMinY(), otherBB.getRealMinY());
	            double d2 = Math.max(this.getRealMinZ(), otherBB.getRealMinZ());
	            double d3 = Math.min(this.getRealMaxX(), otherBB.getRealMaxX());
	            double d4 = Math.min(this.getRealMaxY(), otherBB.getRealMaxY());
	            double d5 = Math.min(this.getRealMaxZ(), otherBB.getRealMaxZ());
	            return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    		}
    	}
		double d0 = Math.max(this.getRealMinX(), other.minX);
        double d1 = Math.max(this.getRealMinY(), other.minY);
        double d2 = Math.max(this.getRealMinZ(), other.minZ);
        double d3 = Math.min(this.getRealMaxX(), other.maxX);
        double d4 = Math.min(this.getRealMaxY(), other.maxY);
        double d5 = Math.min(this.getRealMaxZ(), other.maxZ);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public AxisAlignedBB union(AxisAlignedBB other)
    {
    	if(other instanceof EntityAABB)
    	{
    		EntityAABB otherBB = (EntityAABB) other;
    		if(otherBB.origin == origin)
    		{
    			double d0 = Math.min(this.minX, other.minX);
    	        double d1 = Math.min(this.minY, other.minY);
    	        double d2 = Math.min(this.minZ, other.minZ);
    	        double d3 = Math.max(this.maxX, other.maxX);
    	        double d4 = Math.max(this.maxY, other.maxY);
    	        double d5 = Math.max(this.maxZ, other.maxZ);
	            return new EntityAABB(origin, d0, d1, d2, d3, d4, d5);
    		}else{
    			double d0 = Math.min(this.getRealMinX(), otherBB.getRealMinX());
    	        double d1 = Math.min(this.getRealMinY(), otherBB.getRealMinY());
    	        double d2 = Math.min(this.getRealMinZ(), otherBB.getRealMinZ());
    	        double d3 = Math.max(this.getRealMaxX(), otherBB.getRealMaxX());
    	        double d4 = Math.max(this.getRealMaxY(), otherBB.getRealMaxY());
    	        double d5 = Math.max(this.getRealMaxZ(), otherBB.getRealMaxZ());
	            return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    		}
    	}
		double d0 = Math.min(this.getRealMinX(), other.minX);
        double d1 = Math.min(this.getRealMinY(), other.minY);
        double d2 = Math.min(this.getRealMinZ(), other.minZ);
        double d3 = Math.max(this.getRealMaxX(), other.maxX);
        double d4 = Math.max(this.getRealMaxY(), other.maxY);
        double d5 = Math.max(this.getRealMaxZ(), other.maxZ);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    @Override
    public EntityAABB offset(double x, double y, double z)
    {
        return new EntityAABB(origin, this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
    
    @Override
    public EntityAABB offset(BlockPos pos)
    {
        return new EntityAABB(origin, this.minX + (double)pos.getX(), this.minY + (double)pos.getY(), this.minZ + (double)pos.getZ(), this.maxX + (double)pos.getX(), this.maxY + (double)pos.getY(), this.maxZ + (double)pos.getZ());
    }

    @Override
    public EntityAABB move(Vec3d vec)
    {
        return this.offset(vec.xCoord, vec.yCoord, vec.zCoord);
    }
    
    /**
     * @return -1 -> value is too small; 0 -> value is inside min and max; 1 -> value is too large
     */
    private static int getCornerOffset(double value, double min, double max)
    {
    	if(value <= min)
    		return -1;
    	else if(value >= max)
    		return 1;
    	return 0;
    }
    
    private static boolean isFurtherOrEqualThan(double value, double toCheck)
    {
    	if(value < 0)
    		return toCheck <= value;
    	return toCheck >= value;
    }
    
    /**
     * @return if result is negative there should be no collision
     */
    public double calculateDistanceRotated(AxisAlignedBB other, Axis axis, double offset)
    {
    	boolean positive = offset > 0;
    	EnumFacing facing = EnumFacing.getFacingFromAxis(!positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, axis);
    	double closestValue = getValueOfFacing(other, facing.getOpposite());
    	Vector3d[] corners = BoxUtils.getOuterCorner(facing, origin, this);
    	
    	Axis one = RotationUtils.getDifferentAxisFirst(axis);
    	Axis two = RotationUtils.getDifferentAxisSecond(axis);
    	
    	double minOne = getMin(other, one);
    	double minTwo = getMin(other, two);
    	double maxOne = getMax(other, one);
    	double maxTwo = getMax(other, two);
    	
    	Vector3d outerCorner = corners[0];
    	double outerCornerOne = RotationUtils.get(one, outerCorner);
    	double outerCornerTwo = RotationUtils.get(two, outerCorner);
    	double outerCornerAxis = RotationUtils.get(axis, outerCorner);
    	
    	int outerCornerOffsetOne = getCornerOffset(outerCornerOne, minOne, maxOne);
    	int outerCornerOffsetTwo = getCornerOffset(outerCornerTwo, minTwo, maxTwo);
    	
    	if(outerCornerOffsetOne == 0 && outerCornerOffsetTwo == 0) // Hits the outer corner
    	{
    		if(positive)
    			return RotationUtils.get(axis, outerCorner) - closestValue;
    		return closestValue - RotationUtils.get(axis, outerCorner);
    	}
    	
    	double minDistance = Double.MAX_VALUE;
    	
    	Vector2d[] directions = new Vector2d[3];
    	
    	for (int i = 1; i <= 3; i++) { // Check all lines which connect to the outer corner
    		
    		Vector3d corner = corners[i];
    		
    		LittleTile2DLine line = new LittleTile2DLine(one, two, outerCorner, RotationUtils.get(one, corner) - outerCornerOne, RotationUtils.get(two, corner) - outerCornerTwo);
    		directions[i-1] = new Vector2d(line.directionOne, line.directionTwo);
    		
    		int cornerOffsetOne = getCornerOffset(RotationUtils.get(one, corner), minOne, maxOne);
    		if(outerCornerOffsetOne != 0 && outerCornerOffsetOne == cornerOffsetOne)
    			continue;
    		
    		int cornerOffsetTwo = getCornerOffset(RotationUtils.get(two, corner), minTwo, maxTwo);
    		if(outerCornerOffsetTwo != 0 && outerCornerOffsetTwo == cornerOffsetTwo)
    			continue;
    		
    		double axisStart = RotationUtils.get(axis, outerCorner);
    		double axisDirection = RotationUtils.get(axis, corner) - axisStart;
    		
    		if(outerCornerOffsetOne == -1)
    		{
    			double coordinateTwo = line.get(one, minOne);
    			if(coordinateTwo > minTwo && coordinateTwo < maxTwo)
    			{ 
    				double valueAxis = axisStart + ((minOne - line.originOne) / line.directionOne) * axisDirection;
    				double distance = positive ? valueAxis - closestValue : closestValue - valueAxis;
    				
    				if(distance < 0)
    					return distance;
    				
    				minDistance = Math.min(distance, minDistance);
    			}
    		}
    		else if(outerCornerOffsetOne == 1)
    		{
    			double coordinateTwo = line.get(one, maxOne);
    			if(coordinateTwo > minTwo && coordinateTwo < maxTwo)
    			{ 
    				double valueAxis = axisStart + ((maxOne - line.originOne) / line.directionOne) * axisDirection;
    				double distance = positive ? valueAxis - closestValue : closestValue - valueAxis;
    				
    				if(distance < 0)
	    				return distance;
    				
    				minDistance = Math.min(distance, minDistance);
    			}
    		}
    		
    		if(outerCornerOffsetTwo == -1)
    		{
    			double coordinateOne = line.get(two, minTwo);
    			if(coordinateOne > minOne && coordinateOne < maxOne)
    			{ 
    				double valueAxis = axisStart + ((minTwo - line.originTwo) / line.directionTwo) * axisDirection;
    				double distance = positive ? valueAxis - closestValue : closestValue - valueAxis;
    				
    				if(distance < 0)
    					return distance;
    				
    				minDistance = Math.min(distance, minDistance);
    			}
    		}
    		else if(outerCornerOffsetTwo == 1)
    		{
    			double coordinateOne = line.get(two, maxTwo);
    			if(coordinateOne > minOne && coordinateOne < maxOne)
    			{ 
    				double valueAxis = axisStart + ((maxTwo - line.originTwo) / line.directionTwo) * axisDirection;
    				double distance = positive ? valueAxis - closestValue : closestValue - valueAxis;
    				
    				if(distance < 0)
	    				return distance;
    				
    				minDistance = Math.min(distance, minDistance);
    			}
    		}
		}
    	
		boolean minOneOffset = outerCornerOne > minOne;
		boolean minTwoOffset = outerCornerTwo > minTwo;
		boolean maxOneOffset = outerCornerOne > maxOne;
		boolean maxTwoOffset = outerCornerTwo > maxTwo;
		
		Vector2d[] vectors;
		
		if(minOneOffset == minTwoOffset && maxOneOffset == maxTwoOffset) 
			vectors = new Vector2d[] {new Vector2d((minOneOffset ? maxOne : minOne) - outerCornerOne, (minTwoOffset ? maxTwo : minTwo) - outerCornerTwo)};
		else if(minOneOffset == maxOneOffset)
			vectors = new Vector2d[] {new Vector2d((minOneOffset ? maxOne : minOne) - outerCornerOne, minTwo - outerCornerTwo), new Vector2d((minOneOffset ? maxOne : minOne) - outerCornerOne, maxTwo - outerCornerTwo)};
		else if(minTwoOffset == maxTwoOffset) 
			vectors = new Vector2d[] {new Vector2d(minOne - outerCornerOne, (minTwoOffset ? maxTwo : minTwo) - outerCornerTwo), new Vector2d(maxOne - outerCornerOne, (minTwoOffset ? maxTwo : minTwo) - outerCornerTwo)};
		else
			vectors = new Vector2d[] {}; // that one cannot exist {new Vector2d(minOne, minTwo), new Vector2d(maxOne, minTwo), new Vector2d(minOne, maxTwo), new Vector2d(maxOne, maxTwo)};		
		
		for (int i = 0; i < 3; i++) { // Calculate faces
			
			int indexFirst = i;
			int indexSecond = i == 2 ? 0 : i + 1;
			
			Vector2d first = directions[indexFirst];
			Vector2d second = directions[indexSecond];
			
			if(first.x == 0 || second.y == 0)
			{
				int temp = indexFirst;
				indexFirst = indexSecond;
				indexSecond = temp;
				first = directions[indexFirst];
				second = directions[indexSecond];
			}
			
			for (int j = 0; j < vectors.length; j++) {
				
				Vector2d vector = vectors[j];			
				
				if((isFurtherOrEqualThan(vector.x, first.x) || isFurtherOrEqualThan(vector.x, second.x) || isFurtherOrEqualThan(vector.x, first.x + second.x)) &&
						(isFurtherOrEqualThan(vector.y, first.y) || isFurtherOrEqualThan(vector.y, second.y) || isFurtherOrEqualThan(vector.y, first.y + second.y)))
				{					
					double t = (vector.x*second.y-vector.y*second.x)/(first.x*second.y-first.y*second.x);
					if(t <= 0 || t >= 1 || Double.isNaN(t))
						continue;
					
    				double s = (vector.y-t*first.y)/second.y;
    				if(s <= 0 || s >= 1 || Double.isNaN(s))
						continue;
    				
    				double valueAxis = outerCornerAxis + (RotationUtils.get(axis, corners[indexFirst+1]) - outerCornerAxis) * t + (RotationUtils.get(axis, corners[indexSecond+1]) - outerCornerAxis) * s;
    				double distance = positive ? valueAxis - closestValue : closestValue - valueAxis;
    				distance -= 0.00000000001;
    				
    				if(distance < 0)
    					continue;
    				
    				minDistance = Math.min(distance, minDistance);
				}
			}
			
		}
    	
		if(minDistance == Double.MAX_VALUE)
			return -1;
    	
    	return minDistance;
    }
    
    public double calculateOffsetRotated(AxisAlignedBB other, Axis axis, double offset)
    {
    	if(offset == 0)
    		return offset;
    	
    	double distance = calculateDistanceRotated(other, axis, offset);
    	
    	if(distance < 0)
    		return offset;
    	
    	if (offset > 0.0D)
        {
            if (distance < offset)
	            return distance;
            return offset;
        }
        else if (offset < 0.0D)
        {
            if (-distance > offset)
            	return -distance;
            return offset;
        }
        return offset;
    }

    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX)
    {
    	if(origin.isRotated())
    		return calculateOffsetRotated(other, Axis.X, offsetX);
    	
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return super.calculateXOffset(other, offsetX);
    		
			EntityAABB otherBB = (EntityAABB) other;
			
			if (otherBB.getRealMaxY() > this.getRealMinY() && otherBB.getRealMinY() < this.getRealMaxY() && otherBB.getRealMaxZ() > this.getRealMinZ() && otherBB.getRealMinZ() < this.getRealMaxZ())
	        {
	            if (offsetX > 0.0D && otherBB.getRealMaxX() <= this.getRealMinX())
	            {
	                double d1 = this.getRealMinX() - otherBB.getRealMaxX();

	                if (d1 < offsetX)
	                {
	                    offsetX = d1;
	                }
	            }
	            else if (offsetX < 0.0D && otherBB.getRealMinX() >= this.getRealMaxX())
	            {
	                double d0 = this.getRealMaxX() - otherBB.getRealMinX();

	                if (d0 > offsetX)
	                {
	                    offsetX = d0;
	                }
	            }

	            return offsetX;
	        }
			
	        return offsetX;
    	}
        if (other.maxY > this.getRealMinY() && other.minY < this.getRealMaxY() && other.maxZ > this.getRealMinZ() && other.minZ < this.getRealMaxZ())
        {
            if (offsetX > 0.0D && other.maxX <= this.getRealMinX())
            {
                double d1 = this.getRealMinX() - other.maxX;

                if (d1 < offsetX)
                {
                    offsetX = d1;
                }
            }
            else if (offsetX < 0.0D && other.minX >= this.getRealMaxX())
            {
                double d0 = this.getRealMaxX() - other.minX;

                if (d0 > offsetX)
                {
                    offsetX = d0;
                }
            }

            return offsetX;
        }
        
        return offsetX;
    }

    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY)
    {
    	if(origin.isRotated())
    		return calculateOffsetRotated(other, Axis.Y, offsetY);
    	
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return super.calculateYOffset(other, offsetY);
    		
			EntityAABB otherBB = (EntityAABB) other;
			
			if (otherBB.getRealMaxX() > this.getRealMinX() && otherBB.getRealMinX() < this.getRealMaxX() && otherBB.getRealMaxZ() > this.getRealMinZ() && otherBB.getRealMinZ() < this.getRealMaxZ())
	        {
	            if (offsetY > 0.0D && otherBB.getRealMaxY() <= this.getRealMinY())
	            {
	                double d1 = this.getRealMinY() - otherBB.getRealMaxY();

	                if (d1 < offsetY)
	                {
	                    offsetY = d1;
	                }
	            }
	            else if (offsetY < 0.0D && otherBB.getRealMinY() >= this.getRealMaxY())
	            {
	                double d0 = this.getRealMaxY() - otherBB.getRealMinY();

	                if (d0 > offsetY)
	                {
	                    offsetY = d0;
	                }
	            }

	            return offsetY;
	        }
			
	        return offsetY;
    	}
        if (other.maxX > this.getRealMinX() && other.minX < this.getRealMaxX() && other.maxZ > this.getRealMinZ() && other.minZ < this.getRealMaxZ())
        {
            if (offsetY > 0.0D && other.maxY <= this.getRealMinY())
            {
                double d1 = this.getRealMinY() - other.maxY;

                if (d1 < offsetY)
                {
                    offsetY = d1;
                }
            }
            else if (offsetY < 0.0D && other.minY >= this.getRealMaxY())
            {
                double d0 = this.getRealMaxY() - other.minY;

                if (d0 > offsetY)
                {
                    offsetY = d0;
                }
            }

            return offsetY;
        }
        
        return offsetY;
    }

    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ)
    {
    	if(origin.isRotated())
    		return calculateOffsetRotated(other, Axis.Z, offsetZ);
    	
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return super.calculateZOffset(other, offsetZ);
    		
			EntityAABB otherBB = (EntityAABB) other;
			
			if (otherBB.getRealMaxX() > this.getRealMinX() && otherBB.getRealMinX() < this.getRealMaxX() && otherBB.getRealMaxY() > this.getRealMinY() && otherBB.getRealMinY() < this.getRealMaxY())
	        {
	            if (offsetZ > 0.0D && otherBB.getRealMaxZ() <= this.getRealMinZ())
	            {
	                double d1 = this.getRealMinZ() - otherBB.getRealMaxZ();

	                if (d1 < offsetZ)
	                {
	                    offsetZ = d1;
	                }
	            }
	            else if (offsetZ < 0.0D && otherBB.getRealMinZ() >= this.getRealMaxZ())
	            {
	                double d0 = this.getRealMaxZ() - otherBB.getRealMinZ();

	                if (d0 > offsetZ)
	                {
	                    offsetZ = d0;
	                }
	            }

	            return offsetZ;
	        }
			
	        return offsetZ;
    	}
        if (other.maxX > this.getRealMinX() && other.minX < this.getRealMaxX() && other.maxY > this.getRealMinY() && other.minY < this.getRealMaxY())
        {
            if (offsetZ > 0.0D && other.maxZ <= this.getRealMinZ())
            {
                double d1 = this.getRealMinZ() - other.maxZ;

                if (d1 < offsetZ)
                {
                    offsetZ = d1;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= this.getRealMaxZ())
            {
                double d0 = this.getRealMaxZ() - other.minZ;

                if (d0 > offsetZ)
                {
                    offsetZ = d0;
                }
            }

            return offsetZ;
        }
        
        return offsetZ;
    }
    
    public boolean intersects(AxisAlignedBB other, double expandX, double expandY, double expandZ)
    {
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return this.minX + (expandX < 0 ? expandX : 0) < other.maxX && this.maxX + (expandX > 0 ? expandX : 0) > other.minX &&
    					this.minY + (expandY < 0 ? expandY : 0) < other.maxY && this.maxY + (expandY > 0 ? expandY : 0) > other.minY &&
    					this.minZ + (expandZ < 0 ? expandZ : 0) < other.maxZ && this.maxZ + (expandZ > 0 ? expandZ : 0) > other.minZ;
			else
				return this.intersectsExpand(expandX, expandY, expandZ, ((EntityAABB) other).getRealMinX(), ((EntityAABB) other).getRealMinY(), ((EntityAABB) other).getRealMinZ(), ((EntityAABB) other).getRealMaxX(), ((EntityAABB) other).getRealMaxY(), ((EntityAABB) other).getRealMaxZ());
    	}
        return this.intersectsExpand(expandX, expandY, expandZ, other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    @Override
    public boolean intersectsWith(AxisAlignedBB other)
    {
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return this.minX < other.maxX && this.maxX > other.minX && this.minY < other.maxY && this.maxY > other.minY && this.minZ < other.maxZ && this.maxZ > other.minZ;
			else
				return this.intersects(((EntityAABB) other).getRealMinX(), ((EntityAABB) other).getRealMinY(), ((EntityAABB) other).getRealMinZ(), ((EntityAABB) other).getRealMaxX(), ((EntityAABB) other).getRealMaxY(), ((EntityAABB) other).getRealMaxZ());
    	}
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }
    
    public boolean intersectsExpand(double expandX, double expandY, double expandZ, double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return this.getRealMinX() + (expandX < 0 ? expandX : 0) < x2 && this.getRealMaxX() + (expandX > 0 ? expandX : 0) > x1 &&
        		this.getRealMinY() + (expandY < 0 ? expandY : 0) < y2 && this.getRealMaxY() + (expandY > 0 ? expandY : 0) > y1 &&
        		this.getRealMinZ() + (expandZ < 0 ? expandZ : 0) < z2 && this.getRealMaxZ() + (expandZ > 0 ? expandZ : 0) > z1;
    }

    @Override
    public boolean intersects(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return this.getRealMinX() < x2 && this.getRealMaxX() > x1 && this.getRealMinY() < y2 && this.getRealMaxY() > y1 && this.getRealMinZ() < z2 && this.getRealMaxZ() > z1;
    }
    
    public double getDistanceX(AxisAlignedBB other)
    {
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return (this.minX >= other.maxX ? this.minX - other.maxX : this.maxX - other.minX);
			else
				return (this.getRealMinX() >= ((EntityAABB) other).getRealMaxX() ? this.getRealMinX() - ((EntityAABB) other).getRealMaxX() : this.getRealMaxX() - ((EntityAABB) other).getRealMinX());
    	}
    	return (this.getRealMinX() >= other.maxX ? this.getRealMinX() - other.maxX : this.getRealMaxX() - other.minX);
    }
    
    public double getDistanceY(AxisAlignedBB other)
    {
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return (this.minY >= other.maxY ? this.minY - other.maxY : this.maxY - other.minY);
			else
				return (this.getRealMinY() >= ((EntityAABB) other).getRealMaxY() ? this.getRealMinY() - ((EntityAABB) other).getRealMaxY() : this.getRealMaxY() - ((EntityAABB) other).getRealMinY());
    	}
    	return (this.getRealMinY() >= other.maxY ? this.getRealMinY() - other.maxY : this.getRealMaxY() - other.minY);
    }
    
    public double getDistanceZ(AxisAlignedBB other)
    {
    	if(other instanceof EntityAABB)
    	{
    		if(((EntityAABB) other).origin == origin)
    			return (this.minZ >= other.maxZ ? this.minZ - other.maxZ : this.maxZ - other.minZ);
			else
				return (this.getRealMinZ() >= ((EntityAABB) other).getRealMaxZ() ? this.getRealMinZ() - ((EntityAABB) other).getRealMaxZ() : this.getRealMaxZ() - ((EntityAABB) other).getRealMinZ());
    	}
    	return (this.getRealMinZ() >= other.maxZ ? this.getRealMinZ() - other.maxZ : this.getRealMaxZ() - other.minZ);
    }

    @Override
    public boolean isVecInside(Vec3d vec)
    {
        if (vec.xCoord > this.getRealMinX() && vec.xCoord < this.getRealMaxX())
        {
            if (vec.yCoord > this.getRealMinY() && vec.yCoord < this.getRealMaxY())
            {
                return vec.zCoord > this.getRealMinZ() && vec.zCoord < this.getRealMaxZ();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    @Nullable
    public RayTraceResult calculateIntercept(Vec3d vecA, Vec3d vecB)
    {
        Vec3d vec3d = this.collideWithXPlane2(this.getRealMinX(), vecA, vecB);
        EnumFacing enumfacing = EnumFacing.WEST;
        Vec3d vec3d1 = this.collideWithXPlane2(this.getRealMaxX(), vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1))
        {
            vec3d = vec3d1;
            enumfacing = EnumFacing.EAST;
        }

        vec3d1 = this.collideWithYPlane2(this.getRealMinY(), vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1))
        {
            vec3d = vec3d1;
            enumfacing = EnumFacing.DOWN;
        }

        vec3d1 = this.collideWithYPlane2(this.getRealMaxY(), vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1))
        {
            vec3d = vec3d1;
            enumfacing = EnumFacing.UP;
        }

        vec3d1 = this.collideWithZPlane2(this.getRealMinZ(), vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1))
        {
            vec3d = vec3d1;
            enumfacing = EnumFacing.NORTH;
        }

        vec3d1 = this.collideWithZPlane2(this.getRealMaxZ(), vecA, vecB);

        if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1))
        {
            vec3d = vec3d1;
            enumfacing = EnumFacing.SOUTH;
        }

        return vec3d == null ? null : new RayTraceResult(vec3d, enumfacing);
    }

    @Nullable
    @VisibleForTesting
    protected Vec3d collideWithXPlane2(double p_186671_1_, Vec3d p_186671_3_, Vec3d p_186671_4_)
    {
        Vec3d vec3d = p_186671_3_.getIntermediateWithXValue(p_186671_4_, p_186671_1_);
        return vec3d != null && this.intersectsWithYZ(vec3d) ? vec3d : null;
    }

    @Nullable
    @VisibleForTesting
    protected Vec3d collideWithYPlane2(double p_186663_1_, Vec3d p_186663_3_, Vec3d p_186663_4_)
    {
        Vec3d vec3d = p_186663_3_.getIntermediateWithYValue(p_186663_4_, p_186663_1_);
        return vec3d != null && this.intersectsWithXZ(vec3d) ? vec3d : null;
    }

    @Nullable
    @VisibleForTesting
    protected Vec3d collideWithZPlane2(double p_186665_1_, Vec3d p_186665_3_, Vec3d p_186665_4_)
    {
        Vec3d vec3d = p_186665_3_.getIntermediateWithZValue(p_186665_4_, p_186665_1_);
        return vec3d != null && this.intersectsWithXY(vec3d) ? vec3d : null;
    }

    @Override
    @VisibleForTesting
    public boolean intersectsWithYZ(Vec3d vec)
    {
        return vec.yCoord >= this.getRealMinY() && vec.yCoord <= this.getRealMaxY() && vec.zCoord >= this.getRealMinZ() && vec.zCoord <= this.getRealMaxZ();
    }

    @Override
    @VisibleForTesting
    public boolean intersectsWithXZ(Vec3d vec)
    {
        return vec.xCoord >= this.getRealMinX() && vec.xCoord <= this.getRealMaxX() && vec.zCoord >= this.getRealMinZ() && vec.zCoord <= this.getRealMaxZ();
    }

    @Override
    @VisibleForTesting
    public boolean intersectsWithXY(Vec3d vec)
    {
        return vec.xCoord >= this.getRealMinX() && vec.xCoord <= this.getRealMaxX() && vec.yCoord >= this.getRealMinY() && vec.yCoord <= this.getRealMaxY();
    }

    @Override
    public String toString()
    {
        return "realbox[" + this.getRealMinX() + ", " + this.getRealMinY() + ", " + this.getRealMinZ() + " -> " + this.getRealMaxX() + ", " + this.getRealMaxY() + ", " + this.getRealMaxZ() + "],box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getCenter()
    {
        return new Vec3d(origin.offX() + this.minX + (this.maxX - this.minX) * 0.5D, origin.offY() + this.minY + (this.maxY - this.minY) * 0.5D, origin.offZ() + this.minZ + (this.maxZ - this.minZ) * 0.5D);
    }
    
}
