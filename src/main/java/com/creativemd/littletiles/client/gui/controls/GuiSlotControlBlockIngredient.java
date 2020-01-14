package com.creativemd.littletiles.client.gui.controls;

import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.creativecore.common.gui.controls.container.client.GuiSlotControl;
import com.creativemd.littletiles.common.items.ItemBlockIngredient;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;

import net.minecraft.item.ItemStack;

public class GuiSlotControlBlockIngredient extends GuiSlotControl {
	
	public GuiSlotControlBlockIngredient(int x, int y, SlotControl slot) {
		super(x, y, slot);
	}
	
	@Override
	public ItemStack getStackToRender() {
		ItemStack stack = super.getStackToRender().copy();
		BlockIngredientEntry entry = ItemBlockIngredient.loadIngredient(stack);
		if (entry != null && entry.value > 1)
			stack.setCount((int) entry.value);
		return stack;
	}
	
}
