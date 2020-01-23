package com.creativemd.littletiles.common.util.ingredient;

import com.creativemd.littletiles.common.item.ItemBag;

public class LittleBagInventory extends LittleIngredients {
	
	public LittleBagInventory() {
		content[LittleIngredient.indexOf(BlockIngredient.class)] = new BlockIngredient().setLimits(ItemBag.inventorySize, ItemBag.maxStackSize);
		content[LittleIngredient.indexOf(ColorIngredient.class)] = new ColorIngredient().setLimit(ItemBag.colorUnitMaximum);
	}
	
	@Override
	public LittleBagInventory copy() {
		LittleBagInventory bag = new LittleBagInventory();
		bag.assignContent(this);
		return bag;
	}
	
	@Override
	protected boolean removeEmptyIngredients() {
		return false;
	}
	
	@Override
	protected boolean canAddNewIngredients() {
		return false;
	}
	
}
