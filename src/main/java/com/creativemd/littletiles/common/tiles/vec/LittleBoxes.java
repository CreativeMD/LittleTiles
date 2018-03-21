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
		
		box.addOffset(new LittleTileVec(context, pos.subtract(this.pos)));
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
		if(isEmpty())
			return null;
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		for (LittleTileBox box : this) {
			minX = Math.min(minX, box.minX);
			minY = Math.min(minY, box.minY);
			minZ = Math.min(minZ, box.minZ);
			maxX = Math.max(maxX, box.maxX);
			maxY = Math.max(maxY, box.maxY);
			maxZ = Math.max(maxZ, box.maxZ);
		}
		
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
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
