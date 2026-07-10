package team.creative.littletiles.common.math.box;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class LittleBoxAbsolute implements IGridBased {
    
    public static LittleBoxAbsolute of(int[] array) {
        var pos = new BlockPos(array[0], array[1], array[2]);
        if (array.length == 3)
            return new LittleBoxAbsolute(pos, new LittleBox(0, 0, 0, LittleGrid.MIN.count, LittleGrid.MIN.count, LittleGrid.MIN.count), LittleGrid.MIN);
        return new LittleBoxAbsolute(pos, LittleBox.create(Arrays.copyOfRange(array, 4, array.length)), LittleGrid.get(array[3]));
    }
    
    public BlockPos pos;
    public LittleGrid grid;
    public LittleBox box;
    
    public LittleBoxAbsolute(BlockPos pos) {
        this.pos = pos;
        this.grid = LittleGrid.MIN;
        this.box = new LittleBox(0, 0, 0, grid.count, grid.count, grid.count);
    }
    
    public LittleBoxAbsolute(BlockPos pos, LittleBox box, LittleGrid grid) {
        set(pos, box, grid);
    }
    
    public LittleBoxAbsolute(BlockPos pos, LittleBoxGrid box) {
        set(pos, box);
    }
    
    public void set(BlockPos pos, LittleBox box, LittleGrid grid) {
        this.pos = pos;
        this.box = box;
        this.grid = grid;
    }
    
    public void set(BlockPos pos, LittleBoxGrid box) {
        this.pos = pos;
        this.box = box.getBox();
        this.grid = box.getGrid();
    }
    
    public int[] toArray() {
        int[] boxArray = box.getArray();
        int[] result = new int[boxArray.length + 4];
        result[0] = pos.getX();
        result[1] = pos.getY();
        result[2] = pos.getZ();
        result[3] = grid.count;
        for (int i = 0; i < boxArray.length; i++)
            result[4 + i] = boxArray[i];
        return result;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        box.convertTo(this.grid, to);
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        return box.getSmallest(grid);
    }
    
    public void include(LittleBoxAbsolute box) {
        include(box.grid, box.pos, box.box);
    }
    
    public void include(LittleGrid grid, BlockPos pos, LittleBox box) {
        if (grid != this.grid)
            if (grid.count > this.grid.count)
                convertTo(grid);
            else
                box.convertTo(grid, this.grid);
            
        BlockPos offset = pos.subtract(this.pos);
        
        this.box.minX = Math.min(box.minX + grid.toGrid(offset.getX()), this.box.minX);
        this.box.minY = Math.min(box.minY + grid.toGrid(offset.getY()), this.box.minY);
        this.box.minZ = Math.min(box.minZ + grid.toGrid(offset.getZ()), this.box.minZ);
        this.box.maxX = Math.max(box.maxX + grid.toGrid(offset.getX()), this.box.maxX);
        this.box.maxY = Math.max(box.maxY + grid.toGrid(offset.getY()), this.box.maxY);
        this.box.maxZ = Math.max(box.maxZ + grid.toGrid(offset.getZ()), this.box.maxZ);
    }
    
    public LittleVec getDoubledCenter(BlockPos pos) {
        double x = (box.maxX + box.minX) / 2D;
        double y = (box.maxY + box.minY) / 2D;
        double z = (box.maxZ + box.minZ) / 2D;
        x += grid.toGrid(this.pos.getX() - pos.getX());
        y += grid.toGrid(this.pos.getY() - pos.getY());
        z += grid.toGrid(this.pos.getZ() - pos.getZ());
        return new LittleVec((int) (x * 2), (int) (y * 2), (int) (z * 2));
    }
    
    public LittleVec getDoubledCenter() {
        double x = (box.maxX + box.minX) / 2D;
        double y = (box.maxY + box.minY) / 2D;
        double z = (box.maxZ + box.minZ) / 2D;
        return new LittleVec((int) (x * 2), (int) (y * 2), (int) (z * 2));
    }
    
    public LittleVecGrid getSize() {
        return new LittleVecGrid(box.getSize(), grid);
    }
    
    public Vec3d getVanillaCenter() {
        Vec3d vec = new Vec3d(pos);
        vec.x += grid.toVanillaGrid((box.maxX + box.minX) / 2D);
        vec.y += grid.toVanillaGrid((box.maxY + box.minY) / 2D);
        vec.z += grid.toVanillaGrid((box.maxZ + box.minZ) / 2D);
        return vec;
    }
    
    public HashMapList<BlockPos, LittleBox> splitted() {
        HashMapList<BlockPos, LittleBox> boxes = new HashMapList<>();
        box.split(grid, pos, LittleVec.ZERO, boxes, null);
        return boxes;
    }
    
    public LittleVec insideBlockOffset() {
        return new LittleVec(box.minX - grid.toBlockOffset(box.minX), box.minY - grid.toBlockOffset(box.minY), box.minZ - grid.toBlockOffset(box.minZ));
    }
    
    public LittleVecAbsolute getMin() {
        return new LittleVecAbsolute(pos, grid, box.getMinVec());
    }
    
    public int getMinPos(Axis axis) {
        return switch (axis) {
            case X -> pos.getX() + grid.toBlockOffset(box.minX);
            case Y -> pos.getY() + grid.toBlockOffset(box.minY);
            case Z -> pos.getZ() + grid.toBlockOffset(box.minZ);
            default -> 0;
        };
    }
    
    public double getMinPosX() {
        return pos.getX() + grid.toVanillaGrid(box.minX);
    }
    
    public double getMinPosY() {
        return pos.getY() + grid.toVanillaGrid(box.minY);
    }
    
    public double getMinPosZ() {
        return pos.getZ() + grid.toVanillaGrid(box.minZ);
    }
    
    public int getMinGridFrom(Axis axis, BlockPos pos) {
        return grid.toGrid(VectorUtils.get(axis, this.pos) - VectorUtils.get(axis, pos)) + box.getMin(axis);
    }
    
    public BlockPos getMinPos() {
        int x = grid.toBlockOffset(box.minX);
        int y = grid.toBlockOffset(box.minY);
        int z = grid.toBlockOffset(box.minZ);
        if (x != 0 || y != 0 || z != 0)
            return pos.offset(x, y, z);
        return pos;
    }
    
    public LittleVecAbsolute getMax() {
        return new LittleVecAbsolute(pos, grid, box.getMaxVec());
    }
    
    public int getMaxPos(Axis axis) {
        return switch (axis) {
            case X -> pos.getX() + grid.toBlockOffset(box.maxX);
            case Y -> pos.getY() + grid.toBlockOffset(box.maxY);
            case Z -> pos.getZ() + grid.toBlockOffset(box.maxZ);
            default -> 0;
        };
    }
    
    public double getMaxPosX() {
        return pos.getX() + grid.toVanillaGrid(box.maxX);
    }
    
    public double getMaxPosY() {
        return pos.getY() + grid.toVanillaGrid(box.maxY);
    }
    
    public double getMaxPosZ() {
        return pos.getZ() + grid.toVanillaGrid(box.maxZ);
    }
    
    public int getMaxGridFrom(Axis axis, BlockPos pos) {
        return grid.toGrid(VectorUtils.get(axis, this.pos) - VectorUtils.get(axis, pos)) + box.getMax(axis);
    }
    
    public BlockPos getMaxPos() {
        int x = grid.toBlockOffset(box.maxX);
        int y = grid.toBlockOffset(box.maxY);
        int z = grid.toBlockOffset(box.maxZ);
        if (x != 0 || y != 0 || z != 0)
            return pos.offset(x, y, z);
        return pos;
    }
    
    public int getDistanceIfEqualFromOneSide(Facing facing, LittleBoxAbsolute box) {
        return getDistanceIfEqualFromOneSide(facing, box.box, box.pos, box.grid);
    }
    
    public int getDistanceIfEqualFromOneSide(Facing facing, LittleBox box, BlockPos pos, LittleGrid grid) {
        minGrid(grid);
        if (this.grid.count > grid.count) {
            box = box.copy();
            box.convertTo(grid, this.grid);
            grid = this.grid;
        }
        
        Axis one = facing.one();
        Axis two = facing.two();
        
        int diffOne = grid.toGrid(VectorUtils.get(one, this.pos) - VectorUtils.get(one, pos));
        int diffTwo = grid.toGrid(VectorUtils.get(two, this.pos) - VectorUtils.get(two, pos));
        
        if (box.getMin(one) - diffOne == this.box.getMin(one) && box.getMin(two) - diffTwo == this.box.getMin(two))
            return facing.positive ? box.getMin(facing.axis) - grid.toGrid(VectorUtils.get(facing.axis, this.pos) - VectorUtils.get(facing.axis, pos)) - this.box.getMax(
                facing.axis) : this.box.getMin(facing.axis) - (box.getMax(facing.axis) - grid.toGrid(VectorUtils.get(facing.axis, this.pos) - VectorUtils.get(facing.axis, pos)));
        return -1;
    }
    
    public LittleBoxAbsolute createBoxFromFace(Facing facing, int size) {
        LittleBoxAbsolute newBox = new LittleBoxAbsolute(pos, box.copy(), grid);
        if (facing.positive) {
            int max = box.getMax(facing.axis);
            newBox.box.setMin(facing.axis, max);
            newBox.box.setMax(facing.axis, max + size);
        } else {
            int min = box.getMin(facing.axis);
            newBox.box.setMin(facing.axis, min - size);
            newBox.box.setMax(facing.axis, min);
        }
        return newBox;
    }
    
    public LittleBoxAbsolute copy() {
        return new LittleBoxAbsolute(pos, box.copy(), grid);
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBox() {
        return box.getRenderingBox(grid, new LittleVec(grid, pos));
    }
    
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderingBoxWithoutOffset() {
        return box.getRenderingBox(grid);
    }
    
    public ABB toABB() {
        return box.getABB(grid, pos);
    }
    
    public AABB toAABB() {
        return box.getBB(grid, pos);
    }
    
    public LittleBox extractSimple(BlockPos position) {
        int x = (pos.getX() - position.getX()) * grid.count;
        int y = (pos.getY() - position.getY()) * grid.count;
        int z = (pos.getZ() - position.getZ()) * grid.count;
        return box.extractBox(grid, Math.max(box.minX + x, 0), Math.max(box.minY + y, 0), Math.max(box.minZ + z, 0), Math.min(box.maxX + x, grid.count), Math.min(box.maxY + y,
            grid.count), Math.min(box.maxZ + z, grid.count), null);
    }
    
    public void move(LittleVecGrid vec) {
        sameGrid(vec, () -> box.add(vec.getVec()));
    }
    
    @Override
    public int hashCode() { // Has to be independent on grid and take the absolute position into account. BlockPos can be different and still result in the same box.
        int max = LittleGrid.getMax().count;
        int ratio = max / grid.count;
        long absoluteMinX = box.minX * ratio + pos.getX() * max;
        long absoluteMinY = box.minY * ratio + pos.getY() * max;
        long absoluteMinZ = box.minZ * ratio + pos.getZ() * max;
        return (int) ((absoluteMinY + absoluteMinZ * 31) * 31 + absoluteMinX);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleBoxAbsolute b) { // prevent objects from changing grid therefore going the long way
            long minX = box.minX;
            long minY = box.minY;
            long minZ = box.minZ;
            long maxX = box.maxX;
            long maxY = box.maxY;
            long maxZ = box.maxZ;
            long minX2 = b.box.minX;
            long minY2 = b.box.minY;
            long minZ2 = b.box.minZ;
            long maxX2 = b.box.maxX;
            long maxY2 = b.box.maxY;
            long maxZ2 = b.box.maxZ;
            
            int gridCompare = grid.count;
            if (grid.count < b.grid.count) {
                int ratio = b.grid.count / grid.count;
                minX *= ratio;
                minY *= ratio;
                minZ *= ratio;
                maxX *= ratio;
                maxY *= ratio;
                maxZ *= ratio;
                gridCompare = b.grid.count;
            } else if (grid.count > b.grid.count) {
                int ratio = grid.count / b.grid.count;
                minX2 *= ratio;
                minY2 *= ratio;
                minZ2 *= ratio;
                maxX2 *= ratio;
                maxY2 *= ratio;
                maxZ2 *= ratio;
            }
            minX += pos.getX() * gridCompare;
            minY += pos.getY() * gridCompare;
            minZ += pos.getZ() * gridCompare;
            maxX += pos.getX() * gridCompare;
            maxY += pos.getY() * gridCompare;
            maxZ += pos.getZ() * gridCompare;
            minX2 += b.pos.getX() * gridCompare;
            minY2 += b.pos.getY() * gridCompare;
            minZ2 += b.pos.getZ() * gridCompare;
            maxX2 += b.pos.getX() * gridCompare;
            maxY2 += b.pos.getY() * gridCompare;
            maxZ2 += b.pos.getZ() * gridCompare;
            return minX == minX2 && minY == minY2 && minZ == minZ2 && maxX == maxX2 && maxY == maxY2 && maxZ == maxZ2;
        }
        return false;
    }
    
}
