package com.creativemd.littletiles.common.util.ingredient;

import com.creativemd.littletiles.common.item.ItemLittleBag;

public class LittleBagInventory extends LittleIngredients {
	
	public LittleBagInventory() {
		content[LittleIngredient.indexOf(BlockIngredient.class)] = new BlockIngredient().setLimits(ItemLittleBag.inventorySize, ItemLittleBag.maxStackSize);
		content[LittleIngredient.indexOf(ColorIngredient.class)] = new ColorIngredient().setLimit(ItemLittleBag.colorUnitMaximum);
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
