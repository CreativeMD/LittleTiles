package com.creativemd.littletiles.common.api;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;
import com.creativemd.littletiles.common.utils.ingredients.LittleInventory;

import net.minecraft.item.ItemStack;

/** must be implemented by an item * */
public interface ILittleInventory {
	
	public default boolean shouldBeMerged() {
		return false;
	}
	
	public LittleIngredients getInventory(ItemStack stack);
	
	public void setInventory(ItemStack stack, LittleIngredients ingredients, @Nullable LittleInventory inventory);
	
}
