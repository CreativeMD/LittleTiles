package com.creativemd.littletiles.common.tile.preview;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class LittleAbsolutePreviews extends LittlePreviews {
	
	public BlockPos pos;
	
	public LittleAbsolutePreviews(BlockPos pos, LittleGridContext context) {
		super(context);
		this.pos = pos;
	}
	
	protected LittleAbsolutePreviews(LittleAbsolutePreviews previews) {
		super(previews);
		this.pos = previews.pos;
	}
	
	@Override
	public boolean isAbsolute() {
		return true;
	}
	
	@Override
	public BlockPos getBlockPos() {
		return pos;
	}
	
	@Override
	public LittleAbsolutePreviews copy() {
		LittleAbsolutePreviews previews = new LittleAbsolutePreviews(pos, context);
		for (LittlePreview preview : this.previews)
			previews.previews.add(preview.copy());
		
		for (LittlePreviews child : this.children)
			previews.children.add(child.copy());
		return previews;
	}
	
	@Override
	public LittlePreview addPreview(BlockPos pos, LittlePreview preview, LittleGridContext context) {
		if (this.context != context) {
			if (this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		preview.box.add(new LittleVec(context, pos.subtract(this.pos)));
		previews.add(preview);
		return preview;
	}
	
	@Override
	public LittlePreview addTile(LittleTile tile) {
		LittlePreview preview = getPreview(tile);
		preview.box.add(new LittleVec(context, tile.te.getPos().subtract(this.pos)));
		previews.add(preview);
		return preview;
	}
	
	@Override
	public LittlePreview addTile(LittleTile tile, LittleVec offset) {
		LittlePreview preview = getPreview(tile);
		preview.box.add(new LittleVec(context, tile.te.getPos().subtract(this.pos)));
		preview.box.add(offset);
		previews.add(preview);
		return preview;
		
	}
	
}
