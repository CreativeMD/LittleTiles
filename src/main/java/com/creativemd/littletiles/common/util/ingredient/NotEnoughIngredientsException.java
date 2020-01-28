package com.creativemd.littletiles.common.util.ingredient;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;

import net.minecraft.item.ItemStack;

public class NotEnoughIngredientsException extends LittleActionException {
	
	protected LittleIngredients ingredients;
	
	protected NotEnoughIngredientsException(String msg, LittleIngredient ingredient) {
		super(msg);
		this.ingredients = new LittleIngredients();
		this.ingredients.set(ingredient.getClass(), ingredient);
	}
	
	protected NotEnoughIngredientsException(String msg, LittleIngredients ingredients) {
		super(msg);
		this.ingredients = ingredients;
	}
	
	public NotEnoughIngredientsException(LittleIngredient ingredient) {
		this("exception.ingredient.missing", ingredient);
	}
	
	public NotEnoughIngredientsException(ItemStack stack) {
		this(new StackIngredient());
		ingredients.get(StackIngredient.class).add(new StackIngredientEntry(stack, stack.getCount()));
	}
	
	public NotEnoughIngredientsException(LittleIngredients ingredients) {
		super("exception.ingredient.missing");
		this.ingredients = ingredients;
	}
	
	public LittleIngredients getIngredients() {
		return ingredients;
	}
	
	@Override
	public ActionMessage getActionMessage() {
		String message = getLocalizedMessage() + "\n";
		List objects = new ArrayList();
		for (LittleIngredient ingredient : ingredients)
			message += ingredient.print(objects);
		return new ActionMessage(message, objects.toArray());
	}
	
	public static class NotEnoughSpaceException extends NotEnoughIngredientsException {
		
		public NotEnoughSpaceException(LittleIngredient ingredient) {
			super("exception.ingredient.space", ingredient);
		}
		
		public NotEnoughSpaceException(LittleIngredients ingredients) {
			super("exception.ingredient.space", ingredients);
		}
		
		public NotEnoughSpaceException(ItemStack stack) {
			this(new StackIngredient());
			ingredients.get(StackIngredient.class).add(new StackIngredientEntry(stack, stack.getCount()));
		}
		
	}
}
