package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.HashMap;

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
import net.minecraft.inventory.IInventory;
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
	
	public static boolean drainIngridients(ArrayList<BlockEntry> ingredients, ItemStack stack, boolean drain, ArrayList<BlockEntry> remaining, boolean useStructures)
	{
		ArrayList<BlockEntry> content = getContentofStack(stack, useStructures);
		for (int i = 0; i < content.size(); i++) {
			if(!ingredients.isEmpty())
			{
				BlockEntry entry = content.get(i);
				int index = ingredients.indexOf(entry);
				if(index != -1)
				{
					BlockEntry ingredient = ingredients.get(index);
					int takenStackSize = (int) Math.min(stack.stackSize, Math.ceil(ingredient.value / entry.value));
					if(takenStackSize > 0)
					{
						double takenVolume = Math.min(ingredient.value, entry.value*takenStackSize);
						ingredient.value -= takenVolume;
						if(drain)
						{
							if(!(stack.getItem() instanceof ItemTileContainer))
								stack.stackSize -= takenStackSize;
							else
								ItemTileContainer.drainBlock(stack, entry.block, entry.meta, entry.value*takenStackSize);
							if(takenVolume < entry.value*takenStackSize)
							{
								entry.value = entry.value*takenStackSize - takenVolume;
								remaining.add(entry);
							}
						}
					}
					if(ingredient.value <= 0)
						ingredients.remove(ingredient);
				}
			}
		}
		return ingredients.isEmpty();
	}
	
	public static boolean drainIngridients(ArrayList<BlockEntry> ingredients, IInventory inventory, boolean drain, ArrayList<BlockEntry> remaining, boolean useStructures)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null)
			{
				boolean result = drainIngridients(ingredients, stack, drain, remaining, useStructures);
				if(stack.stackSize <= 0)
					inventory.setInventorySlotContents(i, null);
				if(result)
					return true;
			}
		}
		return false;
	}
	
	public static ArrayList<BlockEntry> getRequiredIngredients(ArrayList<LittleTilePreview> previews)
	{
		ArrayList<BlockEntry> ingredients = new ArrayList<>();
		for (int i = 0; i < previews.size(); i++) {
			BlockEntry entry = previews.get(i).getBlockEntry();
			int index = ingredients.indexOf(entry);
			if(index != -1)
				ingredients.get(index).value += entry.value;
			else
				ingredients.add(entry);
		}
		return ingredients;
	}
	
	/*public static ArrayList<BlockEntry> getMissing(ArrayList<LittleTilePreview> tiles, ArrayList<BlockEntry> entries)
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
	}*/
	
	public static ArrayList<BlockEntry> getContentofStack(ItemStack stack, boolean useStructures)
	{
		ArrayList<BlockEntry> entries = new ArrayList<BlockEntry>();
		if(stack != null)
		{
			if(useStructures)
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
								entries.add(tiles.get(i).getBlockEntry());
						}
						return entries;
					}
				}
			}
			
			if(stack.getItem() instanceof ItemTileContainer)
			{
				entries.addAll(ItemTileContainer.loadMap(stack));
			}else{
				Block block = Block.getBlockFromItem(stack.getItem());
						
				if(block != null && !(block instanceof BlockAir))
				{
					if(SubContainerHammer.isBlockValid(block))
					{
						entries.add(new BlockEntry(block, stack.getItemDamage(), 1));
					}
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
					ArrayList<LittleTilePreview> tiles = ItemRecipe.getPreview(stack1);
					ArrayList<BlockEntry> required = SubContainerWrench.getRequiredIngredients(tiles);
					ArrayList<BlockEntry> remaining = new ArrayList<>();
					
					boolean success = true;
					if(!player.isCreative()){
						success = SubContainerWrench.drainIngridients(required, stack2, false, remaining, true) || SubContainerWrench.drainIngridients(required, getPlayer().inventory, false, remaining, false);
					}
					
					if(stack2 != null && stack2.stackSize <= 0)
						basic.setInventorySlotContents(1, null);
					
					if(remaining.size() > 0 && !ItemTileContainer.canStoreRemains(getPlayer()))
						success = false;
					
					if(success)
					{
						if(!player.isCreative()){
							required = SubContainerWrench.getRequiredIngredients(tiles);
							remaining = new ArrayList<>();
							if(SubContainerWrench.drainIngridients(required, stack2, true, remaining, true) || SubContainerWrench.drainIngridients(required, getPlayer().inventory, true, remaining, false))
							{
								
								for (int i = 0; i < remaining.size(); i++) {
									ItemTileContainer.addBlock(player, remaining.get(i).block, remaining.get(i).meta, remaining.get(i).value);
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
