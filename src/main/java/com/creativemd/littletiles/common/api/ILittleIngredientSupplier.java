package com.creativemd.littletiles.common.api;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ILittleIngredientSupplier {
	
	/** can be used to add itemstacks to the collection
	 * 
	 * @param collected
	 *            multiple categories represented by a string (which is translatable)
	 * @param stack
	 *            the ingredient supplier itself
	 * @param player
	 *            the player if there is one */
	public void collect(HashMapList<String, ItemStack> collected, ItemStack stack, @Nullable EntityPlayer player);
	
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
