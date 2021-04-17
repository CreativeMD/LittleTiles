package com.creativemd.littletiles.common.tile.math.box;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.math.box.CreativeAxisAlignedBB;
import com.creativemd.creativecore.common.utils.math.box.OrientatedBoundingBox;
import com.creativemd.creativecore.common.utils.math.geo.NormalPlane;
import com.creativemd.creativecore.common.utils.math.geo.Ray3f;
import com.creativemd.creativecore.common.utils.math.vec.VectorFan;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox.VectorFanCache;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox.VectorFanFaceCache;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TransformableAxisBox extends CreativeAxisAlignedBB {
    
    private LittleGridContext context;
    private LittleBox box;
    
    public TransformableAxisBox(LittleBox box, LittleGridContext context, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.box = box;
        this.context = context;
    }
    
    @Override
    public TransformableAxisBox setMaxY(double y2) {
        return new TransformableAxisBox(box, context, this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ);
    }
    
    @Override
    public boolean equals(Object object) {
        if (object.getClass() == TransformableAxisBox.class) {
            return ((TransformableAxisBox) object).minX == this.minX && ((TransformableAxisBox) object).minY == this.minY && ((TransformableAxisBox) object).minZ == this.minZ && ((TransformableAxisBox) object).maxX == this.maxX && ((TransformableAxisBox) object).maxY == this.maxY && ((TransformableAxisBox) object).maxZ == this.maxZ && ((TransformableAxisBox) object).context == this.context && ((TransformableAxisBox) object).box == this.box;
        }
        return false;
    }
    
    @Override
    public TransformableAxisBox contract(double p_191195_1_, double p_191195_3_, double p_191195_5_) {
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
        
        return new TransformableAxisBox(box, context, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public TransformableAxisBox expand(double x, double y, double z) {
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
        
        return new TransformableAxisBox(box, context, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public AxisAlignedBB grow(double x, double y, double z) {
        double d0 = this.minX - x;
        double d1 = this.minY - y;
        double d2 = this.minZ - z;
        double d3 = this.maxX + x;
        double d4 = this.maxY + y;
        double d5 = this.maxZ + z;
        return new TransformableAxisBox(box, context, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public TransformableAxisBox intersect(AxisAlignedBB p_191500_1_) {
        double d0 = Math.max(this.minX, p_191500_1_.minX);
        double d1 = Math.max(this.minY, p_191500_1_.minY);
        double d2 = Math.max(this.minZ, p_191500_1_.minZ);
        double d3 = Math.min(this.maxX, p_191500_1_.maxX);
        double d4 = Math.min(this.maxY, p_191500_1_.maxY);
        double d5 = Math.min(this.maxZ, p_191500_1_.maxZ);
        return new TransformableAxisBox(box, context, d0, d1, d2, d3, d4, d5);
    }
    
    @Override
    public AxisAlignedBB offset(double x, double y, double z) {
        return new TransformableAxisBox(box, context, this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
    
    @Override
    public AxisAlignedBB offset(BlockPos pos) {
        return new TransformableAxisBox(box, context, this.minX + pos.getX(), this.minY + pos.getY(), this.minZ + pos.getZ(), this.maxX + pos.getX(), this.maxY + pos
            .getY(), this.maxZ + pos.getZ());
    }
    
    @Override
    public double calculateYOffsetStepUp(AxisAlignedBB other, AxisAlignedBB otherY, double offset) {
        double newOffset = calculateYOffset(otherY, offset);
        if (offset > 0) {
            if (newOffset < offset)
                return newOffset / 2;
        } else {
            if (newOffset > offset)
                return newOffset / 2;
        }
        
        return newOffset;
    }
    
    public VectorFanFaceCache getFaceCache(EnumFacing facing) {
        VectorFanCache cache = ((LittleTransformableBox) box).requestCache();
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    public double calculateOffset(AxisAlignedBB other, Axis axis, double offset) {
        if (offset == 0)
            return offset;
        
        boolean positive = offset > 0;
        EnumFacing direction = EnumFacing.getFacingFromAxis(positive ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE, axis);
        
        Axis one = RotationUtils.getOne(axis);
        Axis two = RotationUtils.getTwo(axis);
        float minOne = (float) getMin(other, one);
        minOne -= Math.floor(getMin(one));
        minOne *= context.size;
        float minTwo = (float) getMin(other, two);
        minTwo -= Math.floor(getMin(two));
        minTwo *= context.size;
        float maxOne = (float) getMax(other, one);
        maxOne -= Math.floor(getMin(one));
        maxOne *= context.size;
        float maxTwo = (float) getMax(other, two);
        maxTwo -= Math.floor(getMin(two));
        maxTwo *= context.size;
        
        float otherAxis = (float) (offset > 0 ? getMax(other, axis) : getMin(other, axis));
        otherAxis -= Math.floor(getMin(axis));
        otherAxis *= context.size;
        
        NormalPlane[] cuttingPlanes = new NormalPlane[] { new NormalPlane(one, minOne, EnumFacing
            .getFacingFromAxis(AxisDirection.NEGATIVE, one)),
                new NormalPlane(two, minTwo, EnumFacing
                    .getFacingFromAxis(AxisDirection.NEGATIVE, two)),
                new NormalPlane(one, maxOne, EnumFacing
                    .getFacingFromAxis(AxisDirection.POSITIVE, one)),
                new NormalPlane(two, maxTwo, EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, two)) };
        
        VectorFan tempFan = new VectorFan(null);
        VectorFanFaceCache front = getFaceCache(direction.getOpposite());
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
        
        Ray3f ray = new Ray3f(new Vector3f(), direction);
        VectorUtils.set(ray.origin, otherAxis, axis);
        float distance = Float.POSITIVE_INFINITY;
        
        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            EnumFacing facing = EnumFacing.VALUES[i];
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
                    Vector3f vec = tempFan.get(j);
                    float tempDistance = positive ? VectorUtils.get(axis, vec) - otherAxis : otherAxis - VectorUtils.get(axis, vec);
                    
                    if (tempDistance < 0 && !OrientatedBoundingBox.equals(tempDistance, 0))
                        return offset;
                    
                    if (tempDistance < distance)
                        distance = tempDistance;
                }
            }
        }
        
        if (Double.isInfinite(distance))
            return offset;
        
        distance *= context.pixelSize;
        
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
    
    private static Vector3f[] reverse(Vector3f[] array) {
        Vector3f[] b = new Vector3f[array.length];
        int j = array.length;
        for (int i = 0; i < array.length; i++) {
            b[j - 1] = array[i];
            j = j - 1;
        }
        return b;
    }
    
    @Override
    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        return calculateOffset(other, Axis.X, offsetX);
    }
    
    @Override
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        return calculateOffset(other, Axis.Y, offsetY);
    }
    
    @Override
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        return calculateOffset(other, Axis.Z, offsetZ);
    }
    
    @Nullable
    protected Vec3d collideWithPlane(Axis axis, double value, Vec3d vecA, Vec3d vecB) {
        Vec3d vec3d = axis != Axis.X ? axis != Axis.Y ? vecA.getIntermediateWithZValue(vecB, value) : vecA.getIntermediateWithYValue(vecB, value) : vecA
            .getIntermediateWithXValue(vecB, value);
        return vec3d != null && intersectsWithAxis(axis, vec3d) ? vec3d : null;
    }
    
    public boolean intersectsWithAxis(Axis axis, Vec3d vec) {
        switch (axis) {
        case X:
            return intersectsWithYZ(vec);
        case Y:
            return intersectsWithXZ(vec);
        case Z:
            return intersectsWithXY(vec);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "tbb[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }
    
}
