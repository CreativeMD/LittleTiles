package com.creativemd.littletiles.common.tiles.vec;

import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class LittleAbsoluteBox {
	
	public BlockPos pos;
	public LittleGridContext context;
	public LittleTileBox box;
	
	public void convertTo(LittleGridContext to) {
		box.convertTo(this.context, to);
		this.context = to;
	}
	
	public void convertToSmallest() {
		int size = box.getSmallestContext(context);
		
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public LittleTileVec getDoubledCenter(BlockPos pos) {
		LittleTileVec vec = box.getCenter();
		vec.add(this.pos.subtract(pos), context);
		vec.scale(2);
		return vec;
	}
	
}
