package team.creative.littletiles.common.math.box;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.Maths;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.box.BoxUtils;
import team.creative.creativecore.common.util.math.geo.NormalPlaneD;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.vec.Vec3d;
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
        if (Math.abs(offset) < 1.0E-7D)
            return offset;
        
        boolean positive = offset > 0;
        Facing direction = Facing.get(axis, positive);
        
        double minOne = BoxUtils.min(other, one);
        minOne -= Math.floor(min(one));
        minOne *= grid.count;
        double minTwo = BoxUtils.min(other, two);
        minTwo -= Math.floor(min(two));
        minTwo *= grid.count;
        double maxOne = BoxUtils.max(other, one);
        maxOne -= Math.floor(min(one));
        maxOne *= grid.count;
        double maxTwo = BoxUtils.max(other, two);
        maxTwo -= Math.floor(min(two));
        maxTwo *= grid.count;
        
        double otherAxis = offset > 0 ? BoxUtils.max(other, axis) : BoxUtils.min(other, axis);
        otherAxis -= Math.floor(min(axis));
        otherAxis *= grid.count;
        
        NormalPlaneD[] cuttingPlanes = new NormalPlaneD[] { new NormalPlaneD(one, minOne, Facing.get(one, false)), new NormalPlaneD(two, minTwo, Facing.get(two,
            false)), new NormalPlaneD(one, maxOne, Facing.get(one, true)), new NormalPlaneD(two, maxTwo, Facing.get(two, true)) };
        
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
        
        double distance = Double.POSITIVE_INFINITY;
        
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
                    double tempDistance = positive ? vec.get(axis) - otherAxis : otherAxis - vec.get(axis);
                    
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
    public boolean intersectsPrecise(AABB bb) {
        //Vec3d offset = new Vec3d(minX - Math.floor(minX), minY - Math.floor(minY), minZ - Math.floor(minZ));
        Vec3d offset = new Vec3d(Math.floor(minX), Math.floor(minY), Math.floor(minZ));
        Vec3d[] corners = BoxUtils.getCorners(bb);
        for (int i = 0; i < corners.length; i++) {
            corners[i].sub(offset);
            corners[i].scale(grid.count);
        }
        VectorFanCache cache = new VectorFanCache();
        for (int i = 0; i < cache.faces.length; i++) {
            VectorFanFaceCache face = new VectorFanFaceCache();
            BoxFace boxFace = BoxFace.values()[i];
            Vec3f[] coords = new Vec3f[boxFace.corners.length];
            for (int j = 0; j < coords.length; j++)
                coords[j] = new Vec3f(corners[boxFace.corners[j].ordinal()]);
            face.axisStrips.add(new VectorFan(coords));
            cache.faces[i] = face;
        }
        return box.requestCache().intersectsWith(x -> grid.count * (float) (this.get((Facing) x) - offset.get(((Facing) x).axis)), cache) && cache.intersectsWith(x -> box.get(
            (Facing) x), box.requestCache());
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
