package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll.CreativeCollector;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll.StackCollector;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll.StackSelector;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.items.ItemTileContainer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class LittleSubGuiUtils {
	
	public static class LittleBlockSelector extends GuiStackSelectorAll.BlockSelector {
		
		@Override
		public boolean allow(ItemStack stack)
		{
			if(super.allow(stack))
			{
				Block block = Block.getBlockFromItem(stack.getItem());
				if(block != null && !(block instanceof BlockAir))
					return SubContainerGrabber.isBlockValid(block);
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
			HashMapList<String,	ItemStack> stacks = super.collect(player);
			
			BlockIngredients ingredients = new BlockIngredients();
			for(ItemStack bag : LittleAction.getBags(player))
				ingredients.addIngredients(ItemTileContainer.loadInventory(bag));			
			
			List<ItemStack> newStacks = new ArrayList<>();
			for (BlockIngredient ingredient : ingredients.getIngredients()) {
				newStacks.add(ingredient.getItemStack());
			}
			stacks.add("selector.ingredients", newStacks);
			return stacks;
		}
		
	}
	
	public static StackCollector getCollector(EntityPlayer player)
	{
		if(player.isCreative())
			return new GuiStackSelectorAll.CreativeCollector(new LittleBlockSelector());
		return new LittleBlockCollector(new LittleBlockSelector());
	}
	
}
