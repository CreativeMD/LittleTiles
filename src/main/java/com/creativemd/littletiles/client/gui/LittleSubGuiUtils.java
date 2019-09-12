package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll.StackCollector;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll.StackSelector;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleInventory;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class LittleSubGuiUtils {
	
	public static class LittleBlockSelector extends GuiStackSelectorAll.BlockSelector {
		
		@Override
		public boolean allow(ItemStack stack) {
			if (super.allow(stack)) {
				Block block = Block.getBlockFromItem(stack.getItem());
				if (block != null && !(block instanceof BlockAir))
					return LittleAction.isBlockValid(block);
			}
			return false;
		}
		
	}
	
	public static class LittleBlockCollector extends GuiStackSelectorAll.InventoryCollector {
		
		public LittleBlockCollector(StackSelector selector) {
			super(selector);
		}
		
		@Override
		public HashMapList<String, ItemStack> collect(EntityPlayer player) {
			HashMapList<String, ItemStack> stacks = super.collect(player);
			
			BlockIngredient ingredients = new BlockIngredient();
			for (ItemStack stack : LittleAction.getInventories(player)) {
				LittleIngredients inventory = ((ILittleInventory) stack.getItem()).getInventory(stack);
				if (inventory != null)
					ingredients.add(inventory.get(BlockIngredient.class));
			}
			
			List<ItemStack> newStacks = new ArrayList<>();
			for (BlockIngredientEntry ingredient : ingredients) {
				ItemStack stack = ingredient.getItemStack();
				
				stack.setCount(Math.max(1, (int) ingredient.value));
				
				NBTTagCompound display = new NBTTagCompound();
				NBTTagList list = new NBTTagList();
				int blocks = (int) ingredient.value;
				double pixel = (ingredient.value - blocks) * LittleGridContext.get().maxTilesPerBlock;
				list.appendTag(new NBTTagString((blocks > 0 ? blocks + " blocks " : "") + (pixel > 0 ? (Math.round(pixel * 100) / 100) + " pixel" : "")));
				display.setTag("Lore", list);
				stack.setTagInfo("display", display);
				newStacks.add(stack);
			}
			stacks.add("selector.ingredients", newStacks);
			return stacks;
		}
		
	}
	
	public static StackCollector getCollector(EntityPlayer player) {
		if (player.isCreative())
			return new GuiStackSelectorAll.CreativeCollector(new LittleBlockSelector());
		return new LittleBlockCollector(new LittleBlockSelector());
	}
	
}
