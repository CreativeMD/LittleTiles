package com.creativemd.littletiles.common.ingredients;

import java.util.List;

import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

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
	
}
