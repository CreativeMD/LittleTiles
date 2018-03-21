package com.creativemd.littletiles.common.tiles.preview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

public class LittlePreviews implements Iterable<LittleTilePreview> {
	
	protected List<LittleTilePreview> previews;
	public LittleGridContext context;
	
	public LittlePreviews(LittleGridContext context) {
		this.context = context;
		this.previews = new ArrayList<>();
	}
	
	public boolean isAbsolute()
	{
		return false;
	}
	
	public BlockPos getBlockPos()
	{
		return null;
	}
	
	public void convertTo(LittleGridContext to)
	{
		for (LittleTilePreview preview : previews) {
			preview.convertTo(this.context, to);
		}
		this.context = to;
	}
	
	public void convertToSmallest()
	{
		int size = LittleGridContext.minSize;
		for (LittleTilePreview preview : previews) {
			size = Math.max(size, preview.getSmallestContext(context));
		}
		
		if(size < context.size)
			convertTo(LittleGridContext.get(size));
	}

	public LittlePreviews copy()
	{
		LittlePreviews previews = new LittlePreviews(context);
		previews.previews.addAll(this.previews);
		return previews;
	}
	
	protected LittleTilePreview getPreview(LittleTile tile)
	{
		LittleTilePreview preview = tile.getPreviewTile();
		LittleGridContext context = tile.getContext();
		if(this.context != context)
		{
			if(this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		return preview;
	}
	
	public LittleTilePreview addPreview(BlockPos pos, LittleTilePreview preview, LittleGridContext context)
	{
		if(this.context != context)
		{
			if(this.context.size > context.size)
				preview.convertTo(context, this.context);
			else
				convertTo(context);
		}
		
		previews.add(preview);
		return preview;
	}
	
	public LittleTilePreview addTile(LittleTile tile)
	{
		LittleTilePreview preview = getPreview(tile);
		previews.add(preview);
		return preview;
		
	}
	public LittleTilePreview addTile(LittleTile tile, LittleTileVec offset)
	{
		LittleTilePreview preview = getPreview(tile);
		preview.box.addOffset(offset);
		return addPreview(null, tile.getPreviewTile(), tile.getContext());
	}
	
	public void addTiles(List<LittleTile> tiles)
	{
		if(tiles.isEmpty())
			return;
		
		for (LittleTile tile : tiles) {
			addTile(tile);
		}
	}
	
	@Override
	public Iterator<LittleTilePreview> iterator() {
		return previews.iterator();
	}
	
	public static LittlePreviews getPreview(ItemStack stack, boolean allowLowResolution)
	{
		if(!stack.hasTagCompound())
			return new LittlePreviews(LittleGridContext.get());
		
		LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
		if(stack.getTagCompound().getTag("tiles") instanceof NBTTagInt)
		{
			LittlePreviews previews = new LittlePreviews(context);
			int tiles = stack.getTagCompound().getInteger("tiles");
			for (int i = 0; i < tiles; i++) {
				NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
				LittleTilePreview preview = LittleTilePreview.loadPreviewFromNBT(nbt);
				if(preview != null)
					previews.previews.add(preview);
			}
			return previews;
		}else{
			if(allowLowResolution && stack.getTagCompound().hasKey("pos"))
			{
				LittlePreviews previews = new LittlePreviews(context);
				NBTTagCompound tileData = new NBTTagCompound();
				LittleTile tile = new LittleTileBlock(LittleTiles.coloredBlock);
				tile.saveTileExtra(tileData);
				tileData.setString("tID", tile.getID());	
				
				NBTTagList list = stack.getTagCompound().getTagList("pos", 11);
				for (int i = 0; i < list.tagCount(); i++) {
					int[] array = list.getIntArrayAt(i);
					previews.previews.add(new LittleTilePreview(new LittleTileBox(array[0] * context.size, array[1] * context.size, array[2] * context.size,
							array[0] * context.size + context.maxPos, array[1] * context.size + context.maxPos, array[02] * context.size + context.maxPos), tileData));
				}
				return previews;
			}
			return LittleNBTCompressionTools.readPreviews(context, stack.getTagCompound().getTagList("tiles", 10));
		}
	}

	public LittleTilePreview get(int index)
	{
		return previews.get(index);
	}
	
	public int size() {
		return previews.size();
	}

	public void ensureContext(LittleGridContext context)
	{
		if(this.context.size < context.size)
			convertTo(context);
	}

	public boolean isEmpty() {
		return previews.isEmpty();
	}
	
	public void addWithoutCheckingPreview(LittleTilePreview preview)
	{
		previews.add(preview);
	}
}
