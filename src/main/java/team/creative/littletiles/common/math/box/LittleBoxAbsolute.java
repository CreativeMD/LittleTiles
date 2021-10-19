package team.creative.littletiles.common.math.box;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class LittleBoxAbsolute implements IGridBased {
    
    public BlockPos pos;
    public LittleGrid grid;
    public LittleBox box;
    
    public LittleBoxAbsolute(BlockPos pos) {
        this.pos = pos;
        this.grid = LittleGrid.min();
        this.box = new LittleBox(0, 0, 0, grid.count, grid.count, grid.count);
    }
    
    public LittleBoxAbsolute(BlockPos pos, LittleBox box, LittleGrid grid) {
        this.pos = pos;
        this.box = box;
        this.grid = grid;
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
    
    public LittleVec getDoubledCenter(BlockPos pos) {
        double x = (box.maxX + box.minX) / 2D;
        double y = (box.maxY + box.minY) / 2D;
        double z = (box.maxZ + box.minZ) / 2D;
        return new LittleVec((int) (x * 2), (int) (y * 2), (int) (z * 2));
    }
    
    public LittleVecGrid getSize() {
        return new LittleVecGrid(box.getSize(), grid);
    }
    
    public HashMapList<BlockPos, LittleBox> splitted() {
        HashMapList<BlockPos, LittleBox> boxes = new HashMapList<>();
        box.split(grid, pos, LittleVec.ZERO, boxes, null);
        return boxes;
    }
    
    public int getMinPos(Axis axis) {
        switch (axis) {
        case X:
            return pos.getX() + grid.toBlockOffset(box.minX);
        case Y:
            return pos.getY() + grid.toBlockOffset(box.minY);
        case Z:
            return pos.getZ() + grid.toBlockOffset(box.minZ);
        }
        return 0;
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
    
    public int getMaxPos(Axis axis) {
        switch (axis) {
        case X:
            return pos.getX() + grid.toBlockOffset(box.maxX);
        case Y:
            return pos.getY() + grid.toBlockOffset(box.maxY);
        case Z:
            return pos.getZ() + grid.toBlockOffset(box.maxZ);
        }
        return 0;
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
            return facing.positive ? box.getMin(facing.axis) - grid.toGrid(VectorUtils.get(facing.axis, this.pos) - VectorUtils.get(facing.axis, pos)) - this.box
                    .getMax(facing.axis) : this.box
                            .getMin(facing.axis) - (box.getMax(facing.axis) - grid.toGrid(VectorUtils.get(facing.axis, this.pos) - VectorUtils.get(facing.axis, pos)));
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
}
