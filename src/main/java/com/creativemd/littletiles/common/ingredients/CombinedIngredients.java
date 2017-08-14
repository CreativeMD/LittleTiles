package com.creativemd.littletiles.common.ingredients;

import java.util.List;

import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;

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
	
	public void addPreview(List<LittleTilePreview> previews)
	{
		for (LittleTilePreview preview : previews) {
			addPreview(preview);
		}
	}
	
	public void addPreview(LittleTilePreview preview)
	{
		if(preview.canBeConvertedToBlockEntry())
		{
			block.addIngredient(preview.getBlockIngredient());
			color.addColorUnit(ColorUnit.getRequiredColors(preview));
		}
	}
	
	public void addPreview(LittleTilePreview preview, double volume)
	{
		if(preview.canBeConvertedToBlockEntry())
		{
			BlockIngredient ingredient = preview.getBlockIngredient();
			ingredient.value = volume;
			block.addIngredient(ingredient);
			color.addColorUnit(ColorUnit.getRequiredColors(preview, volume));
		}
	}
	
}
