package com.creativemd.littletiles.common.utils.ingredients;

import com.creativemd.littletiles.common.action.LittleActionException;

public class NotEnoughIngredientsException extends LittleActionException {
	
	public LittleIngredient ingredient;
	
	protected NotEnoughIngredientsException(String msg, LittleIngredient ingredient) {
		super(msg);
		this.ingredient = ingredient;
	}
	
	public NotEnoughIngredientsException(LittleIngredient ingredient) {
		super("exception.ingredient.space");
		this.ingredient = ingredient;
	}
	
	public static class NotEnoughSpaceException extends NotEnoughIngredientsException {
		
		public NotEnoughSpaceException(LittleIngredient ingredient) {
			super("exception.ingredient.space.volume", ingredient);
		}
		
	}
}
