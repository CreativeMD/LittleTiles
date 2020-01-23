package com.creativemd.littletiles.client.gui.controls;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.littletiles.common.item.ItemBlockIngredient;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SlotControlBlockIngredient extends SlotControl {
	
	public SlotControlBlockIngredient(Slot slot) {
		super(slot);
	}
	
	public BlockIngredientEntry getIngredient() {
		return ItemBlockIngredient.loadIngredient(slot.getStack());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiControl createGuiControl() {
		GuiControl control = new GuiSlotControlBlockIngredient(slot.xPos, slot.yPos, this);
		return control;
	}
	
	@Override
	public boolean canMergeIntoInventory(Slot mergeSlot) {
		return super.canMergeIntoInventory(mergeSlot) && !mergeSlot.inventory.getName().equals("input");
	}
	
	public boolean isEntireBlock() {
		BlockIngredientEntry entry = getIngredient();
		if (entry != null && entry.value >= 1)
			return true;
		return false;
	}
	
	@Override
	public void transferIntoOtherInventory(int amount) {
		if (!isEntireBlock())
			super.transferIntoOtherInventory(amount);
		
		BlockIngredientEntry entry = getIngredient();
		if (entry == null)
			return;
		
		ItemStack stack = entry.getItemStack();
		amount = Math.min(amount, (int) entry.value);
		stack.setCount(amount);
		
		mergeToOtherInventory(stack, false);
		if (!stack.isEmpty())
			mergeToOtherInventory(stack, true);
		
		if (!stack.isEmpty())
			amount -= stack.getCount();
		
		entry.value -= amount;
		ItemBlockIngredient.saveIngredient(slot.getStack(), entry);
	}
	
	@Override
	public void takeStack(boolean leftClick, InventoryPlayer inventoryplayer) {
		if (!isEntireBlock()) {
			super.takeStack(leftClick, inventoryplayer);
			return;
		}
		
		ItemStack hand = getPlayer().inventory.getItemStack();
		
		BlockIngredientEntry entry = getIngredient();
		ItemStack slotItem = entry.getItemStack();
		slotItem.setCount((int) entry.value);
		
		if (leftClick) {
			int stackSize = Math.min(Math.min(slotItem.getCount(), slotItem.getMaxStackSize()), slotItem.getCount());
			slotItem.setCount(stackSize);
			inventoryplayer.setItemStack(slotItem);
			
			entry.value -= slotItem.getCount();
			ItemBlockIngredient.saveIngredient(slot.getStack(), entry);
		}
	}
}
