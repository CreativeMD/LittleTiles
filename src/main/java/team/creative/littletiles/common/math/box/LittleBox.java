package team.creative.littletiles.common.math.box;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.RangedBitSet;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.math.face.ILittleFace;
import team.creative.littletiles.common.math.face.LittleFace;
import team.creative.littletiles.common.math.face.LittleFaceState;
import team.creative.littletiles.common.math.face.LittleServerFace;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.SplitRangeBoxes;
import team.creative.littletiles.common.math.vec.SplitRangeBoxes.SplitRangeBox;

public class LittleBox {
    
    // ================Data================
    
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;
    
    protected int faceCache;
    
    // ================Constructors================
    
    public LittleBox(LittleVec center, int sizeX, int sizeY, int sizeZ) {
        LittleVec offset = new LittleVec(sizeX, sizeY, sizeZ).calculateCenter();
        minX = center.x - offset.x;
        minY = center.y - offset.y;
        minZ = center.z - offset.z;
        maxX = minX + sizeX;
        maxY = minY + sizeY;
        maxZ = minZ + sizeZ;
    }
    
    public LittleBox(LittleGrid context, AlignedBox cube) {
        this(context.toGrid(cube.minX), context.toGrid(cube.minY), context.toGrid(cube.minZ), context.toGrid(cube.maxX), context.toGrid(cube.maxY), context.toGrid(cube.maxZ));
    }
    
    public LittleBox(LittleGrid context, AABB box) {
        this(context.toGrid(box.minX), context.toGrid(box.minY), context.toGrid(box.minZ), context.toGrid(box.maxX), context.toGrid(box.maxY), context.toGrid(box.maxZ));
    }
    
    public LittleBox(LittleBox... boxes) {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        
        for (int i = 0; i < boxes.length; i++) {
            this.minX = Math.min(boxes[i].minX, this.minX);
            this.minY = Math.min(boxes[i].minY, this.minY);
            this.minZ = Math.min(boxes[i].minZ, this.minZ);
            this.maxX = Math.max(boxes[i].maxX, this.maxX);
            this.maxY = Math.max(boxes[i].maxY, this.maxY);
            this.maxZ = Math.max(boxes[i].maxZ, this.maxZ);
        }
    }
    
    public LittleBox(LittleVec min, LittleVec max) {
        this(min.x, min.y, min.z, max.x, max.y, max.z);
    }
    
    public LittleBox(LittleVec min) {
        this(min.x, min.y, min.z, min.x + 1, min.y + 1, min.z + 1);
    }
    
