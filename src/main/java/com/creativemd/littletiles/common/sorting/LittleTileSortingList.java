package com.creativemd.littletiles.common.sorting;

import java.util.HashMap;
import java.util.Map.Entry;

import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**This stores all LittleTiles bound to an block (meta). Used for vanilla blocks, but can also be used by modders.
 * This is an easier way than implementing the interface, but you cannot add specialized configurations
 * @author CreativeMD
 *
 */
public class LittleTileSortingList {
	
	public static HashMap<SortingTile, LittleTile> tiles = new HashMap<SortingTile, LittleTile>();
	
	public static LittleTile getLittleTile(ItemStack stack)
	{
		if(stack == null || stack.getItem() == null)
			return null;
		
		if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile)
		{
			return ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittleTile(stack);
		}
		
		for (Entry<SortingTile, LittleTile> entry : tiles.entrySet()) {
			SortingTile tile = entry.getKey();
			if(Block.getBlockFromItem(stack.getItem()) == tile.block)
			{
				if(tile.meta == -1 || stack.getItemDamage() == tile.meta)
					return entry.getValue();
			}else if(stack.getItem() == tile.customItemBlock){
				if(tile.custommeta == -1 || stack.getItemDamage() == tile.custommeta)
					return entry.getValue();
			}
	    }
		return null;
	}
	
	/**NOTE: this adds only custom Tiles. Every solid normal cube can be used as a littletile without configuration*/
	public static void initVanillaBlocks()
	{
		
	}
	
	public static class SortingTile{
		
		public Block block;
		
		public Item customItemBlock = null;
		public int custommeta = -1;
		public int meta = -1;
		
		public SortingTile(Block block)
		{
			this.block = block;
		}
		
		public SortingTile(Block block, int meta)
		{
			this(block);
			this.meta = meta;
		}
		
		public SortingTile setCustomItemBlock(Item itemBlock)
		{
			this.customItemBlock = itemBlock;
			return this;
		}
		
		public SortingTile setCustomItemBlock(Item itemBlock, int meta)
		{
			this.custommeta = meta;
			return setCustomItemBlock(itemBlock);
		}
	}
	
}
