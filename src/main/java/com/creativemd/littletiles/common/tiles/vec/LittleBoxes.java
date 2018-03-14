package com.creativemd.littletiles.common.tiles.vec;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.util.math.BlockPos;

public class LittleBoxes extends ArrayList<LittleTileBox>{
	
	public BlockPos pos;
	public LittleGridContext context;
	
	public LittleBoxes(BlockPos pos, LittleGridContext context) {
		this.pos = pos;
		this.context = context;
	}
	
	public void addBox(LittleTile tile)
	{
		addBox(tile.getContext(), tile.te.getPos(), tile.box.copy());
	}
	
	public void addBox(LittleGridContext context, BlockPos pos, LittleTileBox box)
	{
		if(this.context != context)
		{
			if(this.context.size > context.size)
			{
				box.convertTo(context, this.context);
				context = this.context;
			}
			else
				convertTo(context);
		}
		
		box.addOffset(new LittleTileVec(context, this.pos.subtract(pos)));
		add(box);
	}
	
	/*public void ensureContext(LittleGridContext context)
	{
		if(context.size > this.context.size)
			convertTo(context);
	}*/
	
	public void convertTo(LittleGridContext to)
	{
		for (LittleTileBox box : this) {
			box.convertTo(this.context, to);
		}
		this.context = to;
	}
	
	public void convertToSmallest()
	{
		int size = LittleGridContext.minSize;
		for (LittleTileBox box : this) {
			size = Math.max(size, box.getSmallestContext(context));
		}
		
		if(size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	public LittleTileBox getSurroundingBox()
	{
		return LittleTileBox.getSurroundingBox(this);
	}

	public HashMapList<BlockPos, LittleTileBox> split()
	{
		HashMapList<BlockPos, LittleTileBox> map = new HashMapList<>();
		for (LittleTileBox box : this) {
			box.split(context, pos, map);
		}
		return map;
	}

	public LittleBoxes copy()
	{
		LittleBoxes boxes = new LittleBoxes(pos, context);
		boxes.addAll(this);
		return boxes;
	}
	
}