    public LittleBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        set(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    // ================Conversions================
    
    public ABB getABB(LittleGrid grid) {
        return new ABB(grid.toVanillaGrid(minX), grid.toVanillaGrid(minY), grid.toVanillaGrid(minZ), grid.toVanillaGrid(maxX), grid.toVanillaGrid(maxY), grid.toVanillaGrid(maxZ));
    }
    
    public AABB getSelectionBB(LittleGrid grid, BlockPos pos) {
        return getBB(grid, pos);
    }
    
    public AABB getBB(LittleGrid grid, BlockPos offset) {
        return new AABB(grid.toVanillaGrid(minX) + offset.getX(), grid.toVanillaGrid(minY) + offset.getY(), grid.toVanillaGrid(minZ) + offset.getZ(), grid.toVanillaGrid(
            maxX) + offset.getX(), grid.toVanillaGrid(maxY) + offset.getY(), grid.toVanillaGrid(maxZ) + offset.getZ());
    }
    
    public VoxelShape getShape(LittleGrid grid) {
        return Shapes.box(grid.toVanillaGrid(minX), grid.toVanillaGrid(minY), grid.toVanillaGrid(minZ), grid.toVanillaGrid(maxX), grid.toVanillaGrid(maxY), grid.toVanillaGrid(
            maxZ));
    }
    
    public AABB getBB(LittleGrid grid) {
        return new AABB(grid.toVanillaGrid(minX), grid.toVanillaGrid(minY), grid.toVanillaGrid(minZ), grid.toVanillaGrid(maxX), grid.toVanillaGrid(maxY), grid.toVanillaGrid(maxZ));
    }
    
    public AlignedBox getBox(LittleGrid grid) {
        return new AlignedBox((float) grid.toVanillaGrid(minX), (float) grid.toVanillaGrid(minY), (float) grid.toVanillaGrid(minZ), (float) grid.toVanillaGrid(maxX), (float) grid
                .toVanillaGrid(maxY), (float) grid.toVanillaGrid(maxZ));
    }
    
    public AlignedBox getBox(LittleGrid grid, LittleVec offset) {
        return new AlignedBox((float) grid.toVanillaGrid(minX + offset.x), (float) grid.toVanillaGrid(minY + offset.y), (float) grid.toVanillaGrid(minZ + offset.z), (float) grid
                .toVanillaGrid(maxX + offset.x), (float) grid.toVanillaGrid(maxY + offset.y), (float) grid.toVanillaGrid(maxZ + offset.z));
    }
    
    // ================Save================
    
    public void changed() {
        
    }
    
    public int[] getArray() {
        return new int[] { minX, minY, minZ, maxX, maxY, maxZ };
    }
    
    public IntArrayTag getArrayTag() {
        return new IntArrayTag(getArray());
    }
    
    public int[] getArrayExtended(IParentCollection parent, LittleTile tile, LittleServerFace face) {
        hasOrCreateFaceState(parent, tile, face);
        return new int[] { faceCache, minX, minY, minZ, maxX, maxY, maxZ };
    }
    
    public IntArrayTag getArrayTagExtended(IParentCollection parent, LittleTile tile, LittleServerFace face) {
        return new IntArrayTag(getArrayExtended(parent, tile, face));
    }
    
    // ================Size & Volume================
    
    public int getSmallest(LittleGrid grid) {
        int size = LittleGrid.min().count;
        size = Math.max(size, grid.getMinGrid(minX));
        size = Math.max(size, grid.getMinGrid(minY));
        size = Math.max(size, grid.getMinGrid(minZ));
        size = Math.max(size, grid.getMinGrid(maxX));
        size = Math.max(size, grid.getMinGrid(maxY));
        size = Math.max(size, grid.getMinGrid(maxZ));
        return size;
    }
    
    protected void scale(int ratio) {
        minX *= ratio;
        minY *= ratio;
        minZ *= ratio;
        maxX *= ratio;
        maxY *= ratio;
        maxZ *= ratio;
    }
    
    protected void divide(int ratio) {
        minX /= ratio;
        minY /= ratio;
        minZ /= ratio;
        maxX /= ratio;
        maxY /= ratio;
        maxZ /= ratio;
    }
    
    public void convertTo(LittleGrid from, LittleGrid to) {
        if (from.count > to.count)
            divide(from.count / to.count);
        else
            scale(to.count / from.count);
    }
    
    public void convertTo(int from, int to) {
        if (from > to)
            divide(from / to);
        else
            scale(to / from);
    }
    
    public int getLongestSide() {
        return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
    }
    
    public LittleVec getSize() {
        return new LittleVec(maxX - minX, maxY - minY, maxZ - minZ);
    }
    
    public int getVolume() {
        return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }
    
    /** @return the volume in percent to a size of a normal block */
    public double getPercentVolume(LittleGrid context) {
        return getVolume() / (context.count3d);
    }
    
    public int get(Facing facing) {
        switch (facing) {
            case EAST:
                return maxX;
            case WEST:
                return minX;
            case UP:
                return maxY;
            case DOWN:
                return minY;
            case SOUTH:
                return maxZ;
            case NORTH:
                return minZ;
            
        }
        return 0;
    }
    
    public LittleVec get(BoxCorner corner) {
        return new LittleVec(getX(corner), getY(corner), getZ(corner));
    }
    
    public int get(BoxCorner corner, Axis axis) {
        return get(corner.getFacing(axis));
    }
    
    public int getX(BoxCorner corner) {
        return get(corner.x);
    }
    
    public int getY(BoxCorner corner) {
        return get(corner.y);
    }
    
    public int getZ(BoxCorner corner) {
        return get(corner.z);
    }
    
    public int getSize(Axis axis) {
        switch (axis) {
            case X:
                return maxX - minX;
            case Y:
                return maxY - minY;
            case Z:
                return maxZ - minZ;
        }
        return 0;
    }
    
    public void setMin(Axis axis, int value) {
        switch (axis) {
            case X:
                minX = value;
                break;
            case Y:
                minY = value;
                break;
            case Z:
                minZ = value;
                break;
        }
        changed();
    }
    
    public int getMin(Axis axis) {
        switch (axis) {
            case X:
                return minX;
            case Y:
                return minY;
            case Z:
                return minZ;
        }
        return 0;
    }
    
    public void setMax(Axis axis, int value) {
        switch (axis) {
            case X:
                maxX = value;
                break;
            case Y:
                maxY = value;
                break;
            case Z:
                maxZ = value;
                break;
        }
        changed();
    }
    
    public int getMax(Axis axis) {
        switch (axis) {
            case X:
                return maxX;
            case Y:
                return maxY;
            case Z:
                return maxZ;
        }
        return 0;
    }
    
    public LittleVec[] getCorners() {
        LittleVec[] corners = new LittleVec[BoxCorner.values().length];
        
        for (int i = 0; i < corners.length; i++) {
            BoxCorner corner = BoxCorner.values()[i];
            corners[i] = new LittleVec(get(corner.x), get(corner.y), get(corner.z));
        }
        
        return corners;
    }
    
    // ================Block Integration================
    
    public boolean isValidBox() {
        return maxX > minX && maxY > minY && maxZ > minZ;
    }
    
    public void setMinPos(MutableBlockPos pos, LittleGrid grid) {
        pos.set(grid.toBlockOffset(minX), grid.toBlockOffset(minY), grid.toBlockOffset(minZ));
    }
    
    public boolean needsMultipleBlocks(LittleGrid grid) {
        int x = minX / grid.count;
        int y = minY / grid.count;
        int z = minZ / grid.count;
        
        return maxX - x * grid.count <= grid.count && maxY - y * grid.count <= grid.count && maxZ - z * grid.count <= grid.count;
    }
    
    public boolean isBoxInsideBlock(LittleGrid grid) {
        return minX >= 0 && maxX <= grid.count && minY >= 0 && maxY <= grid.count && minZ >= 0 && maxZ <= grid.count;
    }
    
    public void splitIterator(LittleGrid grid, MutableBlockPos toUse, LittleVec vec, BiConsumer<MutableBlockPos, LittleBox> consumer) {
        int minOffX = grid.toBlockOffset(minX + vec.x);
        int minOffY = grid.toBlockOffset(minY + vec.y);
        int minOffZ = grid.toBlockOffset(minZ + vec.z);
        
        int maxOffX = grid.toBlockOffset(maxX + vec.x);
        int maxOffY = grid.toBlockOffset(maxY + vec.y);
        int maxOffZ = grid.toBlockOffset(maxZ + vec.z);
        
        for (int x = minOffX; x <= maxOffX; x++) {
            for (int y = minOffY; y <= maxOffY; y++) {
                for (int z = minOffZ; z <= maxOffZ; z++) {
                    int minX = Math.max(this.minX + vec.x, x * grid.count);
                    int minY = Math.max(this.minY + vec.y, y * grid.count);
                    int minZ = Math.max(this.minZ + vec.z, z * grid.count);
                    int maxX = Math.min(this.maxX + vec.x, x * grid.count + grid.count);
                    int maxY = Math.min(this.maxY + vec.y, y * grid.count + grid.count);
                    int maxZ = Math.min(this.maxZ + vec.z, z * grid.count + grid.count);
                    
                    if (maxX > minX && maxY > minY && maxZ > minZ) {
                        toUse.set(x, y, z);
                        
                        LittleBox box = extractBox(minX - vec.x, minY - vec.y, minZ - vec.z, maxX - vec.x, maxY - vec.y, maxZ - vec.z, null);
                        if (box != null) {
                            box.add(vec);
                            box.sub(x * grid.count, y * grid.count, z * grid.count);
                            consumer.accept(toUse, box);
                        }
                    }
                }
            }
        }
    }
    
    public void split(LittleGrid grid, BlockPos offset, LittleVec vec, HashMapList<BlockPos, LittleBox> boxes, @Nullable LittleBoxReturnedVolume volume) {
        int minOffX = grid.toBlockOffset(minX + vec.x);
        int minOffY = grid.toBlockOffset(minY + vec.y);
        int minOffZ = grid.toBlockOffset(minZ + vec.z);
        
        int maxOffX = grid.toBlockOffset(maxX + vec.x);
        int maxOffY = grid.toBlockOffset(maxY + vec.y);
        int maxOffZ = grid.toBlockOffset(maxZ + vec.z);
        
        for (int x = minOffX; x <= maxOffX; x++) {
            for (int y = minOffY; y <= maxOffY; y++) {
                for (int z = minOffZ; z <= maxOffZ; z++) {
                    int minX = Math.max(this.minX + vec.x, x * grid.count);
                    int minY = Math.max(this.minY + vec.y, y * grid.count);
                    int minZ = Math.max(this.minZ + vec.z, z * grid.count);
                    int maxX = Math.min(this.maxX + vec.x, x * grid.count + grid.count);
                    int maxY = Math.min(this.maxY + vec.y, y * grid.count + grid.count);
                    int maxZ = Math.min(this.maxZ + vec.z, z * grid.count + grid.count);
                    
                    if (maxX > minX && maxY > minY && maxZ > minZ) {
                        BlockPos pos = new BlockPos(x + offset.getX(), y + offset.getY(), z + offset.getZ());
                        
                        LittleBox box = extractBox(minX - vec.x, minY - vec.y, minZ - vec.z, maxX - vec.x, maxY - vec.y, maxZ - vec.z, volume);
                        if (box != null) {
                            box.add(vec);
                            box.sub(x * grid.count, y * grid.count, z * grid.count);
                            boxes.add(pos, box);
                        }
                    }
                }
            }
        }
    }
    
    public boolean doesFillEntireBlock(LittleGrid context) {
        return minX == 0 && minY == 0 && minZ == 0 && maxX == context.count && maxY == context.count && maxZ == context.count;
    }
    
    public LittleBox createOutsideBlockBox(LittleGrid context, Facing facing) {
        LittleBox box = this.copy();
        switch (facing) {
            case EAST:
                box.minX = 0;
                box.maxX -= context.count;
                break;
            case WEST:
                box.minX += context.count;
                box.maxX = context.count;
                break;
            case UP:
                box.minY = 0;
                box.maxY -= context.count;
                break;
            case DOWN:
                box.minY += context.count;
                box.maxY = context.count;
                break;
            case SOUTH:
                box.minZ = 0;
                box.maxZ -= context.count;
                break;
            case NORTH:
                box.minZ += context.count;
                box.maxZ = context.count;
                break;
        }
        return box;
    }
    
    // ================Box to box================
    
    protected LittleBox combine(LittleBox box) {
        boolean x = this.minX == box.minX && this.maxX == box.maxX;
        boolean y = this.minY == box.minY && this.maxY == box.maxY;
        boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
        
        if (x && y && z) {
            return this;
        }
        if (x && y) {
            if (this.minZ == box.maxZ)
                return new LittleBox(minX, minY, box.minZ, maxX, maxY, maxZ);
            else if (this.maxZ == box.minZ)
                return new LittleBox(minX, minY, minZ, maxX, maxY, box.maxZ);
        }
        if (x && z) {
            if (this.minY == box.maxY)
                return new LittleBox(minX, box.minY, minZ, maxX, maxY, maxZ);
            else if (this.maxY == box.minY)
                return new LittleBox(minX, minY, minZ, maxX, box.maxY, maxZ);
        }
        if (y && z) {
            if (this.minX == box.maxX)
                return new LittleBox(box.minX, minY, minZ, maxX, maxY, maxZ);
            else if (this.maxX == box.minX)
                return new LittleBox(minX, minY, minZ, box.maxX, maxY, maxZ);
        }
        return null;
    }
    
    public LittleBox combineBoxes(LittleBox box) {
        if (box.getClass() != LittleBox.class)
            return null;
        
        return combine(box);
    }
    
    @Nullable
    public Facing sharedBoxFaceWithoutBounds(LittleBox box) {
        boolean x = box.maxX > this.minX && box.minX < this.maxX;
        boolean y = box.maxY > this.minY && box.minY < this.maxY;
        boolean z = box.maxZ > this.minZ && box.minZ < this.maxZ;
        if (this.minZ == box.maxZ)
            if (x && y)
                return Facing.SOUTH;
            else
                return null;
        else if (this.maxZ == box.minZ)
            if (x && y)
                return Facing.NORTH;
            else
                return null;
        else if (this.minY == box.maxY)
            if (x && z)
                return Facing.UP;
            else
                return null;
        else if (this.maxY == box.minY)
            if (x && z)
                return Facing.DOWN;
            else
                return null;
        else if (this.minX == box.maxX)
            if (y && z)
                return Facing.EAST;
            else
                return null;
        else if (this.maxX == box.minX)
            if (y && z)
                return Facing.WEST;
            else
                return null;
        return null;
    }
    
    @Nullable
    public Facing sharedBoxFace(LittleBox box) {
        boolean x = this.minX == box.minX && this.maxX == box.maxX;
        boolean y = this.minY == box.minY && this.maxY == box.maxY;
        boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
        
        if (x && y && z) {
            return null;
        }
        if (x && y) {
            if (this.minZ == box.maxZ)
                return Facing.SOUTH;
            else if (this.maxZ == box.minZ)
                return Facing.NORTH;
        }
        if (x && z) {
            if (this.minY == box.maxY)
                return Facing.UP;
            else if (this.maxY == box.minY)
                return Facing.DOWN;
        }
        if (y && z) {
            if (this.minX == box.maxX)
                return Facing.EAST;
            else if (this.maxX == box.minX)
                return Facing.WEST;
        }
        return null;
    }
    
    public SplitRangeBoxes split(List<LittleBox> boxes) {
        RangedBitSet x = split(Axis.X, boxes);
        RangedBitSet y = split(Axis.Y, boxes);
        RangedBitSet z = split(Axis.Z, boxes);
        if (x != null && y != null && z != null)
            return new SplitRangeBoxes(x, y, z);
        return null;
    }
    
    protected RangedBitSet split(Axis axis, List<LittleBox> boxes) {
        int min = getMin(axis);
        int max = getMax(axis);
        RangedBitSet set = new RangedBitSet(min, max);
        
        for (LittleBox box : boxes) {
            
            if (!box.isSolid())
                return null;
            
            if (box.intersectsWith(this)) {
                set.add(box.getMin(axis));
                set.add(box.getMax(axis));
            }
        }
        
        return set;
    }
    
    /** @param cutout
     *            a list of boxes which have been cut out.
     * @return all remaining boxes or null if the box remains as it is */
    public List<LittleBox> cutOut(List<LittleBox> boxes, List<LittleBox> cutout, @Nullable LittleBoxReturnedVolume volume) {
        List<LittleBox> newBoxes = new ArrayList<>();
        
        if (boxes.isEmpty()) {
            newBoxes.add(this.copy());
            return newBoxes;
        }
        
        SplitRangeBoxes ranges;
        if ((ranges = split(boxes)) != null) {
            for (SplitRangeBox range : ranges) {
                LittleBox box = extractBox(range.x.min, range.y.min, range.z.min, range.x.max, range.y.max, range.z.max, volume);
                
                if (box != null) {
                    boolean cutted = false;
                    for (LittleBox cutBox : boxes) {
                        if (cutBox.intersectsWith(box)) {
                            cutted = true;
                            break;
                        }
                    }
                    if (cutted)
                        cutout.add(box);
                    else
                        newBoxes.add(box);
                }
                
            }
        } else {
            boolean[][][] filled = new boolean[getSize(Axis.X)][getSize(Axis.Y)][getSize(Axis.Z)];
            
            for (LittleBox box : boxes)
                box.fillInSpace(this, filled);
            
            boolean expected = filled[0][0][0];
            boolean continuous = true;
            
            loop: for (int x = 0; x < filled.length; x++) {
                for (int y = 0; y < filled[x].length; y++) {
                    for (int z = 0; z < filled[x][y].length; z++) {
                        if (filled[x][y][z] != expected) {
                            continuous = false;
                            break loop;
                        }
                    }
                }
            }
            
            if (continuous) {
                if (expected) {
                    cutout.add(this.copy());
                    return new ArrayList<>();
                }
                newBoxes.add(this.copy());
                return newBoxes;
            }
            
            for (int x = 0; x < filled.length; x++) {
                for (int y = 0; y < filled[x].length; y++) {
                    for (int z = 0; z < filled[x][y].length; z++) {
                        LittleBox box = extractBox(x + minX, y + minY, z + minZ, volume);
                        if (box != null) {
                            if (filled[x][y][z])
                                cutout.add(box);
                            else
                                newBoxes.add(box);
                        }
                    }
                }
            }
        }
        
        LittleBoxCombiner.combine(newBoxes);
        LittleBoxCombiner.combine(cutout);
        
        return newBoxes;
    }
    
    /** @return all remaining boxes or null if the box remains as it is */
    public List<LittleBox> cutOut(LittleBox box, LittleBoxReturnedVolume volume) {
        if (intersectsWith(box)) {
            List<LittleBox> boxes = new ArrayList<>();
            
            if (box.isSolid()) {
                List<LittleBox> splitting = new ArrayList<>();
                splitting.add(box);
                
                for (SplitRangeBox range : split(splitting)) {
                    LittleBox tempBox = extractBox(range.x.min, range.y.min, range.z.min, range.x.max, range.y.max, range.z.max, volume);
                    
                    boolean cutted = false;
                    if (tempBox != null) {
                        if (box.intersectsWith(tempBox)) {
                            cutted = true;
                            continue;
                        }
                        if (!cutted)
                            boxes.add(tempBox);
                    }
                    
                }
                
                return boxes;
            } else {
                LittleBox testBox = new LittleBox(0, 0, 0, 0, 0, 0);
                for (int x = minX; x < maxX; x++) {
                    for (int y = minY; y < maxY; y++) {
                        for (int z = minZ; z < maxZ; z++) {
                            testBox.set(x, y, z, x + 1, y + 1, z + 1);
                            if (!intersectsWith(testBox))
                                boxes.add(extractBox(x, y, z, volume));
                        }
                    }
                }
            }
            
            LittleBoxCombiner.combine(boxes);
            
            return boxes;
        }
        
        return null;
    }
    
    protected boolean intersectsWith(LittleBox box) {
        return box.maxX > this.minX && box.minX < this.maxX && box.maxY > this.minY && box.minY < this.maxY && box.maxZ > this.minZ && box.minZ < this.maxZ;
    }
    
    public boolean intersectsWith(AABB bb, LittleGrid grid) {
        return bb.maxX > grid.toVanillaGrid(this.minX) && bb.minX < grid.toVanillaGrid(this.maxX) && bb.maxY > grid.toVanillaGrid(this.minY) && bb.minY < grid.toVanillaGrid(
            this.maxY) && bb.maxZ > grid.toVanillaGrid(this.minZ) && bb.minZ < grid.toVanillaGrid(this.maxZ);
    }
    
    public boolean containsBox(LittleBox box) {
        return this.minX <= box.minX && this.maxX >= box.maxX && this.minY <= box.minY && this.maxY >= box.maxY && this.minZ <= box.minZ && this.maxZ >= box.maxZ;
    }
    
    public void fillInSpace(boolean[][][] filled) {
        if (!isSolid())
            return;
        
        for (int x = minX; x < maxX; x++)
            for (int y = minY; y < maxY; y++)
                for (int z = minZ; z < maxZ; z++)
                    filled[x][y][z] = true;
    }
    
    public boolean fillInSpaceInaccurate(LittleBox otherBox, Axis one, Axis two, Axis axis, boolean[][] filled) {
        boolean changed = false;
        if (isSolid()) {
            int minOne = Math.max(this.getMin(one), otherBox.getMin(one));
            int maxOne = Math.min(this.getMax(one), otherBox.getMax(one));
            int minTwo = Math.max(this.getMin(two), otherBox.getMin(two));
            int maxTwo = Math.min(this.getMax(two), otherBox.getMax(two));
            for (int valueOne = minOne; valueOne < maxOne; valueOne++)
                for (int valueTwo = minTwo; valueTwo < maxTwo; valueTwo++) {
                    filled[valueOne - otherBox.getMin(one)][valueTwo - otherBox.getMin(two)] = true;
                    changed = true;
                }
        }
        return changed;
        
    }
    
    public boolean fillInSpaceInaccurate(LittleBox otherBox, boolean[][][] filled) {
        boolean changed = false;
        if (isSolid()) {
            int minX = Math.max(this.minX, otherBox.minX);
            int maxX = Math.min(this.maxX, otherBox.maxX);
            int minY = Math.max(this.minY, otherBox.minY);
            int maxY = Math.min(this.maxY, otherBox.maxY);
            int minZ = Math.max(this.minZ, otherBox.minZ);
            int maxZ = Math.min(this.maxZ, otherBox.maxZ);
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        filled[x - otherBox.minX][y - otherBox.minY][z - otherBox.minZ] = true;
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
    
    public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled) {
        boolean changed = false;
        int minX = Math.max(this.minX, otherBox.minX);
        int maxX = Math.min(this.maxX, otherBox.maxX);
        int minY = Math.max(this.minY, otherBox.minY);
        int maxY = Math.min(this.maxY, otherBox.maxY);
        int minZ = Math.max(this.minZ, otherBox.minZ);
        int maxZ = Math.min(this.maxZ, otherBox.maxZ);
        if (isSolid()) {
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        filled[x - otherBox.minX][y - otherBox.minY][z - otherBox.minZ] = true;
                        changed = true;
                    }
                }
            }
        } else {
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        LittleBox box = otherBox.extractBox(x, y, z, null);
                        if (box != null && intersectsWith(box)) {
                            filled[x - otherBox.minX][y - otherBox.minY][z - otherBox.minZ] = true;
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }
    
    public boolean isSolid() {
        return true;
    }
    
    // ================Vectors================
    
    public void add(int x, int y, int z) {
        minX += x;
        minY += y;
        minZ += z;
        maxX += x;
        maxY += y;
        maxZ += z;
        changed();
    }
    
    public void add(LittleVec vec) {
        add(vec.x, vec.y, vec.z);
    }
    
    public void sub(int x, int y, int z) {
        minX -= x;
        minY -= y;
        minZ -= z;
        maxX -= x;
        maxY -= y;
        maxZ -= z;
        changed();
    }
    
    public void sub(LittleVec vec) {
        sub(vec.x, vec.y, vec.z);
    }
    
    public LittleVec getMinVec() {
        return new LittleVec(minX, minY, minZ);
    }
    
    public LittleVec getMaxVec() {
        return new LittleVec(maxX, maxY, maxZ);
    }
    
    public LittleVec getNearstedPointTo(LittleVec vec) {
        int x = minX;
        if (vec.x >= minX || vec.x <= maxX)
            x = vec.x;
        if (Math.abs(minX - x) > Math.abs(maxX - x))
            x = maxX;
        
        int y = minY;
        if (vec.y >= minY || vec.y <= maxY)
            y = vec.y;
        if (Math.abs(minY - y) > Math.abs(maxY - y))
            y = maxY;
        
        int z = minZ;
        if (vec.z >= minZ || vec.z <= maxZ)
            z = vec.z;
        if (Math.abs(minZ - z) > Math.abs(maxZ - z))
            z = maxZ;
        
        return new LittleVec(x, y, z);
    }
    
    public LittleVec getNearstedPointTo(LittleBox box) {
        int x = 0;
        if (minX >= box.minX && minX <= box.maxX)
            x = minX;
        else if (box.minX >= minX && box.minX <= box.maxX)
            x = box.minX;
        else if (Math.abs(minX - box.maxX) > Math.abs(maxX - box.minX))
            x = maxX;
        else
            x = minX;
        
        int y = 0;
        if (minY >= box.minY && minY <= box.maxY)
            y = minY;
        else if (box.minY >= minY && box.minY <= box.maxY)
            y = box.minY;
        else if (Math.abs(minY - box.maxY) > Math.abs(maxY - box.minY))
            y = maxY;
        else
            y = minY;
        
        int z = 0;
        if (minZ >= box.minZ && minZ <= box.maxZ)
            z = minZ;
        else if (box.minZ >= minZ && box.minZ <= box.maxZ)
            z = box.minZ;
        else if (Math.abs(minZ - box.maxZ) > Math.abs(maxZ - box.minZ))
            z = maxZ;
        else
            z = minZ;
        
        return new LittleVec(x, y, z);
    }
    
    public double distanceTo(LittleBox box) {
        return distanceTo(box.getNearstedPointTo(this));
    }
    
    public double distanceTo(LittleVec vec) {
        return this.getNearstedPointTo(vec).distanceTo(vec);
    }
    
    public boolean intersectsWithFace(Facing facing, LittleVec vec) {
        Axis one = facing.one();
        Axis two = facing.two();
        return vec.get(one) >= getMin(one) && vec.get(one) <= getMax(one) && vec.get(two) >= getMin(two) && vec.get(two) <= getMax(two);
    }
    
    public boolean intersectsWithAxis(LittleGrid context, Axis axis, Vec3 vec) {
        switch (axis) {
            case X:
                return intersectsWithYZ(context, vec);
            case Y:
                return intersectsWithXZ(context, vec);
            case Z:
                return intersectsWithXY(context, vec);
        }
        return false;
    }
    
    public boolean intersectsWithYZ(LittleGrid context, Vec3 vec) {
        return vec.y >= context.toVanillaGrid(this.minY) && vec.y < context.toVanillaGrid(this.maxY) && vec.z >= context.toVanillaGrid(this.minZ) && vec.z < context.toVanillaGrid(
            this.maxZ);
    }
    
    public boolean intersectsWithXZ(LittleGrid context, Vec3 vec) {
        return vec.x >= context.toVanillaGrid(this.minX) && vec.x < context.toVanillaGrid(this.maxX) && vec.z >= context.toVanillaGrid(this.minZ) && vec.z < context.toVanillaGrid(
            this.maxZ);
    }
    
    public boolean intersectsWithXY(LittleGrid context, Vec3 vec) {
        return vec.x >= context.toVanillaGrid(this.minX) && vec.x < context.toVanillaGrid(this.maxX) && vec.y >= context.toVanillaGrid(this.minY) && vec.y < context.toVanillaGrid(
            this.maxY);
    }
    
    public boolean isVecInside(Vec3f vec) {
        return vec.x > this.minX && vec.x < this.maxX && vec.y > this.minY && vec.y < this.maxY && vec.z > this.minZ && vec.z < this.maxZ;
    }
    
    public LittleVec getCenter() {
        return new LittleVec((maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2);
    }
    
    @Nullable
    public static Vec3 getIntermediateWithAxisValue(Vec3 first, Vec3 second, Axis axis, double value) {
        double d0 = second.x - first.x;
        double d1 = second.y - first.y;
        double d2 = second.z - first.z;
        
        double axisValue = axis.get(d0, d1, d2);
        
        if (axisValue * axisValue < VectorFan.EPSILON)
            return null;
        
        double d3 = (value - first.get(axis.toVanilla())) / axisValue;
        return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(first.x + d0 * d3, first.y + d1 * d3, first.z + d2 * d3) : null;
    }
    
    @Nullable
    protected Vec3 collideWithPlane(LittleGrid context, Axis axis, double value, Vec3 vecA, Vec3 vecB) {
        Vec3 result = getIntermediateWithAxisValue(vecA, vecB, axis, value);
        return result != null && intersectsWithAxis(context, axis, result) ? result : null;
    }
    
    @Nullable
    public BlockHitResult rayTrace(LittleGrid grid, BlockPos blockPos, Vec3 pos, Vec3 look) {
        pos = pos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        look = look.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        
        Vec3 collision = null;
        Facing collided = null;
        
        for (int i = 0; i < Facing.VALUES.length; i++) {
            Facing facing = Facing.VALUES[i];
            Vec3 temp = collideWithPlane(grid, facing.axis, grid.toVanillaGrid(get(facing)), pos, look);
            if (temp != null && isClosest(pos, collision, temp)) {
                collided = facing;
                collision = temp;
            }
        }
        
        if (collision == null)
            return null;
        
        return new BlockHitResult(collision.add(blockPos.getX(), blockPos.getY(), blockPos.getZ()), collided.toVanilla(), blockPos, true);
    }
    
    public Vec3f[] getVecArray(BoxCorner[] corners) {
        Vec3f[] result = new Vec3f[corners.length];
        for (int i = 0; i < result.length; i++)
            result[i] = new Vec3f(get(corners[i].x), get(corners[i].y), get(corners[i].z));
        return result;
    }
    
    public boolean doesTouch(LittleGrid own, LittleGrid other, LittleBox box) {
        LittleGrid context = LittleGrid.max(own, other);
        
        LittleBox thisBox = this;
        if (own != context) {
            thisBox = this.copy();
            thisBox.convertTo(own, context);
        }
        
        if (other != context) {
            box = box.copy();
            box.convertTo(other, context);
        }
        
        boolean x = box.maxX > thisBox.minX && box.minX < thisBox.maxX;
        boolean y = box.maxY > thisBox.minY && box.minY < thisBox.maxY;
        boolean z = box.maxZ > thisBox.minZ && box.minZ < thisBox.maxZ;
        
        if (x && y && (thisBox.minZ == box.maxZ || box.minZ == thisBox.maxZ))
            return true;
        
        if (x && z && (thisBox.minY == box.maxY || box.minY == thisBox.maxY))
            return true;
        
        if (y && z && (thisBox.minX == box.maxX || box.minX == thisBox.maxX))
            return true;
        
        return false;
    }
    
    public boolean doesTouch(LittleBox box) {
        boolean x = box.maxX > this.minX && box.minX < this.maxX;
        boolean y = box.maxY > this.minY && box.minY < this.maxY;
        boolean z = box.maxZ > this.minZ && box.minZ < this.maxZ;
        
        if (x && y && (minZ == box.maxZ || box.minZ == maxZ))
            return true;
        
        if (x && z && (minY == box.maxY || box.minY == maxY))
            return true;
        
        if (y && z && (minX == box.maxX || box.minX == maxX))
            return true;
        
        return false;
    }
    
    // ================Rotation & Flip================
    
    /** @param rotation
     * @param doubledCenter
     *            coordinates are doubled, meaning in order to get the correct
     *            coordinates they have to be divided by two. This allows to rotate
     *            around even axis. */
    public void rotate(Rotation rotation, LittleVec doubledCenter) {
        long tempMinX = minX * 2 - doubledCenter.x;
        long tempMinY = minY * 2 - doubledCenter.y;
        long tempMinZ = minZ * 2 - doubledCenter.z;
        long tempMaxX = maxX * 2 - doubledCenter.x;
        long tempMaxY = maxY * 2 - doubledCenter.y;
        long tempMaxZ = maxZ * 2 - doubledCenter.z;
        resort((int) ((rotation.getMatrix().getX(tempMinX, tempMinY, tempMinZ) + doubledCenter.x) / 2), (int) ((rotation.getMatrix().getY(tempMinX, tempMinY,
            tempMinZ) + doubledCenter.y) / 2), (int) ((rotation.getMatrix().getZ(tempMinX, tempMinY, tempMinZ) + doubledCenter.z) / 2), (int) ((rotation.getMatrix().getX(tempMaxX,
                tempMaxY, tempMaxZ) + doubledCenter.x) / 2), (int) ((rotation.getMatrix().getY(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.y) / 2), (int) ((rotation.getMatrix()
                        .getZ(tempMaxX, tempMaxY, tempMaxZ) + doubledCenter.z) / 2));
        changed();
    }
    
    /** @param axis
     * @param doubledCenter
     *            coordinates are doubled, meaning in order to get the correct
     *            coordinates they have to be divided by two. This allows to flip
     *            around even axis. */
    public void mirror(Axis axis, LittleVec doubledCenter) {
        long tempMin = getMin(axis) * 2 - doubledCenter.get(axis);
        long tempMax = getMax(axis) * 2 - doubledCenter.get(axis);
        int min = (int) ((doubledCenter.get(axis) - tempMin) / 2);
        int max = (int) ((doubledCenter.get(axis) - tempMax) / 2);
        setMin(axis, Math.min(min, max));
        setMax(axis, Math.max(min, max));
        changed();
    }
    
    // ================Basic Object Overrides================
    
    @Override
    public int hashCode() {
        return minX + minY + minZ + maxX + maxY + maxZ;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof LittleBox)
            return object.getClass() == this
                    .getClass() && minX == ((LittleBox) object).minX && minY == ((LittleBox) object).minY && minZ == ((LittleBox) object).minZ && maxX == ((LittleBox) object).maxX && maxY == ((LittleBox) object).maxY && maxZ == ((LittleBox) object).maxZ;
        return super.equals(object);
    }
    
    @Override
    public String toString() {
        return "[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "]";
    }
    
    // ================Special methods================
    
    public LittleBox extractBox(int x, int y, int z, @Nullable LittleBoxReturnedVolume volume) {
        return new LittleBox(x, y, z, x + 1, y + 1, z + 1);
    }
    
    public LittleBox extractBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @Nullable LittleBoxReturnedVolume volume) {
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public LittleBox copy() {
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public boolean isFaceAtEdge(LittleGrid context, Facing facing) {
        if (facing.positive)
            return getMax(facing.axis) == context.count;
        else
            return getMin(facing.axis) == 0;
    }
    
    public void growCentered(int size) {
        int invSize = size / 2;
        size -= invSize;
        minX -= invSize;
        minY -= invSize;
        minZ -= invSize;
        maxX += size;
        maxY += size;
        maxZ += size;
    }
    
    public void growToInclude(LittleBox box) {
        minX = Math.min(minX, box.minX);
        minY = Math.min(minY, box.minY);
        minZ = Math.min(minZ, box.minZ);
        maxX = Math.max(maxX, box.maxX);
        maxY = Math.max(maxY, box.maxY);
        maxZ = Math.max(maxZ, box.maxZ);
    }
    
    public LittleBox grow(Facing facing) {
        LittleBox result = this.copy();
        if (facing.positive)
            result.setMax(facing.axis, getMax(facing.axis) + 1);
        else
            result.setMin(facing.axis, getMin(facing.axis) - 1);
        return result;
    }
    
    public LittleBox shrink(Facing facing, boolean toLimit) {
        if (getSize(facing.axis) > 1) {
            LittleBox result = this.copy();
            if (facing.positive)
                result.setMax(facing.axis, toLimit ? getMin(facing.axis) + 1 : getMax(facing.axis) - 1);
            else
                result.setMin(facing.axis, toLimit ? getMax(facing.axis) - 1 : getMin(facing.axis) + 1);
            return result;
        }
        return null;
    }
    
    public void resort() {
        set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
    }
    
    public void resort(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
    }
    
    public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }
    
    // ================Rendering================
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid) {
        return new LittleRenderBox(grid, this);
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid, LittleVec vec) {
        LittleBox box = copy();
        box.add(vec);
        return new LittleRenderBox(grid, box);
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid, BlockState state) {
        return new LittleRenderBox(grid, this, state);
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid, BlockState state, LittleVec vec) {
        LittleBox box = copy();
        box.add(vec);
        return new LittleRenderBox(grid, box, state);
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox(LittleGrid grid, LittleElement element) {
        return new LittleRenderBox(grid, this, element);
    }
    
    // ================Faces================
    
    @Nullable
    public LittleFace generateFace(LittleGrid context, Facing facing) {
        Axis one = facing.one();
        Axis two = facing.two();
        
        return new LittleFace(this, null, null, context, facing, getMin(one), getMin(two), getMax(one), getMax(two), get(facing));
    }
    
    @Nullable
    public boolean set(LittleServerFace face, LittleGrid grid, Facing facing) {
        face.set(getMin(face.one), getMin(face.two), getMax(face.one), getMax(face.two), get(facing));
        return true;
    }
    
    public boolean intersectsWith(ILittleFace face) {
        return (face.facing().positive ? getMin(face.facing().axis) : getMax(face.facing().axis)) == face.origin() && face.maxOne() > getMin(face.one()) && face.minOne() < getMax(
            face.one()) && face.maxTwo() > getMin(face.two()) && face.minTwo() < getMax(face.two());
    }
    
    public LittleBox intersection(LittleBox other) {
        int minX = Math.max(this.minX, other.minX);
        int minY = Math.max(this.minY, other.minY);
        int minZ = Math.max(this.minZ, other.minZ);
        int maxX = Math.min(this.maxX, other.maxX);
        int maxY = Math.min(this.maxY, other.maxY);
        int maxZ = Math.min(this.maxZ, other.maxZ);
        return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public boolean isFaceSolid(Facing facing) {
        return true;
    }
    
    public boolean canFaceBeCombined(LittleBox other) {
        return true;
    }
    
    public void fill(ILittleFace face) {
        if (intersectsWith(face)) {
            int minOne = Math.max(getMin(face.one()), face.minOne());
            int maxOne = Math.min(getMax(face.one()), face.maxOne());
            int minTwo = Math.max(getMin(face.two()), face.minTwo());
            int maxTwo = Math.min(getMax(face.two()), face.maxTwo());
            if (isFaceSolid(face.facing().opposite()))
                for (int one = minOne; one < maxOne; one++)
                    for (int two = minTwo; two < maxTwo; two++)
                        face.set(one - face.minOne(), two - face.minTwo(), true);
            else if (face.supportsCutting())
                fillAdvanced(face);
            else
                face.setPartiallyFilled();
        }
    }
    
    protected void fillAdvanced(ILittleFace face) {}
    
    public void resetFaceState() {
        faceCache = 0;
    }
    
    public boolean hasFaceState() {
        return faceCache != 0;
    }
    
    /** @return whether the facestate was available before or not */
    public boolean hasOrCreateFaceState(IParentCollection parent, LittleTile tile, LittleServerFace face) {
        if (hasFaceState())
            return true;
        for (int i = 0; i < Facing.VALUES.length; i++)
            setFaceState(Facing.VALUES[i], face.set(parent, tile, this, Facing.VALUES[i]).calculate());
        return false;
    }
    
    public void setFaceState(Facing facing, LittleFaceState state) {
        faceCache &= ~(15 << (facing.ordinal() * 4));
        faceCache |= state.ordinal() << (facing.ordinal() * 4);
    }
    
    public LittleFaceState getFaceState(Facing facing) {
        return LittleFaceState.values()[(faceCache >> (facing.ordinal() * 4)) & 15];
    }
    
    // ================Static Helpers================
    
    public static LittleBox createExtended(int[] array) {
        LittleBox box = create(1, array);
        box.faceCache = array[0];
        return box;
    }
    
    public static LittleBox create(int[] array) {
        return create(0, array);
    }
    
    private static LittleBox create(int offset, int[] array) {
        if (array.length == offset + 6)
            return new LittleBox(array[offset], array[offset + 1], array[offset + 2], array[offset + 3], array[offset + 4], array[offset + 5]);
        
        if (array.length < offset + 6)
            throw new InvalidParameterException("No valid box given " + Arrays.toString(array));
        
        int identifier = array[offset + 6];
        if (identifier < 0)
            return new LittleTransformableBox(offset, array);
        
        throw new InvalidParameterException("No valid box given " + Arrays.toString(array));
    }
    
    public static boolean isClosest(Vec3 from, @Nullable Vec3 optional, Vec3 toCheck) {
        return optional == null || from.distanceToSqr(toCheck) < from.distanceToSqr(optional);
    }
    
    public static boolean isClosest(Vec3d from, @Nullable Vec3d optional, Vec3d toCheck) {
        return optional == null || from.distanceSqr(toCheck) < from.distanceSqr(optional);
    }
    
    public static boolean intersectsWith(LittleBox box, LittleBox box2) {
        if (box.getClass() == LittleBox.class)
            return box2.intersectsWith(box);
        return box.intersectsWith(box2);
    }
    
}
