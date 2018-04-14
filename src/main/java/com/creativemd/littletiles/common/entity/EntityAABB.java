package com.creativemd.littletiles.common.entity;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.collision.CreativeAxisAlignedBB;
import com.creativemd.creativecore.common.utils.IVecOrigin;
import com.creativemd.creativecore.common.world.WorldFake;
import com.google.common.annotations.VisibleForTesting;

import net.minecraft.util.EnumFacing;
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
    public EntityAABB expand(double x, double y, double z)
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
    public EntityAABB grow(double x, double y, double z)
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
    public EntityAABB grow(double value)
    {
        return this.grow(value, value, value);
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
    public EntityAABB offset(Vec3d vec)
    {
        return this.offset(vec.x, vec.y, vec.z);
    }

    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX)
    {
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
    public boolean intersects(AxisAlignedBB other)
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
    public boolean contains(Vec3d vec)
    {
        if (vec.x > this.getRealMinX() && vec.x < this.getRealMaxX())
        {
            if (vec.y > this.getRealMinY() && vec.y < this.getRealMaxY())
            {
                return vec.z > this.getRealMinZ() && vec.z < this.getRealMaxZ();
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

    @VisibleForTesting
    protected static boolean isClosest(Vec3d p_186661_1_, @Nullable Vec3d p_186661_2_, Vec3d p_186661_3_)
    {
        return p_186661_2_ == null || p_186661_1_.squareDistanceTo(p_186661_3_) < p_186661_1_.squareDistanceTo(p_186661_2_);
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
        return vec.y >= this.getRealMinY() && vec.y <= this.getRealMaxY() && vec.z >= this.getRealMinZ() && vec.z <= this.getRealMaxZ();
    }

    @Override
    @VisibleForTesting
    public boolean intersectsWithXZ(Vec3d vec)
    {
        return vec.x >= this.getRealMinX() && vec.x <= this.getRealMaxX() && vec.z >= this.getRealMinZ() && vec.z <= this.getRealMaxZ();
    }

    @Override
    @VisibleForTesting
    public boolean intersectsWithXY(Vec3d vec)
    {
        return vec.x >= this.getRealMinX() && vec.x <= this.getRealMaxX() && vec.y >= this.getRealMinY() && vec.y <= this.getRealMaxY();
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
