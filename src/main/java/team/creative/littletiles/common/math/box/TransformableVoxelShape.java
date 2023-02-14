package team.creative.littletiles.common.math.box;

import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.math.Maths;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AABBVoxelShape;
import team.creative.creativecore.common.util.math.geo.NormalPlane;
import team.creative.creativecore.common.util.math.geo.Ray3f;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.unsafe.CreativeHackery;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanCache;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanFaceCache;

public class TransformableVoxelShape extends AABBVoxelShape {
    
    private LittleGrid grid;
    private LittleBox box;
    
    public static TransformableVoxelShape create(LittleBox box, LittleGrid grid, AABB bb) {
        TransformableVoxelShape shape = CreativeHackery.allocateInstance(TransformableVoxelShape.class);
        shape.bb = bb;
        shape.box = box;
        shape.grid = grid;
        return shape;
    }
    
    public VectorFanFaceCache getFaceCache(Facing facing) {
        VectorFanCache cache = ((LittleTransformableBox) box).requestCache();
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    @Override
    public double collide(net.minecraft.core.Direction.Axis axis, AABB bb, double value) {
        return calculateOffset(bb, Axis.get(axis), value);
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
                    
                    if (tempDistance < 0 && !Maths.equals(tempDistance, 0))
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
    
}
