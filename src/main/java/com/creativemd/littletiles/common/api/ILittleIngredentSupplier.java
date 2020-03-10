package com.creativemd.littletiles.common.api;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ILittleIngredentSupplier {
	
	/** Requests ingredients which are not available directly
	 * 
	 * @param stack
	 *            the ingredient supplier itself
	 * @param ingredients
	 *            the ingredients that are requested
	 * @param overflow
	 *            the overflow ingredients
	 * @param player
	 *            the player that is requesting the material */
	public void requestIngredients(ItemStack stack, LittleIngredients ingredients, LittleIngredients overflow, @Nullable EntityPlayer player);
}
