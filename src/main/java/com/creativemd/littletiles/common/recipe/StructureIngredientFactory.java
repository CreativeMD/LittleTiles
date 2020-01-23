package com.creativemd.littletiles.common.recipe;

import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class StructureIngredientFactory implements IIngredientFactory {
	
	@Override
	public Ingredient parse(JsonContext context, JsonObject json) {
		if (json.has("structure")) {
			ItemStack stack = LittleStructurePremade.getPremadeStack(json.get("structure").getAsString());
			if (stack == null)
				throw new JsonSyntaxException("Unkown structure type '" + json.get("structure").getAsString() + "'!");
			
			return Ingredient.fromStacks(stack.copy());
		}
		throw new JsonSyntaxException("Missing 'structure' type!");
	}
	
}
