package com.creativemd.littletiles.common.api;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface ILittleInventory {
	
	public default boolean canBeFilled(ItemStack stack) {
		return true;
	}
	
	public IInventory getInventory(ItemStack stack);
	
	public void setInventory(ItemStack stack, IInventory inventory);
	
}
