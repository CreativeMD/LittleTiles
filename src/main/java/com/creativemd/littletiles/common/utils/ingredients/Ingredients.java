package com.creativemd.littletiles.common.utils.ingredients;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient.BlockIngredients;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class Ingredients {
	
	public ColorUnit color;
	public BlockIngredients block;
	private List<StackIngredient> stacks = new ArrayList<>();
	
	public Ingredients() {
		this(new ColorUnit(), new BlockIngredients());
	}
	
	public Ingredients(ColorUnit color, BlockIngredients block) {
		this.color = color;
		this.block = block;
	}
	
	public Ingredients add(Ingredients ingredients) {
		
		if (color == null)
			color = ingredients.color;
		else if (ingredients.color != null)
			color.addColorUnit(ingredients.color);
		
		if (block == null)
			block = ingredients.block;
		else if (ingredients.block != null)
			block.addIngredients(ingredients.block);
		
		if (stacks == null)
			stacks = ingredients.stacks;
		else if (ingredients.stacks != null)
			for (StackIngredient stack : stacks)
				addStack(stack);
			
		return this;
	}
	
	public void addPreview(LittleGridContext context, List<LittleTilePreview> previews) {
		for (LittleTilePreview preview : previews) {
			addPreview(context, preview);
		}
	}
	
	public void addPreview(LittleGridContext context, LittleTilePreview preview) {
		if (preview.canBeConvertedToBlockEntry()) {
			block.addIngredient(preview.getBlockIngredient(context));
			color.addColorUnit(ColorUnit.getColors(context, preview));
		}
	}
	
	public void addPreview(LittleTilePreview preview, double volume) {
		if (preview.canBeConvertedToBlockEntry()) {
			BlockIngredient ingredient = preview.getBlockIngredient(LittleGridContext.get());
			ingredient.value = volume;
			block.addIngredient(ingredient);
			color.addColorUnit(ColorUnit.getColors(preview, volume));
		}
	}
	
	public List<StackIngredient> getStacks() {
		return stacks;
	}
	
	public void addStack(ItemStack stack) {
		if (stack.isEmpty())
			return;
		
		StackIngredient ingredient = new StackIngredient(stack);
		
		int index = stacks.indexOf(ingredient);
		if (index == -1)
			stacks.add(ingredient);
		else
			stacks.get(index).count += stack.getCount();
	}
	
	public void addStack(StackIngredient ingredient) {
		
		int index = stacks.indexOf(ingredient);
		if (index == -1)
			stacks.add(ingredient);
		else
			stacks.get(index).count += ingredient.count;
	}
	
	public void addStack(IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); i++)
			addStack(inventory.getStackInSlot(i));
	}
	
	public boolean hasStacks() {
		return stacks != null && !stacks.isEmpty();
	}
	
}
