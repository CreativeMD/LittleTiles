package com.creativemd.littletiles.common.ingredients;

import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;

public class CombinedIngredients {
	
	public final ColorUnit color;
	public final BlockIngredients block;
	
	public CombinedIngredients() {
		this(new ColorUnit(), new BlockIngredients());
	}
	
	public CombinedIngredients(ColorUnit color, BlockIngredients block) {
		this.color = color;
		this.block = block;
	}
	
}
