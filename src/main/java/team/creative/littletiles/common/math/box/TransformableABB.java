package team.creative.littletiles.common.math.box;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.Maths;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.BoxUtils;
import team.creative.creativecore.common.util.math.geo.NormalPlane;
import team.creative.creativecore.common.util.math.geo.Ray3f;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanCache;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanFaceCache;

public class TransformableABB extends ABB {
    
    private LittleGrid grid;
    private LittleTransformableBox box;
    
    public TransformableABB(ABB bb, LittleGrid grid, LittleTransformableBox box) {
        super(bb);
        this.grid = grid;
        this.box = box;
    }
    
    public TransformableABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, LittleGrid grid, LittleTransformableBox box) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.grid = grid;
        this.box = box;
    }
    
    public VectorFanFaceCache getFaceCache(Facing facing) {
        VectorFanCache cache = box.requestCache();
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    @Override
    public double calculateAxisOffset(Axis axis, Axis one, Axis two, AABB other, double offset) {
        if (offset == 0)
            return offset;
        
        boolean positive = offset > 0;
        Facing direction = Facing.get(axis, positive);
        
        float minOne = (float) BoxUtils.min(other, one);
        minOne -= Math.floor(min(one));
        minOne *= grid.count;
        float minTwo = (float) BoxUtils.min(other, two);
        minTwo -= Math.floor(min(two));
        minTwo *= grid.count;
        float maxOne = (float) BoxUtils.max(other, one);
        maxOne -= Math.floor(min(one));
        maxOne *= grid.count;
        float maxTwo = (float) BoxUtils.max(other, two);
        maxTwo -= Math.floor(min(two));
        maxTwo *= grid.count;
        
        float otherAxis = (float) (offset > 0 ? BoxUtils.max(other, axis) : BoxUtils.min(other, axis));
        otherAxis -= Math.floor(min(axis));
        otherAxis *= grid.count;
        
        NormalPlane[] cuttingPlanes = new NormalPlane[] { new NormalPlane(one, minOne, Facing.get(one, false)), new NormalPlane(two, minTwo, Facing.get(two,
            false)), new NormalPlane(one, maxOne, Facing.get(one, true)), new NormalPlane(two, maxTwo, Facing.get(two, true)) };
        
        VectorFan tempFan = new VectorFan(null);
        VectorFanFaceCache front = getFaceCache(direction.opposite());
        if (front.hasAxisStrip()) {
            for (VectorFan vectorFan : front.axisStrips) {
                tempFan.set(vectorFan);
                if (tempFan.cutWithoutCopy(cuttingPlanes)) {
                    if (offset > 0.0D && BoxUtils.max(other, axis) <= min(axis)) {
                        double d1 = min(axis) - BoxUtils.max(other, axis);
                        
                        if (d1 < offset)
                            return d1;
                    } else if (offset < 0.0D && BoxUtils.min(other, axis) >= max(axis)) {
                        double d0 = max(axis) - BoxUtils.min(other, axis);
                        
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
    
    @Override
    public BlockHitResult rayTrace(Vec3 pos, Vec3 look, BlockPos blockPos) {
        return box.rayTrace(grid, blockPos, pos, look);
    }
    
    @Override
    public TransformableABB copy() {
        return new TransformableABB(this, grid, box);
    }
    
}
