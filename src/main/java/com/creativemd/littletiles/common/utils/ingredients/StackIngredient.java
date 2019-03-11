package com.creativemd.littletiles.common.utils.ingredients;

import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;

import net.minecraft.item.ItemStack;

public class StackIngredient {
	
	public final ItemStack stack;
	public int count;
	
	public StackIngredient(ItemStack stack) {
		this.stack = stack;
		this.count = stack.getCount();
	}
	
	@Override
	public int hashCode() {
		return stack.getItem().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StackIngredient)
			return InventoryUtils.isItemStackEqual(stack, ((StackIngredient) obj).stack);
		return false;
	}
	
	@Override
	public String toString() {
		return stack.toString();
	}
	
	public boolean drain(List<ItemStack> inventory) {
		for (Iterator iterator = inventory.iterator(); iterator.hasNext();) {
			ItemStack invStack = (ItemStack) iterator.next();
			if (InventoryUtils.isItemStackEqual(invStack, stack)) {
				int amount = Math.min(stack.getCount(), invStack.getCount());
				if (amount > 0) {
					invStack.shrink(amount);
					stack.shrink(amount);
				}
				
				if (invStack.isEmpty())
					iterator.remove();
				
				if (stack.isEmpty())
					return true;
			}
		}
		return false;
	}
}
