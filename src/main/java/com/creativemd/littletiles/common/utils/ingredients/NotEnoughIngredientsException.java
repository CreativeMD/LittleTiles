package com.creativemd.littletiles.common.utils.ingredients;

import com.creativemd.littletiles.common.action.LittleActionException;

import net.minecraft.item.ItemStack;

public class NotEnoughIngredientsException extends LittleActionException {
	
	protected LittleIngredients ingredients;
	
	protected NotEnoughIngredientsException(String msg, LittleIngredient ingredient) {
		super(msg);
		this.ingredients = new LittleIngredients();
		this.ingredients.set(ingredient);
	}
	
	public NotEnoughIngredientsException(LittleIngredient ingredient) {
		this("exception.ingredient.space", ingredient);
	}
	
	public NotEnoughIngredientsException(ItemStack stack) {
		this(new StackIngredient());
		ingredients.get(StackIngredient.class).add(new StackIngredientEntry(stack, stack.getCount()));
	}
	
	public NotEnoughIngredientsException(LittleIngredients ingredients) {
		super("exception.ingredient.space");
		this.ingredients = ingredients;
	}
	
	public LittleIngredients getIngredients() {
		return ingredients;
	}
	
	public static class NotEnoughSpaceException extends NotEnoughIngredientsException {
		
		public NotEnoughSpaceException(LittleIngredient ingredient) {
			super("exception.ingredient.space.volume", ingredient);
		}
		
		public NotEnoughSpaceException(ItemStack stack) {
			this(new StackIngredient());
			ingredients.get(StackIngredient.class).add(new StackIngredientEntry(stack, stack.getCount()));
		}
		
	}
}
