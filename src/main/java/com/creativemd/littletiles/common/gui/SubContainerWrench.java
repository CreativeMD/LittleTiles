package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemTileContainer.BlockEntry;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerWrench extends SubContainer{
	
	public InventoryBasic basic = new InventoryBasic("default", false, 2);
	
	public SubContainerWrench(EntityPlayer player) {
		super(player);
	}

	@Override
	public void createControls() {
		addSlotToContainer(new Slot(basic, 0, 5, 5));
		addSlotToContainer(new Slot(basic, 1, 35, 5));
		
		addPlayerSlotsToContainer(player);
	}
	
	public static ArrayList<BlockEntry> getMissing(ArrayList<LittleTilePreview> tiles, ArrayList<BlockEntry> entries)
	{
		ArrayList<BlockEntry> missing = new ArrayList<>();
		for (int i = 0; i < tiles.size(); i++) {
			Block blockofTile = tiles.get(i).getPreviewBlock();
			int meta = tiles.get(i).getPreviewBlockMeta();
			double size = tiles.get(i).size.getPercentVolume();
			int j = 0;
			boolean found = false;
			while(j < entries.size())
			{
				if(blockofTile == entries.get(j).block && meta == entries.get(j).meta)
				{
					double amount = Math.min(entries.get(j).value, size);
					entries.get(j).value -= amount;
					size -= amount;
					
					if(entries.get(j).value <= 0)
					{
						entries.remove(j);
						if(size > 0)
							continue;
					}
					
					if(size <= 0)
					{
						found = true;
						break;
					}
				}
				j++;
			}
			if(!found)
			{
				BlockEntry entry = new BlockEntry(blockofTile, meta, size);
				int index = missing.indexOf(entry);
				if(index == -1)
					missing.add(entry);
				else
					missing.get(index).value += size;
			}
		}
		return missing;
	}
	
	public static ArrayList<BlockEntry> getContentofStack(ItemStack stack)
	{
		ArrayList<BlockEntry> entries = new ArrayList<BlockEntry>();
		if(stack != null)
		{
			ILittleTile tile = PlacementHelper.getLittleInterface(stack);
			
			if(tile != null)
			{
				ArrayList<LittleTilePreview> tiles = tile.getLittlePreview(stack);
				if(tiles != null)
				{
					for (int i = 0; i < tiles.size(); i++) {
						
						Block block2 = tiles.get(i).getPreviewBlock();
						if(block2 != null && !(block2 instanceof BlockAir))
							entries.add(new BlockEntry(block2, tiles.get(i).getPreviewBlockMeta(), tiles.get(i).size.getPercentVolume()));
					}
					
				}
			}
			
			if(stack.getItem() instanceof ItemTileContainer)
			{
				entries.addAll(ItemTileContainer.loadMap(stack));
			}
			
			Block block = Block.getBlockFromItem(stack.getItem());
					
			if(block != null && !(block instanceof BlockAir))
			{
				if(SubContainerHammer.isBlockValid(block))
				{
					entries.add(new BlockEntry(block, stack.getItemDamage(), 1));
				}
			}
		}
		return entries;
	}
	
	@Override
	public void onClosed()
	{
		for (int i = 0; i < basic.getSizeInventory(); i++) {
			if(basic.getStackInSlot(i) != null)
				player.dropItem(basic.getStackInSlot(i), false);
		}
	}

	@Override
	public void onPacketReceive(NBTTagCompound nbt) {
		ItemStack stack1 = basic.getStackInSlot(0);
		ItemStack stack2 = basic.getStackInSlot(1);
		if(stack1 != null)
		{
			if(stack1.getItem() instanceof ItemRecipe)
			{
				if(stack1.getTagCompound() != null && !stack1.getTagCompound().hasKey("x"))
				{
					boolean enough = true;
					
					ArrayList<BlockEntry> entries = getContentofStack(stack2);
					ArrayList<LittleTilePreview> tiles = ItemRecipe.getPreview(stack1);
					
					if(!player.capabilities.isCreativeMode){
						enough = getMissing(tiles, entries).size() == 0;
					}
					
					
					if(enough)
					{
						if(stack2 != null && !player.capabilities.isCreativeMode)
						{
							if(stack2.getItem() instanceof ItemTileContainer)
							{
								ItemTileContainer.saveMap(stack2, entries);
							}else{
								stack2.stackSize--;
								if(stack2.stackSize == 0)
									basic.setInventorySlotContents(1, null);
								if(entries.size() > 0){
									for (int i = 0; i < entries.size(); i++) {
										ItemTileContainer.addBlock(player, entries.get(i).block, entries.get(i).meta, entries.get(i).value);
									}
								}
							}
						}
						
						ItemStack stack = new ItemStack(LittleTiles.multiTiles);
						stack.setTagCompound((NBTTagCompound) stack1.getTagCompound().copy());
						if(!player.inventory.addItemStackToInventory(stack))
							WorldUtils.dropItem(player, stack);
					}
				}
			}else if(ChiselsAndBitsManager.isChiselsAndBitsStructure(stack1)){
				ArrayList<LittleTilePreview> previews = ChiselsAndBitsManager.getPreviews(stack1);
				if(previews != null && !previews.isEmpty() && stack2 == null)
				{
					stack2 = new ItemStack(LittleTiles.multiTiles);
					ItemRecipe.savePreviewTiles(previews, stack2);
					basic.setInventorySlotContents(0, null);
					basic.setInventorySlotContents(1, stack2);
				}
			}else{
				ILittleTile tile = PlacementHelper.getLittleInterface(stack1);
				if(tile != null && stack2 != null && stack2.getItem() instanceof ItemRecipe)
				{
					stack2.setTagCompound((NBTTagCompound) stack1.getTagCompound().copy());
				}
			}				
		}
	}

}
