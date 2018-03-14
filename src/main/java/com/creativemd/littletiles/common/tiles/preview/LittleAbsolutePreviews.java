package com.creativemd.littletiles.common.tiles.preview;

import java.util.List;

import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class LittleAbsolutePreviews extends LittlePreviews {
	
	public BlockPos pos;
	
	public LittleAbsolutePreviews(BlockPos pos, LittleGridContext context) {
		super(context);
		this.pos = pos;
	}
	
	@Override
	public boolean isAbsolute()
	{
		return false;
	}
	
	@Override
	public BlockPos getBlockPos()
	{
		return null;
	}
	
	@Override
	public LittleTilePreview addPreview(BlockPos pos, LittleTilePreview preview, LittleGridContext context)
	{
		if(this.context != context)
		{
			if(this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		preview.box.addOffset(new LittleTileVec(context, this.pos.subtract(pos)));
		previews.add(preview);
		return preview;
	}
	
	@Override
	public LittleTilePreview addTile(LittleTile tile)
	{
		LittleTilePreview preview = getPreview(tile);
		preview.box.addOffset(new LittleTileVec(context, this.pos.subtract(tile.te.getPos())));
		previews.add(preview);
		return preview;
	}
	
	@Override
	public LittleTilePreview addTile(LittleTile tile, LittleTileVec offset)
	{
		LittleTilePreview preview = getPreview(tile);
		preview.box.addOffset(new LittleTileVec(context, this.pos.subtract(tile.te.getPos())));
		preview.box.addOffset(offset);
		previews.add(preview);
		return preview;
		
	}
	
}
