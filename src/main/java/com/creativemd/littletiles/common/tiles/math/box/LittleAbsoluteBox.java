package com.creativemd.littletiles.common.tiles.math.box;

import com.creativemd.littletiles.common.tiles.math.vec.LittleVec;
import com.creativemd.littletiles.common.utils.grid.IGridBased;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

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
	
}
