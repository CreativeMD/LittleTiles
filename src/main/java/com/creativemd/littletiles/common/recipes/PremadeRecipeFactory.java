package com.creativemd.littletiles.common.recipes;

import java.util.Map;

import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class PremadeRecipeFactory implements IRecipeFactory {
	
	private IRecipeFactory shapedCrafting;
	
	@Override
	public IRecipe parse(JsonContext context, JsonObject json) {
		
		if (shapedCrafting == null)
			shapedCrafting = ((Map<ResourceLocation, IRecipeFactory>) ReflectionHelper.getPrivateValue(CraftingHelper.class, null, "recipes")).get(new ResourceLocation("minecraft", "crafting_shaped"));
		
		ShapedRecipes recipe = (ShapedRecipes) shapedCrafting.parse(context, json);
		
		JsonObject result = JsonUtils.getJsonObject(json, "result");
		ItemStack stack = LittleStructurePremade.getPremadeStack(result.get("structure").getAsString());
		if (stack == null)
			throw new JsonSyntaxException("Unkown structure type '" + result.get("structure").getAsString() + "'!");
		
		return new ShapedRecipes(recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeWidth(), recipe.getIngredients(), stack);
	}
	
}
