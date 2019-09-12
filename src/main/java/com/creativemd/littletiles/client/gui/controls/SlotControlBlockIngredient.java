package com.creativemd.littletiles.client.gui.controls;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.controls.container.SlotControl;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SlotControlBlockIngredient extends SlotControl {
	
	public SlotControlBlockIngredient(Slot slot, BlockIngredientEntry ingredient) {
		super(slot);
		this.ingredient = ingredient;
	}
	
	public BlockIngredientEntry ingredient;
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiControl createGuiControl() {
		GuiControl control = new GuiSlotControlBlockIngredient(slot.xPos, slot.yPos, this, ingredient);
		return control;
	}
	
	@Override
	public int getStackLimit(Slot slot, ItemStack stack) {
		return LittleGridContext.get().maxTilesPerBlock;
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return LittleGridContext.get().maxTilesPerBlock;
	}
	
	@Override
	public boolean canMergeIntoInventory(Slot mergeSlot) {
		return super.canMergeIntoInventory(mergeSlot) && !mergeSlot.inventory.getName().equals("input");
	}
	
	public boolean isFullItem() {
		return slot.getStack().getCount() >= LittleGridContext.get().maxTilesPerBlock;
	}
	
	public ItemStack getFullStack() {
		if (ingredient != null) {
			ItemStack stack = ingredient.getItemStack();
			stack.setCount(slot.getStack().getCount() / LittleGridContext.get().maxTilesPerBlock);
			return stack;
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public void transferIntoOtherInventory(int amount) {
		if (!isFullItem())
			super.transferIntoOtherInventory(amount);
		
		ItemStack stack = getFullStack();
		int countbefore = stack.getCount();
		if (amount > stack.getCount())
			amount = stack.getCount();
		ItemStack copy = stack.copy();
		copy.setCount(amount);
		int minAmount = stack.getCount() - amount;
		
		mergeToOtherInventory(copy, false);
		if (!copy.isEmpty())
			mergeToOtherInventory(copy, true);
		
		if (copy.isEmpty())
			stack.setCount(minAmount);
		else
			stack.shrink(amount - copy.getCount());
		
		slot.getStack().shrink((countbefore - stack.getCount()) * LittleGridContext.get().maxTilesPerBlock);
	}
	
	@Override
	public void takeStack(boolean leftClick, InventoryPlayer inventoryplayer) {
		if (!isFullItem()) {
			super.takeStack(leftClick, inventoryplayer);
			return;
		}
		
		ItemStack hand = getPlayer().inventory.getItemStack();
		ItemStack slotItem = getFullStack();
		
		int countbefore = slotItem.getCount();
		
		if (leftClick) {
			int stackSize = Math.min(Math.min(slotItem.getCount(), slotItem.getMaxStackSize()), slotItem.getCount());
			ItemStack newHand = slotItem.copy();
			newHand.setCount(stackSize);
			inventoryplayer.setItemStack(newHand);
			slotItem.shrink(stackSize);
			
			slot.getStack().shrink((countbefore - slotItem.getCount()) * LittleGridContext.get().maxTilesPerBlock);
			
			slot.onTake(getPlayer(), inventoryplayer.getItemStack());
		}
	}
}
