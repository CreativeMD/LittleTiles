package team.creative.littletiles.common.math.box;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.math.geo.NormalPlane;
import team.creative.creativecore.common.util.math.geo.Ray3f;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.utils.BooleanUtils;
import team.creative.creativecore.common.util.math.utils.IntegerUtils;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.client.render.tile.LittleRenderBoxTransformable;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.face.ILittleFace;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.face.LittleServerFace;
import team.creative.littletiles.common.math.vec.LittleRay;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleTransformableBox extends LittleBox {
    
    private static boolean[][] flipRotationMatrix = new boolean[][] { { false, false, false, false, true, true }, { false, false, false, false, true, true }, { true, true, false, false, false, false }, { true, true, false, false, false, false }, { true, true, true, true, true, true }, { true, true, true, true, true, true } };
    private static boolean[][] flipMirrorMatrix = new boolean[][] { { true, true, true, true, true, true }, { true, true, true, true, true, true }, { true, true, true, true, true, true } };
    
    protected static boolean[][] buildFlipRotationCache() {
        boolean[][] cache = new boolean[Rotation.values().length][Facing.values().length];
        AlignedBox box = new AlignedBox();
        Vec3f center = new Vec3f(0.5F, 0.5F, 0.5F);
        for (int i = 0; i < cache.length; i++) {
            Rotation rotation = Rotation.values()[i];
            boolean[] flipped = cache[i];
            for (int j = 0; j < Facing.values().length; j++) {
                Facing facing = Facing.values()[j];
                BoxFace face = BoxFace.get(facing);
                BoxCorner corner = face.getCornerInQuestion(false, false);
                
                Vec3f vec = box.getCorner(corner);
                vec.sub(center);
                rotation.transform(vec);
                vec.add(center);
                
                Facing rotatedFacing = rotation.rotate(facing);
                BoxFace rotatedFace = BoxFace.get(rotatedFacing);
                
                if (vec.epsilonEquals(box.getCorner(rotatedFace.getCornerInQuestion(false, false)), 0.0001F) || vec
                        .epsilonEquals(box.getCorner(rotatedFace.getCornerInQuestion(true, false)), 0.0001F))
                    flipped[j] = false;
                else
                    flipped[j] = true;
            }
        }
        return cache;
    }
    
    protected static boolean[][] buildFlipMirrorCache() {
        boolean[][] cache = new boolean[Axis.values().length][Facing.values().length];
        AlignedBox box = new AlignedBox();
        Vec3f center = new Vec3f(0.5F, 0.5F, 0.5F);
        for (int i = 0; i < cache.length; i++) {
            Axis axis = Axis.values()[i];
            boolean[] flipped = cache[i];
            for (int j = 0; j < Facing.values().length; j++) {
                Facing facing = Facing.values()[j];
                BoxFace face = BoxFace.get(facing);
                BoxCorner corner = face.getCornerInQuestion(false, false);
                
                Vec3f vec = box.getCorner(corner);
                vec.sub(center);
                axis.mirror(vec);
                vec.add(center);
                
                Facing rotatedFacing = axis.mirror(facing);
                BoxFace rotatedFace = BoxFace.get(rotatedFacing);
                
                if (vec.epsilonEquals(box.getCorner(rotatedFace.getCornerInQuestion(false, false)), 0.0001F) || vec
                        .epsilonEquals(box.getCorner(rotatedFace.getCornerInQuestion(true, false)), 0.0001F))
                    flipped[j] = false;
                else
                    flipped[j] = true;
            }
        }
        return cache;
    }
    
    private static final Vec3f ZERO = new Vec3f();
    
    protected static final int dataStartIndex = 0;
    protected static final int dataEndIndex = 23;
    protected static final int flipStartIndex = 24;
    protected static final int flipEndIndex = 29;
    
    private int[] data;
    private SoftReference<VectorFanCache> cache;
    
    public LittleTransformableBox(int data[]) {
        super(data[0], data[1], data[2], data[3], data[4], data[5]);
        this.data = Arrays.copyOfRange(data, 6, data.length);
    }
    
    public LittleTransformableBox(LittleBox box, int[] data) {
        super(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        this.data = data;
    }
    
    @Override
    public VoxelShape getShape(LittleGrid grid) {
        return TransformableVoxelShape.create(this, grid, getBB(grid));
    }
    
    public int getIndicator() {
        return data[0];
    }
    
    public void setFlipped(int facing, boolean value) {
        data[0] = IntegerUtils.set(getIndicator(), flipStartIndex + facing, value);
        changed();
    }
    
    public boolean getFlipped(int facing) {
        return IntegerUtils.bitIs(getIndicator(), flipStartIndex + facing);
    }
    
    public void setFlipped(Facing facing, boolean value) {
        data[0] = IntegerUtils.set(getIndicator(), flipStartIndex + facing.ordinal(), value);
        changed();
    }
    
    public boolean getFlipped(Facing facing) {
        return IntegerUtils.bitIs(getIndicator(), flipStartIndex + facing.ordinal());
    }
    
    public void setData(int[] data) {
        this.data = data;
        this.cache = null;
    }
    
    public void setData(int index, short value) {
        int realIndex = index / 2 + 1;
        if (index % 2 == 1)
            data[realIndex] = data[realIndex] & 0xFFFF0000 | value & 0x0000FFFF;
        else
            data[realIndex] = value << 16 | data[realIndex] & 0xFFFF;
        changed();
    }
    
    public short getData(int index) {
        int realIndex = index / 2 + 1;
        if (index % 2 == 1)
            return (short) (data[realIndex] & 0xFFFF);
        return (short) ((data[realIndex] >> 16) & 0xFFFF);
    }
    
    public Vec3f[] getTiltedCorners() {
        Vec3f[] corners = new Vec3f[BoxCorner.values().length];
        int indicator = getIndicator();
        
        int activeBits = 0;
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = BoxCorner.values()[i];
            
            int x = 0;
            int y = 0;
            int z = 0;
            int index = i * 3;
            if (IntegerUtils.bitIs(indicator, index)) {
                x = getData(activeBits);
                activeBits++;
            }
            
            if (IntegerUtils.bitIs(indicator, index + 1)) {
                y = getData(activeBits);
                activeBits++;
            }
            
            if (IntegerUtils.bitIs(indicator, index + 2)) {
                z = getData(activeBits);
                activeBits++;
            }
            
            corners[i] = new Vec3f(x + get(corner.x), y + get(corner.y), z + get(corner.z));
        }
        
        return corners;
    }
    
    @Override
    public void changed() {
        super.changed();
        cache = null;
    }
    
    private static boolean checkEqual(Vec3f[] corners, LittleVec[] otherCorners, BoxCorner[] toCheck, Axis axis) {
        for (int j = 0; j < toCheck.length; j++) {
            BoxCorner corner = toCheck[j];
            
            switch (axis) {
                case X:
                    if (corners[corner.ordinal()].x != otherCorners[corner.ordinal()].x)
                        return false;
                    break;
                case Y:
                    if (corners[corner.ordinal()].y != otherCorners[corner.ordinal()].y)
                        return false;
                    break;
                case Z:
                    if (corners[corner.ordinal()].z != otherCorners[corner.ordinal()].z)
                        return false;
                    break;
            }
        }
        
        return true;
    }
    
    private static VectorFan createStrip(BoxCorner[] corners, Vec3f[] vec) {
        Vec3f[] coords = BoxFace.getVecArray(corners, vec);
        boolean invalid = false;
        for (int i = 0; i < coords.length - 1; i++) {
            if (coords[i].epsilonEquals(coords[i + 1], VectorFan.EPSILON)) {
                invalid = true;
                break;
            }
        }
        if (invalid) {
            if (coords.length == 3)
                return null;
            List<Vec3f> newCoords = new ArrayList<>();
            for (int i = 0; i < coords.length - 1; i++)
                if (!coords[i].epsilonEquals(coords[i + 1], VectorFan.EPSILON))
                    newCoords.add(coords[i]);
            if (newCoords.size() < 3)
                return null;
            coords = newCoords.toArray(new Vec3f[newCoords.size()]);
        }
        return new VectorFan(coords);
    }
    
    public synchronized VectorFanCache requestCache() {
        if (cache != null) {
            VectorFanCache temp = cache.get();
            if (temp != null)
                return temp;
        }
        
        // Cache axis aligned faces
        NormalPlane[] planes = new NormalPlane[Facing.values().length];
        for (int i = 0; i < planes.length; i++) {
            Facing facing = Facing.values()[i];
            Axis axis = facing.axis;
            
            NormalPlane plane = new NormalPlane(new Vec3f(), new Vec3f());
            plane.origin.set(0, 0, 0);
            plane.origin.set(axis, get(facing));
            
            plane.normal.set(0, 0, 0);
            plane.normal.set(axis, facing.offset());
            
            planes[i] = plane;
        }
        
        VectorFanCache cache = new VectorFanCache();
        
        NormalPlane[] tiltedPlanes = new NormalPlane[Facing.values().length * 2]; // Stores all tilted planes to use them for cutting later
        
        // Tilted strips against axis box
        Vec3f[] corners = getTiltedCorners();
        LittleVec[] boxCorners = getCorners();
        for (int i = 0; i < Facing.values().length; i++) {
            Facing facing = Facing.values()[i];
            
            BoxFace face = BoxFace.get(facing);
            boolean inverted = getFlipped(facing);
            
            VectorFanFaceCache faceCache = new VectorFanFaceCache();
            cache.faces[i] = faceCache;
            
            BoxCorner[] first = face.getTriangleFirst(inverted);
            Vec3f firstNormal = BoxFace.getTraingleNormal(first, corners);
            boolean firstSame = checkEqual(corners, boxCorners, first, facing.axis);
            
            BoxCorner[] second = face.getTriangleSecond(inverted);
            Vec3f secondNormal = BoxFace.getTraingleNormal(second, corners);
            boolean secondSame = checkEqual(corners, boxCorners, second, facing.axis);
            
            if (firstSame && secondSame)
                continue;
            
            //BoxFace.ensureSameLength(firstNormal, secondNormal);
            firstNormal.normalize();
            secondNormal.normalize();
            
            boolean parallel = firstNormal.epsilonEquals(secondNormal, VectorFan.EPSILON);
            if (parallel) {
                if (!firstSame && !firstNormal.epsilonEquals(ZERO, VectorFan.EPSILON)) {
                    faceCache.tiltedStrip1 = createStrip(face.corners, corners);
                    if (faceCache.tiltedStrip1 != null)
                        tiltedPlanes[i * 2] = new NormalPlane(corners[first[0].ordinal()], firstNormal);
                }
            } else {
                if (!firstSame && !firstNormal.epsilonEquals(ZERO, VectorFan.EPSILON)) {
                    faceCache.tiltedStrip1 = createStrip(first, corners);
                    if (faceCache.tiltedStrip1 != null)
                        tiltedPlanes[i * 2] = new NormalPlane(corners[first[0].ordinal()], firstNormal);
                }
                
                if (!secondSame && !secondNormal.epsilonEquals(ZERO, VectorFan.EPSILON)) {
                    faceCache.tiltedStrip2 = createStrip(second, corners);
                    if (faceCache.tiltedStrip2 != null)
                        tiltedPlanes[i * 2 + 1] = new NormalPlane(corners[second[0].ordinal()], secondNormal);
                }
            }
            
            if (faceCache.tiltedStrip1 != null && faceCache.tiltedStrip2 != null) {
                for (int j = 0; j < faceCache.tiltedStrip2.count(); j++) {
                    Vec3f vec = faceCache.tiltedStrip2.get(j);
                    if (BooleanUtils.isTrue(tiltedPlanes[i * 2].isInFront(vec))) {
                        faceCache.convex = false; // If path strips face inwards the axis strip has to be copied and each cut by one plane
                        break;
                    }
                }
            }
            
            for (int j = 0; j < planes.length; j++) {
                if (faceCache.tiltedStrip1 != null)
                    faceCache.tiltedStrip1 = faceCache.tiltedStrip1.cut(planes[j]);
                if (faceCache.tiltedStrip2 != null)
                    faceCache.tiltedStrip2 = faceCache.tiltedStrip2.cut(planes[j]);
            }
            
        }
        
        // Axis strips against transformed box
        for (int i = 0; i < Facing.values().length; i++) {
            Facing facing = Facing.values()[i];
            BoxFace face = BoxFace.get(facing);
            
            VectorFanFaceCache axisFaceCache = cache.faces[i];
            axisFaceCache.axisStrips.add(new VectorFan(getVecArray(face.corners)));
            
            for (int j = 0; j < Facing.values().length; j++) {
                VectorFanFaceCache faceCache = cache.faces[j];
                if (faceCache.tiltedStrip1 == null && faceCache.tiltedStrip2 == null) {
                    NormalPlane cutPlane1 = tiltedPlanes[j * 2];
                    NormalPlane cutPlane2 = tiltedPlanes[j * 2 + 1];
                    if (faceCache.convex) {
                        if (cutPlane1 != null)
                            axisFaceCache.cutAxisStrip(cutPlane1);
                        if (cutPlane2 != null)
                            axisFaceCache.cutAxisStrip(cutPlane2);
                    } else
                        axisFaceCache.cutAxisStrip(facing, cutPlane1, cutPlane2);
                } else {
                    NormalPlane cutPlane1 = null;
                    NormalPlane cutPlane2 = null;
                    if (!faceCache.convex || (faceCache.tiltedStrip1 != null && faceCache.tiltedStrip2 != null)) {
                        cutPlane1 = tiltedPlanes[j * 2];
                        cutPlane2 = tiltedPlanes[j * 2 + 1];
                    } else if (faceCache.tiltedStrip1 != null)
                        cutPlane1 = tiltedPlanes[j * 2];
                    else if (faceCache.tiltedStrip2 != null)
                        cutPlane1 = tiltedPlanes[j * 2 + 1];
                    
                    if (faceCache.convex) {
                        if (cutPlane1 != null)
                            axisFaceCache.cutAxisStrip(cutPlane1);
                        if (cutPlane2 != null)
                            axisFaceCache.cutAxisStrip(cutPlane2);
                    } else
                        axisFaceCache.cutAxisStrip(facing, cutPlane1, cutPlane2);
                }
                
                if (!axisFaceCache.hasAxisStrip())
                    break;
            }
        }
        this.cache = new SoftReference<>(cache);
        return cache;
    }
    
    @Override
    public int[] getArray() {
        int[] array = new int[6 + data.length];
        array[0] = minX;
        array[1] = minY;
        array[2] = minZ;
        array[3] = maxX;
        array[4] = maxY;
        array[5] = maxZ;
        for (int i = 0; i < data.length; i++)
            array[i + 6] = data[i];
        return array;
    }
    
    @Override
    public int getSmallest(LittleGrid grid) {
        int size = super.getSmallest(grid);
        Iterator<TransformablePoint> points = points();
        while (points.hasNext())
            size = Math.max(size, grid.getMinGrid(points.next().getRelative()));
        return size;
    }
    
    @Override
    protected void scale(int ratio) {
        super.scale(ratio);
        Iterator<TransformablePoint> points = points();
        while (points.hasNext()) {
            TransformablePoint point = points.next();
            point.setRelative((short) (point.getRelative() * ratio));
        }
        if (cache != null) {
            VectorFanCache temp = cache.get();
            if (temp != null)
                temp.scale(ratio);
        }
    }
    
    @Override
    protected void divide(int ratio) {
        super.divide(ratio);
        Iterator<TransformablePoint> points = points();
        while (points.hasNext()) {
            TransformablePoint point = points.next();
            point.setRelative((short) (point.getRelative() / ratio));
        }
        if (cache != null) {
            VectorFanCache temp = cache.get();
            if (temp != null)
                temp.divide(ratio);
        }
    }
    
    @Override
    public boolean isSolid() {
        return false;
    }
    
    @Override
    public boolean doesFillEntireBlock(LittleGrid grid) {
        return false;
    }
    
    @Override
    public LittleBox combineBoxes(LittleBox box) {
        if (box instanceof LittleTransformableBox) {
            Facing facing = box.sharedBoxFaceWithoutBounds(this);
            
            if (facing == null)
                return null;
            
            Iterator<TransformableVec> points = corners();
            Iterator<TransformableVec> otherPoints = ((LittleTransformableBox) box).corners();
            
            TransformableVec point = null;
            TransformableVec otherPoint = null;
            
            Axis one = facing.one();
            Axis two = facing.two();
            
            while (points.hasNext() || otherPoints.hasNext()) {
                
                if (points.hasNext())
                    point = points.next();
                else
                    point = null;
                
                if (otherPoints.hasNext())
                    otherPoint = otherPoints.next();
                else
                    otherPoint = null;
                
                while (point != null && (otherPoint == null || point.corner.ordinal() < otherPoint.corner.ordinal())) {
                    
                    if (box.get(point.corner, one) != point.getAbsolute(one) || box.get(point.corner, two) != point.getAbsolute(two))
                        return null;
                    
                    if (points.hasNext())
                        point = points.next();
                    else
                        point = null;
                }
                
                while (otherPoint != null && (point == null || point.corner.ordinal() > otherPoint.corner.ordinal())) {
                    if (get(otherPoint.corner, one) != otherPoint.getAbsolute(one) || get(otherPoint.corner, two) != otherPoint.getAbsolute(two))
                        return null;
                    
                    if (otherPoints.hasNext())
                        otherPoint = otherPoints.next();
                    else
                        otherPoint = null;
                }
                
                if (point != null && otherPoint != null && (point.getAbsolute(one) != otherPoint.getAbsolute(one) || point.getAbsolute(two) != otherPoint.getAbsolute(two)))
                    return null;
            }
            
            if (!requestCache().get(facing).equalAxisStrip(((LittleTransformableBox) box).requestCache().get(facing.opposite()), facing.axis))
                return null;
            
            CornerCache cornerCache = new CornerCache(false);
            setAbsoluteCorners(cornerCache);
            CornerCache otherCornerCache = ((LittleTransformableBox) box).new CornerCache(false);
            ((LittleTransformableBox) box).setAbsoluteCorners(otherCornerCache);
            
            // Check lines and angles
            LittleRay ray = new LittleRay(new LittleVec(0, 0, 0), new LittleVec(0, 0, 0));
            LittleRay ray2 = new LittleRay(new LittleVec(0, 0, 0), new LittleVec(0, 0, 0));
            BoxCorner[] corners = BoxCorner.faceCorners(facing);
            for (int i = 0; i < corners.length; i++) {
                BoxCorner corner = corners[i];
                BoxCorner otherCorner = corner.mirror(facing.axis);
                ray.set(cornerCache.getOrCreate(corner), cornerCache.getOrCreate(otherCorner));
                ray2.set(otherCornerCache.getOrCreate(corner), otherCornerCache.getOrCreate(otherCorner));
                if (!ray.parallel(ray2))
                    return null;
                
                if (ray.direction.x == 0 && ray.direction.y == 0 && ray.direction.z == 0) {
                    BoxCorner newCorner = otherCorner.mirror(one);
                    ray.set(cornerCache.getOrCreate(corner), cornerCache.getOrCreate(newCorner));
                    ray2.set(otherCornerCache.getOrCreate(corner), otherCornerCache.getOrCreate(newCorner));
                    if (!ray.parallel(ray2))
                        return null;
                    
                    newCorner = otherCorner.mirror(two);
                    ray.set(cornerCache.getOrCreate(corner), cornerCache.getOrCreate(newCorner));
                    ray2.set(otherCornerCache.getOrCreate(corner), otherCornerCache.getOrCreate(newCorner));
                    if (!ray.parallel(ray2))
                        return null;
                }
            }
            
            LittleTransformableBox result = new LittleTransformableBox(new LittleBox(this, box), data.clone());
            CornerCache cache = result.new CornerCache(false);
            ((LittleTransformableBox) box).setAbsoluteCornersTakeBounds(cache);
            setAbsoluteCornersTakeBounds(cache);
            result.data = cache.getData();
            
            return result;
        }
        
        LittleBox newBox = super.combine(box);
        if (newBox == null)
            return null;
        if (box.getClass() == LittleBox.class) {
            LittleTransformableBox test = new LittleTransformableBox(box, data);
            CornerCache cache = test.new CornerCache(false);
            setAbsoluteCorners(cache);
            test.data = cache.getData();
            if (test.requestCache().isCompletelyFilled()) {
                LittleTransformableBox result = new LittleTransformableBox(newBox, data);
                cache = result.new CornerCache(false);
                setAbsoluteCorners(cache);
                result.data = cache.getData();
                return result;
            }
        }
        return null;
    }
    
    @Override
    public boolean isFaceSolid(Facing facing) {
        return requestCache().get(facing).isCompletelyFilled();
    }
    
    @Override
    protected boolean intersectsWith(LittleBox box) {
        if (super.intersectsWith(box)) {
            if (box instanceof LittleTransformableBox)
                return intersectsWith(((LittleTransformableBox) box).requestCache()) || ((LittleTransformableBox) box).intersectsWith(this.requestCache());
            
            VectorFanCache ownCache = requestCache();
            for (int i = 0; i < ownCache.faces.length; i++)
                for (VectorFan fan : ownCache.faces[i])
                    for (int j = 0; j < fan.count(); j++)
                        if (box.isVecInside(fan.get(j)))
                            return true;
                        
            // Build fan cache
            VectorFanCache newCache = new VectorFanCache();
            for (int i = 0; i < newCache.faces.length; i++) {
                Facing facing = Facing.values()[i];
                BoxFace face = BoxFace.get(facing);
                VectorFanFaceCache faceCache = new VectorFanFaceCache();
                faceCache.axisStrips.add(new VectorFan(box.getVecArray(face.corners)));
                newCache.faces[i] = faceCache;
            }
            return intersectsWith(newCache);
        }
        return false;
    }
    
    protected boolean intersectsWith(VectorFanCache cache) {
        VectorFanCache ownCache = requestCache();
        for (int i = 0; i < ownCache.faces.length; i++) {
            VectorFanFaceCache face = ownCache.faces[i];
            VectorFanFaceCache otherFace = cache.faces[i];
            
            if (face.hasAxisStrip() && otherFace.hasAxisStrip()) {
                Facing facing = Facing.values()[i];
                Axis axis = facing.axis;
                Axis one = facing.one();
                Axis two = facing.two();
                
                for (VectorFan fan : face.axisStrips)
                    for (VectorFan fan2 : otherFace.axisStrips)
                        if (fan.get(0).get(axis) == fan2.get(0).get(axis) && fan.intersect2d(fan2, one, two, facing.positive))
                            return true;
            }
        }
        
        List<List<NormalPlane>> shapes = new ArrayList<>();
        shapes.add(new ArrayList<>());
        
        // Build all possible shapes
        for (int i = 0; i < ownCache.faces.length; i++) {
            VectorFanFaceCache face = ownCache.faces[i];
            
            if (face.hasTiltedStrip()) {
                NormalPlane plane1 = face.tiltedStrip1 != null ? face.tiltedStrip1.createPlane() : null;
                NormalPlane plane2 = face.tiltedStrip2 != null ? face.tiltedStrip2.createPlane() : null;
                
                if (face.convex) {
                    for (int j = 0; j < shapes.size(); j++) {
                        if (plane1 != null)
                            shapes.get(j).add(plane1);
                        if (plane2 != null)
                            shapes.get(j).add(plane2);
                    }
                } else { // concave requires two separate shapes
                    int sizeBefore = shapes.size();
                    for (int j = 0; j < sizeBefore; j++) {
                        if (plane1 != null && plane2 != null) {
                            List<NormalPlane> newList = new ArrayList<>(shapes.get(j));
                            shapes.get(j).add(plane1);
                            newList.add(plane2);
                            shapes.add(newList);
                        } else if (plane1 != null)
                            shapes.get(j).add(plane1);
                        else if (plane2 != null)
                            shapes.get(j).add(plane2);
                    }
                }
            }
            
            if (face.hasAxisStrip()) {
                for (int j = 0; j < shapes.size(); j++) {
                    Facing facing = Facing.values()[i];
                    NormalPlane plane = new NormalPlane(new Vec3f(), new Vec3f());
                    plane.origin.set(0, 0, 0);
                    plane.origin.set(facing.axis, get(facing));
                    
                    plane.normal.set(0, 0, 0);
                    plane.normal.set(facing.axis, facing.offset());
                    
                    shapes.get(j).add(plane);
                }
            }
        }
        
        return cache.isInside(shapes);
    }
    
    @Override
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        CornerCache cache = new CornerCache(false);
        Iterator<TransformableVec> corners = corners();
        while (corners.hasNext()) {
            TransformableVec vec = corners.next();
            
            long tempX = (vec.getAbsoluteX()) * 2 - doubledCenter.x;
            long tempY = (vec.getAbsoluteY()) * 2 - doubledCenter.y;
            long tempZ = (vec.getAbsoluteZ()) * 2 - doubledCenter.z;
            LittleVec rotatedVec = new LittleVec(0, 0, 0);
            rotatedVec.x = (int) ((rotation.getMatrix().getX(tempX, tempY, tempZ) + doubledCenter.x) / 2);
            rotatedVec.y = (int) ((rotation.getMatrix().getY(tempX, tempY, tempZ) + doubledCenter.y) / 2);
            rotatedVec.z = (int) ((rotation.getMatrix().getZ(tempX, tempY, tempZ) + doubledCenter.z) / 2);
            cache.setAbsolute(vec.corner.rotate(rotation), rotatedVec);
        }
        
        super.rotate(rotation, doubledCenter);
        this.data = cache.getData();
        
        boolean[] cachedFlipped = new boolean[6];
        for (int i = 0; i < Facing.VALUES.length; i++)
            cachedFlipped[i] = getFlipped(i);
        
        for (int i = 0; i < Facing.VALUES.length; i++) {
            Facing facing = rotation.rotate(Facing.get(i));
            if (flipRotationMatrix[rotation.ordinal()][i])
                setFlipped(facing.ordinal(), !cachedFlipped[i]);
            else
                setFlipped(facing.ordinal(), cachedFlipped[i]);
        }
    }
    
    @Override
    public void mirror(Axis axis, LittleVec doubledCenter) {
        CornerCache cache = new CornerCache(false);
        Iterator<TransformableVec> corners = corners();
        while (corners.hasNext()) {
            TransformableVec vec = corners.next();
            
            long tempX = (vec.getAbsoluteX()) * 2 - doubledCenter.x;
            long tempY = (vec.getAbsoluteY()) * 2 - doubledCenter.y;
            long tempZ = (vec.getAbsoluteZ()) * 2 - doubledCenter.z;
            LittleVec flippedVec = new LittleVec(0, 0, 0);
            switch (axis) {
                case X:
                    tempX = -tempX;
                    break;
                case Y:
                    tempY = -tempY;
                    break;
                case Z:
                    tempZ = -tempZ;
                    break;
            }
            
            flippedVec.x = (int) ((tempX + doubledCenter.x) / 2);
            flippedVec.y = (int) ((tempY + doubledCenter.y) / 2);
            flippedVec.z = (int) ((tempZ + doubledCenter.z) / 2);
            cache.setAbsolute(vec.corner.mirror(axis), flippedVec);
        }
        
        super.mirror(axis, doubledCenter);
        
        this.data = cache.getData();
        
        boolean[] cachedFlipped = new boolean[6];
        for (int i = 0; i < Facing.VALUES.length; i++)
            cachedFlipped[i] = getFlipped(i);
        
        for (int i = 0; i < Facing.VALUES.length; i++) {
            Facing facing = axis.mirror(Facing.get(i));
            if (flipMirrorMatrix[axis.ordinal()][i])
                setFlipped(facing.ordinal(), !cachedFlipped[i]);
            else
                setFlipped(facing.ordinal(), cachedFlipped[i]);
        }
    }
    
    protected void setAbsoluteCorners(CornerCache cache) {
        int indicator = getIndicator();
        
        int activeBits = 0;
        for (int i = 0; i < BoxCorner.values().length; i++) {
            BoxCorner corner = BoxCorner.values()[i];
            
            int x = 0;
            int y = 0;
            int z = 0;
            int index = i * 3;
            if (IntegerUtils.bitIs(indicator, index)) {
                x = getData(activeBits);
                activeBits++;
            }
            
            if (IntegerUtils.bitIs(indicator, index + 1)) {
                y = getData(activeBits);
                activeBits++;
            }
            
            if (IntegerUtils.bitIs(indicator, index + 2)) {
                z = getData(activeBits);
                activeBits++;
            }
            
            cache.setAbsolute(corner, new LittleVec(x + get(corner.x), y + get(corner.y), z + get(corner.z)));
        }
    }
    
    protected void setAbsoluteCornersTakeBounds(CornerCache cache) {
        int indicator = getIndicator();
        
        int activeBits = 0;
        for (int i = 0; i < BoxCorner.values().length; i++) {
            BoxCorner corner = BoxCorner.values()[i];
            
            int index = i * 3;
            if (IntegerUtils.bitIs(indicator, index)) {
                cache.setAbsolute(corner, Axis.X, getData(activeBits) + get(corner.x));
                activeBits++;
            }
            
            if (IntegerUtils.bitIs(indicator, index + 1)) {
                cache.setAbsolute(corner, Axis.Y, getData(activeBits) + get(corner.y));
                activeBits++;
            }
            
            if (IntegerUtils.bitIs(indicator, index + 2)) {
                cache.setAbsolute(corner, Axis.Z, getData(activeBits) + get(corner.z));
                activeBits++;
            }
        }
    }
    
    @Override
    public LittleBox extractBox(int x, int y, int z, @Nullable LittleBoxReturnedVolume volume) {
        LittleTransformableBox box = this.copy();
        CornerCache cache = box.new CornerCache(false);
        
        box.minX = x;
        box.minY = y;
        box.minZ = z;
        box.maxX = x + 1;
        box.maxY = y + 1;
        box.maxZ = z + 1;
        
        setAbsoluteCorners(cache);
        
        box.data = cache.getData();
        box.cache = null;
        if (!box.requestCache().isInvalid())
            if (box.requestCache().isCompletelyFilled())
                return new LittleBox(x, y, z, x + 1, y + 1, z + 1);
            else
                return box;
        if (volume != null)
            volume.addPixel();
        return null;
    }
    
    @Override
    public LittleBox extractBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @Nullable LittleBoxReturnedVolume volume) {
        LittleTransformableBox box = this.copy();
        CornerCache cache = box.new CornerCache(false);
        
        box.minX = minX;
        box.minY = minY;
        box.minZ = minZ;
        box.maxX = maxX;
        box.maxY = maxY;
        box.maxZ = maxZ;
        
        setAbsoluteCorners(cache);
        
        box.data = cache.getData();
        box.cache = null;
        if (!box.requestCache().isInvalid())
            if (box.requestCache().isCompletelyFilled())
                return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ); //System.out.println("Complete!" + box); //boxes.add(new LittleBox(minX, minY, minZ, maxX, maxY, maxZ));
            else {
                box.requestCache().setBounds(minX, minY, minZ, maxX, maxY, maxZ);
                box.data = cache.getData();
                if (volume != null)
                    volume.addDifBox(box, minX, minY, minZ, maxX, maxY, maxZ);
                return box;
            }
        if (volume != null)
            volume.addBox(minX, minY, minZ, maxX, maxY, maxZ);
        return null;
    }
    
    @Override
    public LittleTransformableBox copy() {
        return new LittleTransformableBox(this, data.clone());
    }
    
    @Override
    public LittleBox grow(Facing facing) {
        LittleBox box = super.grow(facing);
        if (box != null)
            return new LittleTransformableBox(box, data);
        return null;
    }
    
    @Override
    public LittleBox shrink(Facing facing, boolean toLimit) {
        LittleBox box = super.shrink(facing, toLimit);
        if (box != null)
            return new LittleTransformableBox(box, data);
        return null;
    }
    
    protected Iterator<TransformableVec> corners() {
        return new Iterator<TransformableVec>() {
            
            int indicator = getIndicator();
            int corner = -1;
            int activeBits = 0;
            LittleVec vec = new LittleVec(0, 0, 0);
            TransformableVec holder = new TransformableVec();
            
            {
                findNext();
            }
            
            void findNext() {
                corner++;
                
                while (corner < BoxCorner.values().length) {
                    
                    int x = 0;
                    int y = 0;
                    int z = 0;
                    int index = corner * 3;
                    if (IntegerUtils.bitIs(indicator, index)) {
                        x = getData(activeBits);
                        activeBits++;
                    }
                    
                    if (IntegerUtils.bitIs(indicator, index + 1)) {
                        y = getData(activeBits);
                        activeBits++;
                    }
                    
                    if (IntegerUtils.bitIs(indicator, index + 2)) {
                        z = getData(activeBits);
                        activeBits++;
                    }
                    
                    if (x != 0 || y != 0 || z != 0) {
                        vec.set(x, y, z);
                        return;
                    }
                    
                    corner++;
                }
                
                vec = null;
            }
            
            @Override
            public boolean hasNext() {
                return vec != null;
            }
            
            /** not safe for caching
             * 
             * @return same object with updated values */
            @Override
            public TransformableVec next() {
                if (holder == null)
                    throw new NoSuchElementException();
                TransformableVec toReturn = holder.set(activeBits, BoxCorner.values()[corner], vec.x, vec.y, vec.z);
                findNext();
                if (!hasNext())
                    holder = null;
                return toReturn;
            }
            
        };
    }
    
    protected Iterator<TransformablePoint> points() {
        return new Iterator<TransformablePoint>() {
            
            int indicator = getIndicator();
            int corner = 0;
            int axisIndex = -1;
            int index = -1;
            int activeBits = -1;
            TransformablePoint point = new TransformablePoint();
            
            {
                findNext();
            }
            
            void inc() {
                axisIndex++;
                index++;
                
                if (axisIndex == 3) {
                    corner++;
                    axisIndex = 0;
                }
            }
            
            void findNext() {
                inc();
                while (index < flipStartIndex && !IntegerUtils.bitIs(indicator, index))
                    inc();
                
                activeBits++;
            }
            
            @Override
            public boolean hasNext() {
                return index < flipStartIndex;
            }
            
            /** not safe for caching
             * 
             * @return same object with updated values */
            @Override
            public TransformablePoint next() {
                if (point == null)
                    throw new NoSuchElementException();
                TransformablePoint toReturn = point.set(corner * 3 + axisIndex, activeBits, Axis.values()[axisIndex], BoxCorner.values()[corner]);
                findNext();
                if (!hasNext())
                    point = null;
                return toReturn;
            }
            
        };
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid) {
        return new LittleRenderBoxTransformable(grid, this);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid, BlockState state) {
        return new LittleRenderBoxTransformable(grid, this, state);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid, LittleElement element) {
        return new LittleRenderBoxTransformable(grid, this, element);
    }
    
    @Override
    public BlockHitResult rayTrace(LittleGrid grid, BlockPos pos, Vec3 vecA, Vec3 vecB) {
        VectorFanCache cache = requestCache();
        
        Vec3f start = new Vec3f((float) (vecA.x - pos.getX()), (float) (vecA.y - pos.getY()), (float) (vecA.z - pos.getZ()));
        Vec3f end = new Vec3f((float) (vecB.x - pos.getX()), (float) (vecB.y - pos.getY()), (float) (vecB.z - pos.getZ()));
        
        start.scale(grid.count);
        end.scale(grid.count);
        
        Ray3f ray = new Ray3f(start, end);
        
        vecA = vecA.subtract(pos.getX(), pos.getY(), pos.getZ());
        
        Vec3d startA = new Vec3d(vecA);
        
        Vec3d collision = null;
        Facing collided = null;
        
        for (int i = 0; i < Facing.values().length; i++) {
            VectorFanFaceCache face = cache.get(Facing.values()[i]);
            for (VectorFan strip : face) {
                Vec3d temp = strip.calculateIntercept(ray);
                if (temp != null)
                    temp.scale(grid.pixelLength);
                if (temp != null && isClosest(startA, collision, temp)) {
                    collided = Facing.values()[i];
                    collision = temp;
                }
            }
        }
        
        if (collision == null)
            return null;
        
        return new BlockHitResult(collision.toVanilla().add(pos.getX(), pos.getY(), pos.getZ()), collided.toVanilla(), pos, true);
    }
    
    @Override
    protected void fillAdvanced(ILittleFace face) {
        List<VectorFan> axis = requestCache().get(face.facing().opposite()).axisStrips;
        if (axis != null && !axis.isEmpty())
            face.cut(axis);
    }
    
    @Override
    @Nullable
    public LittleFace generateFace(LittleGrid grid, Facing facing) {
        VectorFanFaceCache faceCache = requestCache().get(facing);
        if (faceCache.isCompletelyFilled())
            return super.generateFace(grid, facing);
        if (faceCache.axisStrips.isEmpty())
            return null;
        Axis one = facing.one();
        Axis two = facing.two();
        return new LittleFace(this, faceCache.axisStrips, faceCache
                .tilted(), grid, facing, getMin(one), getMin(two), getMax(one), getMax(two), facing.positive ? getMax(facing.axis) : getMin(facing.axis));
    }
    
    @Override
    @Nullable
    public boolean set(LittleServerFace face, LittleGrid grid, Facing facing) {
        if (requestCache().get(facing).axisStrips.isEmpty())
            return false;
        return super.set(face, grid, facing);
    }
    
    class TransformableVec {
        
        int index;
        BoxCorner corner;
        int x;
        int y;
        int z;
        
        public TransformableVec() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
        
        public int getAbsolute(Axis axis) {
            switch (axis) {
                case X:
                    return getAbsoluteX();
                case Y:
                    return getAbsoluteY();
                case Z:
                    return getAbsoluteZ();
            }
            return 0;
        }
        
        public int getAbsoluteX() {
            return get(corner, Axis.X) + x;
        }
        
        public int getAbsoluteY() {
            return get(corner, Axis.Y) + y;
        }
        
        public int getAbsoluteZ() {
            return get(corner, Axis.Z) + z;
        }
        
        public int getRelative(Axis axis) {
            switch (axis) {
                case X:
                    return getRelativeX();
                case Y:
                    return getRelativeY();
                case Z:
                    return getRelativeZ();
            }
            return 0;
        }
        
        public BoxCorner getCorner() {
            return corner;
        }
        
        public int getRelativeX() {
            return x;
        }
        
        public int getRelativeY() {
            return y;
        }
        
        public int getRelativeZ() {
            return z;
        }
        
        public TransformableVec set(int index, BoxCorner corner, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.index = index;
            this.corner = corner;
            return this;
        }
        
        @Override
        public String toString() {
            return "[" + x + "," + y + "," + z + "]";
        }
        
    }
    
    class TransformablePoint {
        
        int progressIndex;
        int index;
        Axis axis;
        BoxCorner corner;
        
        public TransformablePoint set(int progressIndex, int index, Axis axis, BoxCorner corner) {
            this.progressIndex = progressIndex;
            this.index = index;
            this.axis = axis;
            this.corner = corner;
            return this;
        }
        
        public int getProgressIndex() {
            return progressIndex;
        }
        
        public int getAbsolute() {
            return getRelative() + get(corner, axis);
        }
        
        public void setAbsolute(int value) {
            setRelative((short) (value - get(corner, axis)));
        }
        
        public short getRelative() {
            return getData(index);
        }
        
        public void setRelative(short value) {
            setData(index, value);
        }
        
        @Override
        public String toString() {
            return corner + "," + axis + "," + getRelative();
        }
        
    }
    
    public class CornerCache {
        
        public final boolean relative;
        
        public CornerCache(boolean relative) {
            this.relative = relative;
        }
        
        public LittleTransformableBox getBox() {
            return LittleTransformableBox.this;
        }
        
        public LittleVec[] corners = new LittleVec[BoxCorner.values().length];
        
        public LittleVec getOrCreate(BoxCorner corner) {
            LittleVec vec = corners[corner.ordinal()];
            if (vec == null) {
                if (relative)
                    vec = new LittleVec(0, 0, 0);
                else
                    vec = get(corner);
                corners[corner.ordinal()] = vec;
            }
            
            return vec;
        }
        
        public void setAbsolute(BoxCorner corner, LittleVec vec) {
            corners[corner.ordinal()] = vec;
            if (relative) {
                vec.x -= get(corner, Axis.X);
                vec.y -= get(corner, Axis.Y);
                vec.z -= get(corner, Axis.Z);
            }
        }
        
        public void setAbsolute(BoxCorner corner, Axis axis, int value) {
            if (relative)
                getOrCreate(corner).set(axis, value - get(corner, axis));
            else
                getOrCreate(corner).set(axis, value);
        }
        
        public void setRelative(BoxCorner corner, LittleVec vec) {
            corners[corner.ordinal()] = vec;
            if (!relative) {
                vec.x += get(corner, Axis.X);
                vec.y += get(corner, Axis.Y);
                vec.z += get(corner, Axis.Z);
            }
        }
        
        public void setRelative(BoxCorner corner, Axis axis, int value) {
            if (relative)
                getOrCreate(corner).set(axis, value);
            else
                getOrCreate(corner).set(axis, value + get(corner, axis));
        }
        
        public int[] getData() {
            int indicator = Integer.MIN_VALUE | (0b10111111_00000000_00000000_00000000 & getBox().getIndicator());
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < corners.length; i++) {
                LittleVec vec = corners[i];
                if (vec == null)
                    continue;
                
                int index = i * 3;
                
                if (relative) {
                    if (vec.x != 0) {
                        indicator = IntegerUtils.set(indicator, index);
                        data.add(vec.x);
                    }
                    if (vec.y != 0) {
                        indicator = IntegerUtils.set(indicator, index + 1);
                        data.add(vec.y);
                    }
                    if (vec.z != 0) {
                        indicator = IntegerUtils.set(indicator, index + 2);
                        data.add(vec.z);
                    }
                } else {
                    BoxCorner corner = BoxCorner.values()[i];
                    if (vec.x != get(corner, Axis.X)) {
                        indicator = IntegerUtils.set(indicator, index);
                        data.add(vec.x - get(corner, Axis.X));
                    }
                    if (vec.y != get(corner, Axis.Y)) {
                        indicator = IntegerUtils.set(indicator, index + 1);
                        data.add(vec.y - get(corner, Axis.Y));
                    }
                    if (vec.z != get(corner, Axis.Z)) {
                        indicator = IntegerUtils.set(indicator, index + 2);
                        data.add(vec.z - get(corner, Axis.Z));
                    }
                }
            }
            
            int[] array = new int[1 + (int) Math.ceil(data.size() / 2D)];
            array[0] = indicator;
            for (int i = 0; i < array.length - 1; i++) {
                int second = i * 2 + 1 < data.size() ? data.get(i * 2 + 1) : 0;
                array[i + 1] = ((short) (int) data.get(i * 2)) << 16 | ((short) second) & 0xFFFF;
            }
            return array;
        }
        
    }
    
    @Override
    public void add(int x, int y, int z) {
        minX += x;
        minY += y;
        minZ += z;
        maxX += x;
        maxY += y;
        maxZ += z;
        if (cache != null) {
            VectorFanCache temp = cache.get();
            if (temp != null)
                temp.add(x, y, z);
        }
    }
    
    @Override
    public void sub(int x, int y, int z) {
        minX -= x;
        minY -= y;
        minZ -= z;
        maxX -= x;
        maxY -= y;
        maxZ -= z;
        if (cache != null) {
            VectorFanCache temp = cache.get();
            if (temp != null)
                temp.sub(x, y, z);
        }
    }
    
    public class VectorFanCache {
        
        VectorFanFaceCache[] faces = new VectorFanFaceCache[6];
        
        public VectorFanFaceCache get(Facing facing) {
            return faces[facing.ordinal()];
        }
        
        public void add(float x, float y, float z) {
            for (int i = 0; i < faces.length; i++)
                faces[i].add(x, y, z);
        }
        
        public void sub(float x, float y, float z) {
            for (int i = 0; i < faces.length; i++)
                faces[i].sub(x, y, z);
        }
        
        public void scale(float ratio) {
            for (int i = 0; i < faces.length; i++)
                faces[i].scale(ratio);
        }
        
        public boolean isCompletelyFilled() {
            for (int i = 0; i < faces.length; i++)
                if (!faces[i].isCompletelyFilled())
                    return false;
            return true;
        }
        
        public boolean isInvalid() {
            int count = 0;
            for (int i = 0; i < faces.length; i++)
                if (!faces[i].isInvalid())
                    count++;
            return count < 3;
        }
        
        protected void divide(int ratio) {
            for (int i = 0; i < faces.length; i++)
                faces[i].divide(ratio);
        }
        
        protected boolean intersects(NormalPlane plane1, NormalPlane plane2) {
            for (int i = 0; i < faces.length; i++)
                if (faces[i].intersects(plane1, plane2))
                    return true;
            return false;
        }
        
        public boolean isInside(List<List<NormalPlane>> shapes) {
            List<CenterPoint> centers = new ArrayList<>();
            centers.add(new CenterPoint());
            
            for (int i = 0; i < faces.length; i++)
                if (faces[i].isInside(shapes, centers))
                    return true;
                
            for (int i = 0; i < centers.size(); i++) {
                Vec3f center = centers.get(i).getCenter();
                for (int j = 0; j < shapes.size(); j++)
                    if (isInside(shapes.get(j), center))
                        return true;
            }
            return false;
        }
        
        public boolean isInside(List<NormalPlane> shape, Vec3f vec) {
            for (int i = 0; i < shape.size(); i++)
                if (!BooleanUtils.isFalse(shape.get(i).isInFront(vec)))
                    return false;
            return true;
        }
        
        protected void setBounds(int oldMinX, int oldMinY, int oldMinZ, int oldMaxX, int oldMaxY, int oldMaxZ) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (int i = 0; i < faces.length; i++)
                for (VectorFan fan : faces[i])
                    for (int j = 0; j < fan.count(); j++) {
                        Vec3f vec = fan.get(j);
                        minX = Math.min(minX, (int) Math.floor(vec.x));
                        minY = Math.min(minY, (int) Math.floor(vec.y));
                        minZ = Math.min(minZ, (int) Math.floor(vec.z));
                        maxX = Math.max(maxX, (int) Math.ceil(vec.x));
                        maxY = Math.max(maxY, (int) Math.ceil(vec.y));
                        maxZ = Math.max(maxZ, (int) Math.ceil(vec.z));
                    }
                
            minX = Math.max(minX, oldMinX);
            minY = Math.max(minY, oldMinY);
            minZ = Math.max(minZ, oldMinZ);
            maxX = Math.min(maxX, oldMaxX);
            maxY = Math.min(maxY, oldMaxY);
            maxZ = Math.min(maxZ, oldMaxZ);
            set(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
    
    public static class VectorFanFaceCache implements Iterable<VectorFan> {
        
        public boolean convex = true;
        public VectorFan tiltedStrip1;
        public VectorFan tiltedStrip2;
        public boolean completedFilled = true;
        
        public List<VectorFan> axisStrips = new ArrayList<>();
        
        public boolean isInvalid() {
            return tiltedStrip1 == null && tiltedStrip2 == null && axisStrips.isEmpty();
        }
        
        public boolean isCompletelyFilled() {
            return completedFilled && !hasTiltedStrip();
        }
        
        public boolean hasTiltedStrip() {
            return tiltedStrip1 != null || tiltedStrip2 != null;
        }
        
        public boolean hasAxisStrip() {
            return !axisStrips.isEmpty();
        }
        
        public void cutAxisStrip(Facing facing, NormalPlane plane, NormalPlane plane2) {
            Axis one = facing.one();
            Axis two = facing.two();
            boolean inverse = facing.positive;
            
            List<VectorFan> newAxisStrips = new ArrayList<>();
            for (int i = 0; i < axisStrips.size(); i++) {
                VectorFan strip = axisStrips.get(i).cut(plane);
                VectorFan strip2 = axisStrips.get(i).cut(plane2);
                
                if (strip != null && strip2 != null && strip.intersect2d(strip2, one, two, inverse)) {
                    List<VectorFan> fans = strip.cut2d(strip2, one, two, inverse, false);
                    newAxisStrips.add(strip2);
                    newAxisStrips.addAll(fans);
                } else {
                    if (strip != null)
                        newAxisStrips.add(strip);
                    if (strip2 != null)
                        newAxisStrips.add(strip2);
                }
            }
            if (completedFilled) {
                if (newAxisStrips.size() == 1 && axisStrips.size() == 1)
                    completedFilled = newAxisStrips.get(0).equals(axisStrips.get(0));
                else
                    completedFilled = false;
            }
            this.axisStrips = newAxisStrips;
        }
        
        public void cutAxisStrip(NormalPlane plane) {
            int i = 0;
            VectorFan before = null;
            if (completedFilled && axisStrips.size() == 1)
                before = axisStrips.get(0).copy();
            while (i < axisStrips.size()) {
                VectorFan strip = axisStrips.get(i).cut(plane);
                if (strip == null)
                    axisStrips.remove(i);
                else {
                    axisStrips.set(i, strip);
                    i++;
                }
            }
            if (completedFilled)
                if (axisStrips.size() == 1)
                    completedFilled = before.equals(axisStrips.get(0));
                else
                    completedFilled = false;
        }
        
        public boolean equalAxisStrip(VectorFanFaceCache cache, Axis toIgnore) {
            if (axisStrips.size() != cache.axisStrips.size() || axisStrips.size() != 1)
                return false;
            return axisStrips.get(0).equalsIgnoreOrder(cache.axisStrips.get(0), toIgnore);
        }
        
        public void add(float x, float y, float z) {
            if (tiltedStrip1 != null)
                tiltedStrip1.move(x, y, z);
            if (tiltedStrip2 != null)
                tiltedStrip2.move(x, y, z);
            for (int i = 0; i < axisStrips.size(); i++)
                axisStrips.get(i).move(x, y, z);
        }
        
        public void sub(float x, float y, float z) {
            if (tiltedStrip1 != null)
                tiltedStrip1.move(-x, -y, -z);
            if (tiltedStrip2 != null)
                tiltedStrip2.move(-x, -y, -z);
            for (int i = 0; i < axisStrips.size(); i++)
                axisStrips.get(i).move(-x, -y, -z);
        }
        
        public void scale(float ratio) {
            if (tiltedStrip1 != null)
                tiltedStrip1.scale(ratio);
            if (tiltedStrip2 != null)
                tiltedStrip2.scale(ratio);
            for (int i = 0; i < axisStrips.size(); i++)
                axisStrips.get(i).scale(ratio);
        }
        
        public void divide(float ratio) {
            if (tiltedStrip1 != null)
                tiltedStrip1.divide(ratio);
            if (tiltedStrip2 != null)
                tiltedStrip2.divide(ratio);
            for (int i = 0; i < axisStrips.size(); i++)
                axisStrips.get(i).divide(ratio);
        }
        
        public boolean intersects(NormalPlane plane1, NormalPlane plane2) {
            for (VectorFan fan : this)
                if (fan.intersects(plane1, plane2))
                    return true;
            return false;
        }
        
        public boolean isInside(List<List<NormalPlane>> shapes, List<CenterPoint> centers) {
            if (!convex) {
                int sizeBefore = centers.size();
                for (int i = 0; i < sizeBefore; i++) {
                    CenterPoint first = centers.get(i);
                    CenterPoint second = first.copy();
                    if (tiltedStrip1 != null)
                        first.add(tiltedStrip1);
                    if (tiltedStrip2 != null)
                        second.add(tiltedStrip2);
                    centers.add(second);
                }
            }
            
            if (hasAxisStrip())
                for (int i = 0; i < centers.size(); i++)
                    for (int j = 0; j < axisStrips.size(); j++)
                        centers.get(i).add(axisStrips.get(j));
                    
            for (VectorFan fan : this)
                if (fan.isInside(shapes))
                    return true;
                
            return false;
        }
        
        public Iterable<VectorFan> tilted() {
            return new Iterable<VectorFan>() {
                
                @Override
                public Iterator<VectorFan> iterator() {
                    return new Iterator<VectorFan>() {
                        
                        int additionalCount = (tiltedStrip1 != null ? 1 : 0) + (tiltedStrip2 != null ? 1 : 0);
                        int index = 0;
                        
                        @Override
                        public boolean hasNext() {
                            return index < additionalCount;
                        }
                        
                        @Override
                        public VectorFan next() {
                            VectorFan result;
                            int secondIndex = index;
                            if (secondIndex < additionalCount)
                                if (secondIndex == 0)
                                    result = tiltedStrip1 != null ? tiltedStrip1 : tiltedStrip2;
                                else
                                    result = tiltedStrip2;
                            else
                                throw new RuntimeException("Missing next element in iterator");
                            
                            index++;
                            return result;
                        }
                    };
                }
            };
        }
        
        @Override
        public Iterator<VectorFan> iterator() {
            return new Iterator<VectorFan>() {
                
                int additionalCount = (tiltedStrip1 != null ? 1 : 0) + (tiltedStrip2 != null ? 1 : 0);
                int index = 0;
                
                @Override
                public boolean hasNext() {
                    return index < axisStrips.size() + additionalCount;
                }
                
                @Override
                public VectorFan next() {
                    VectorFan result;
                    if (index < axisStrips.size())
                        result = axisStrips.get(index);
                    else {
                        int secondIndex = index - axisStrips.size();
                        if (secondIndex < additionalCount)
                            if (secondIndex == 0)
                                result = tiltedStrip1 != null ? tiltedStrip1 : tiltedStrip2;
                            else
                                result = tiltedStrip2;
                        else
                            throw new RuntimeException("Missing next element in iterator");
                    }
                    
                    index++;
                    return result;
                }
            };
        }
        
    }
    
    public static class CenterPoint {
        
        Vec3f vec = new Vec3f();
        int count = 0;
        
        public void add(Vec3f vec) {
            this.vec.add(vec);
            count++;
        }
        
        public void add(VectorFan fan) {
            for (int i = 0; i < fan.count(); i++)
                add(fan.get(i));
        }
        
        public Vec3f getCenter() {
            float multiplier = 1F / (count);
            Vec3f result = new Vec3f(vec);
            result.scale(multiplier);
            return result;
        }
        
        public CenterPoint copy() {
            CenterPoint point = new CenterPoint();
            point.vec = new Vec3f(vec);
            point.count = count;
            return point;
        }
        
    }
    
}
