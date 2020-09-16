package com.creativemd.littletiles.common.tile.math.box;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.VectorUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class LittleAbsoluteBox implements IGridBased {
	
	public BlockPos pos;
	public LittleGridContext context;
	public LittleBox box;
	
	public LittleAbsoluteBox(BlockPos pos) {
		this.pos = pos;
		this.context = LittleGridContext.getMin();
		this.box = new LittleBox(0, 0, 0, context.size, context.size, context.size);
	}
	
	public LittleAbsoluteBox(BlockPos pos, LittleBox box, LittleGridContext context) {
		this.pos = pos;
		this.box = box;
		this.context = context;
	}
	
	@Override
	public LittleGridContext getContext() {
		return context;
	}
	
	@Override
	public void convertTo(LittleGridContext to) {
		box.convertTo(this.context, to);
		this.context = to;
	}
	
	@Override
	public void convertToSmallest() {
		int size = box.getSmallestContext(context);
		
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public LittleVec getDoubledCenter(BlockPos pos) {
		LittleVec vec = box.getCenter();
		vec.add(this.pos.subtract(pos), context);
		vec.scale(2);
		return vec;
	}
	
	public LittleVecContext getSize() {
		return new LittleVecContext(box.getSize(), context);
	}
	
	public HashMapList<BlockPos, LittleBox> splitted() {
		HashMapList<BlockPos, LittleBox> boxes = new HashMapList<>();
		box.split(context, pos, boxes);
		return boxes;
	}
	
	public int getMinPos(Axis axis) {
		switch (axis) {
		case X:
			return pos.getX() + context.toBlockOffset(box.minX);
		case Y:
			return pos.getY() + context.toBlockOffset(box.minY);
		case Z:
			return pos.getZ() + context.toBlockOffset(box.minZ);
		}
		return 0;
	}
	
	public int getMinGridFrom(Axis axis, BlockPos pos) {
		return context.toGrid(VectorUtils.get(axis, this.pos) - VectorUtils.get(axis, pos)) + box.getMin(axis);
	}
	
	public BlockPos getMinPos() {
		int x = context.toBlockOffset(box.minX);
		int y = context.toBlockOffset(box.minY);
		int z = context.toBlockOffset(box.minZ);
		if (x != 0 || y != 0 || z != 0)
			return pos.add(x, y, z);
		return pos;
	}
	
	public int getMaxPos(Axis axis) {
		switch (axis) {
		case X:
			return pos.getX() + context.toBlockOffset(box.maxX);
		case Y:
			return pos.getY() + context.toBlockOffset(box.maxY);
		case Z:
			return pos.getZ() + context.toBlockOffset(box.maxZ);
		}
		return 0;
	}
	
	public int getMaxGridFrom(Axis axis, BlockPos pos) {
		return context.toGrid(VectorUtils.get(axis, this.pos) - VectorUtils.get(axis, pos)) + box.getMax(axis);
	}
	
	public BlockPos getMaxPos() {
		int x = context.toBlockOffset(box.maxX);
		int y = context.toBlockOffset(box.maxY);
		int z = context.toBlockOffset(box.maxZ);
		if (x != 0 || y != 0 || z != 0)
			return pos.add(x, y, z);
		return pos;
	}
	
	public int getDistanceIfEqualFromOneSide(EnumFacing facing, LittleAbsoluteBox box) {
		return getDistanceIfEqualFromOneSide(facing, box.box, box.pos, box.context);
	}
	
	public int getDistanceIfEqualFromOneSide(EnumFacing facing, LittleBox box, BlockPos pos, LittleGridContext context) {
		convertToAtMinimum(context);
		if (this.context.size > context.size) {
			box = box.copy();
			box.convertTo(context, this.context);
			context = this.context;
		}
		
		Axis axis = facing.getAxis();
		Axis one = RotationUtils.getOne(axis);
		Axis two = RotationUtils.getTwo(axis);
		boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
		
		BlockPos diff = this.pos.subtract(pos);
		int diffOne = context.toGrid(VectorUtils.get(one, this.pos) - VectorUtils.get(one, pos));
		int diffTwo = context.toGrid(VectorUtils.get(two, this.pos) - VectorUtils.get(two, pos));
		
		if (box.getMin(one) - diffOne == this.box.getMin(one) /*&& box.getMax(one) - diffOne == this.box.getMax(one)*/ && box.getMin(two) - diffTwo == this.box.getMin(two) /* && box.getMax(
		                                                                                                                                                                    two) - diffTwo == this.box.getMax(two)*/)
			return positive ? box.getMin(axis) - context.toGrid(VectorUtils.get(axis, this.pos) - VectorUtils.get(axis, pos)) - this.box.getMax(axis) : this.box.getMin(axis) - (box.getMax(axis) - context.toGrid(VectorUtils.get(axis, this.pos) - VectorUtils.get(axis, pos)));
		return -1;
	}
	
	public LittleAbsoluteBox createBoxFromFace(EnumFacing facing, int size) {
		LittleAbsoluteBox newBox = new LittleAbsoluteBox(pos, box.copy(), context);
		Axis axis = facing.getAxis();
		if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
			int max = box.getMax(axis);
			newBox.box.setMin(axis, max);
			newBox.box.setMax(axis, max + size);
		} else {
			int min = box.getMin(axis);
			newBox.box.setMin(axis, min - size);
			newBox.box.setMax(axis, min);
		}
		return newBox;
	}
}
