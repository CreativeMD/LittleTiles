package team.creative.littletiles.common.math.box;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.CreativeAABB;
import team.creative.creativecore.common.util.math.box.OBB;
import team.creative.creativecore.common.util.math.geo.NormalPlane;
import team.creative.creativecore.common.util.math.geo.Ray3f;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanCache;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanFaceCache;

public class TransformableAABB extends CreativeAABB {
    
    private LittleGrid grid;
    private LittleBox box;
    
    public TransformableAABB(LittleBox box, LittleGrid grid, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.box = box;
        this.grid = grid;
    }
    
    @Override
    public TransformableAABB setMaxY(double y2) {
        return new TransformableAABB(box, grid, this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object.getClass() == TransformableAABB.class) {
            return ((TransformableAABB) object).minX == this.minX && ((TransformableAABB) object).minY == this.minY && ((TransformableAABB) object).minZ == this.minZ && ((TransformableAABB) object).maxX == this.maxX && ((TransformableAABB) object).maxY == this.maxY && ((TransformableAABB) object).maxZ == this.maxZ && ((TransformableAABB) object).grid == this.grid && ((TransformableAABB) object).box == this.box;
        }
        return false;
    }
    
    @Override
    public TransformableAABB contract(double p_191195_1_, double p_191195_3_, double p_191195_5_) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;
        
        if (p_191195_1_ < 0.0D) {
            d0 -= p_191195_1_;
        } else if (p_191195_1_ > 0.0D) {
            d3 -= p_191195_1_;
        }
        
        if (p_191195_3_ < 0.0D) {
            d1 -= p_191195_3_;
        } else if (p_191195_3_ > 0.0D) {
            d4 -= p_191195_3_;
        }
        
        if (p_191195_5_ < 0.0D) {
            d2 -= p_191195_5_;
        } else if (p_191195_5_ > 0.0D) {
            d5 -= p_191195_5_;
        }
        
        return new TransformableAABB(box, grid, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public AABB inflate(double x, double y, double z) {
        double d0 = this.minX - x;
        double d1 = this.minY - y;
        double d2 = this.minZ - z;
        double d3 = this.maxX + x;
        double d4 = this.maxY + y;
        double d5 = this.maxZ + z;
        return new TransformableAABB(box, grid, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public TransformableAABB expandTowards(double x, double y, double z) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;
        
        if (x < 0.0D) {
            d0 += x;
        } else if (x > 0.0D) {
            d3 += x;
        }
        
        if (y < 0.0D) {
            d1 += y;
        } else if (y > 0.0D) {
            d4 += y;
        }
        
        if (z < 0.0D) {
            d2 += z;
        } else if (z > 0.0D) {
            d5 += z;
        }
        
        return new TransformableAABB(box, grid, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public TransformableAABB intersect(AABB p_191500_1_) {
        double d0 = Math.max(this.minX, p_191500_1_.minX);
        double d1 = Math.max(this.minY, p_191500_1_.minY);
        double d2 = Math.max(this.minZ, p_191500_1_.minZ);
        double d3 = Math.min(this.maxX, p_191500_1_.maxX);
        double d4 = Math.min(this.maxY, p_191500_1_.maxY);
        double d5 = Math.min(this.maxZ, p_191500_1_.maxZ);
        return new TransformableAABB(box, grid, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public AABB move(double x, double y, double z) {
        return new TransformableAABB(box, grid, this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
    
    @Override
    public AABB move(BlockPos pos) {
        return new TransformableAABB(box, grid, this.minX + pos.getX(), this.minY + pos.getY(), this.minZ + pos.getZ(), this.maxX + pos.getX(), this.maxY + pos
                .getY(), this.maxZ + pos.getZ());
    }
    
    public VectorFanFaceCache getFaceCache(Facing facing) {
        VectorFanCache cache = ((LittleTransformableBox) box).requestCache();
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    public double calculateOffset(AABB other, Axis axis, double offset) {
        if (offset == 0)
            return offset;
        
        boolean positive = offset > 0;
        Facing direction = Facing.get(axis, positive);
        
        Axis one = axis.one();
        Axis two = axis.two();
        float minOne = (float) getMin(other, one);
        minOne -= Math.floor(getMin(one));
        minOne *= grid.count;
        float minTwo = (float) getMin(other, two);
        minTwo -= Math.floor(getMin(two));
        minTwo *= grid.count;
        float maxOne = (float) getMax(other, one);
        maxOne -= Math.floor(getMin(one));
        maxOne *= grid.count;
        float maxTwo = (float) getMax(other, two);
        maxTwo -= Math.floor(getMin(two));
        maxTwo *= grid.count;
        
        float otherAxis = (float) (offset > 0 ? getMax(other, axis) : getMin(other, axis));
        otherAxis -= Math.floor(getMin(axis));
        otherAxis *= grid.count;
        
        NormalPlane[] cuttingPlanes = new NormalPlane[] { new NormalPlane(one, minOne, Facing.get(one, false)), new NormalPlane(two, minTwo, Facing
                .get(two, false)), new NormalPlane(one, maxOne, Facing.get(one, true)), new NormalPlane(two, maxTwo, Facing.get(two, true)) };
        
        VectorFan tempFan = new VectorFan(null);
        VectorFanFaceCache front = getFaceCache(direction.opposite());
        if (front.hasAxisStrip()) {
            for (VectorFan vectorFan : front.axisStrips) {
                tempFan.set(vectorFan);
                if (tempFan.cutWithoutCopy(cuttingPlanes)) {
                    if (offset > 0.0D && getMax(other, axis) <= getMin(axis)) {
                        double d1 = getMin(axis) - getMax(other, axis);
                        
                        if (d1 < offset)
                            return d1;
                    } else if (offset < 0.0D && getMin(other, axis) >= getMax(axis)) {
                        double d0 = getMax(axis) - getMin(other, axis);
                        
                        if (d0 > offset)
                            return d0;
                        
                    }
                    return offset;
                }
            }
        }
        
        Ray3f ray = new Ray3f(new Vec3f(), direction);
        ray.origin.set(axis, otherAxis);
        float distance = Float.POSITIVE_INFINITY;
        
        for (int i = 0; i < Facing.values().length; i++) {
            Facing facing = Facing.values()[i];
            if (facing == direction)
                continue;
            
            VectorFanFaceCache face = getFaceCache(facing);
            if (!face.hasTiltedStrip())
                continue;
            
            for (VectorFan vectorFan : face.tilted()) {
                tempFan.set(vectorFan);
                tempFan.cutWithoutCopy(cuttingPlanes);
                if (tempFan.isEmpty())
                    continue;
                
                for (int j = 0; j < tempFan.count(); j++) {
                    Vec3f vec = tempFan.get(j);
                    float tempDistance = positive ? vec.get(axis) - otherAxis : otherAxis - vec.get(axis);
                    
                    if (tempDistance < 0 && !OBB.equals(tempDistance, 0))
                        return offset;
                    
                    if (tempDistance < distance)
                        distance = tempDistance;
                }
            }
        }
        
        if (Double.isInfinite(distance))
            return offset;
        
        distance *= grid.pixelLength;
        
        if (offset > 0.0D) {
            if (distance < offset)
                return distance;
            return offset;
        } else if (offset < 0.0D) {
            if (-distance > offset)
                return -distance;
            return offset;
        }
        return offset;
    }
    
    @Override
    public String toString() {
        return "tbb[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }
    
}
