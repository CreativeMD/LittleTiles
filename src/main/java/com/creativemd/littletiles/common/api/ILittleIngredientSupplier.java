package com.creativemd.littletiles.common.api;

import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import net.minecraft.item.ItemStack;

public interface ILittleIngredientSupplier {
	
	/** Requests ingredients which are not available directly
	 * 
	 * @param stack
	 *            the ingredient supplier itself
	 * @param ingredients
	 *            the ingredients that are requested
	 * @param overflow
	 *            the overflow ingredients
	 * @param inventory
	 *            the player that is requesting the material */
	public void requestIngredients(ItemStack stack, LittleIngredients ingredients, LittleIngredients overflow, LittleInventory inventory);
}
