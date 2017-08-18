package com.creativemd.littletiles.common.action.block;

import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.ingredients.ColorUnit;

import net.minecraft.item.ItemStack;

public abstract class NotEnoughIngredientsException extends LittleActionException {

	public NotEnoughIngredientsException(String msg) {
		super(msg);
	}
	
	public static class NotEnoughColorException extends NotEnoughIngredientsException {
		
		public ColorUnit missing;
		
		public NotEnoughColorException(ColorUnit missing) {
			super("exception.ingredient.color");
			this.missing = missing;
		}
		
		@Override
		public String getLocalizedMessage()
		{
			return super.getLocalizedMessage() + " " + missing.getDescription();
		}
		
	}
	
	public static class NotEnoughVolumeExcepion extends NotEnoughIngredientsException {
		
		public BlockIngredients ingredients;
		
		public NotEnoughVolumeExcepion(BlockIngredients ingredients) {
			super("exception.ingredient.volume");
			this.ingredients = ingredients;
		}
		
		@Override
		public String getLocalizedMessage()
		{
			String message = super.getLocalizedMessage() + " (";
			boolean first = true;
			for (BlockIngredient ingredient : ingredients.getIngredients()) {
				if(!first)
					message += ", ";
				else
					first = false;
				message += ingredient.block.getLocalizedName();
			}
			return message + ")";
		}
		
	}
	
	public static class NotEnoughVolumeSpaceException extends NotEnoughIngredientsException {
		
		public NotEnoughVolumeSpaceException() {
			super("exception.ingredient.space.volume");
		}
	}
	
	public static class NotEnoughColorSpaceException extends NotEnoughIngredientsException {
		
		public NotEnoughColorSpaceException() {
			super("exception.ingredient.space.color");
		}
	}
	
	public static class NotEnoughStackException extends NotEnoughIngredientsException {
		
		public ItemStack stack;
		
		public NotEnoughStackException(ItemStack stack) {
			super("exception.ingredient.stack");
			this.stack = stack;
		}
		
		@Override
		public String getLocalizedMessage()
		{
			return super.getLocalizedMessage() + " " + stack.getDisplayName();
		}
		
	}
}
