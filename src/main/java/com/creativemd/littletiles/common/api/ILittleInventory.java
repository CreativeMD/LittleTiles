package com.creativemd.littletiles.common.api;

import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;

import net.minecraft.item.ItemStack;

/** must be implemented by an item * */
public interface ILittleInventory {
	
	public LittleIngredients getInventory(ItemStack stack);
	
	public void setInventory(ItemStack stack, LittleIngredients ingredients);
	
}
