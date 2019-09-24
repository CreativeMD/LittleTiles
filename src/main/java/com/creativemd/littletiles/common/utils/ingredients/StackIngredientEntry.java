package com.creativemd.littletiles.common.utils.ingredients;

import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;

import net.minecraft.item.ItemStack;

public class StackIngredientEntry {
	
	public final ItemStack stack;
	public int count;
	
	public StackIngredientEntry(ItemStack stack, int count) {
		this.stack = stack;
		this.count = count;
	}
	
	@Override
	public int hashCode() {
		return stack.getItem().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StackIngredientEntry)
			return InventoryUtils.isItemStackEqual(stack, ((StackIngredientEntry) obj).stack);
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
	
	public StackIngredientEntry copy() {
		return new StackIngredientEntry(stack, count);
	}
	
	public boolean isEmpty() {
		return count <= 0;
	}
	
	public void scale(int count) {
		this.count *= count;
	}
	
}
