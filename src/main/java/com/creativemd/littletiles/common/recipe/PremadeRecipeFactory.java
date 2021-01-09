package com.creativemd.littletiles.common.recipe;

import java.util.Map;

import com.creativemd.littletiles.LittleTiles;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
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
            shapedCrafting = ((Map<ResourceLocation, IRecipeFactory>) ReflectionHelper.getPrivateValue(CraftingHelper.class, null, "recipes"))
                .get(new ResourceLocation("minecraft", "crafting_shaped"));
        
        ShapedRecipes recipe = (ShapedRecipes) shapedCrafting.parse(context, json);
        
        JsonObject result = JsonUtils.getJsonObject(json, "result");
        
        ItemStack stack = new ItemStack(LittleTiles.premade);
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound structureNBT = new NBTTagCompound();
        if (!result.has("structure"))
            throw new RuntimeException("Missing structure type");
        structureNBT.setString("id", result.get("structure").getAsString());
        nbt.setTag("structure", structureNBT);
        stack.setTagCompound(nbt);
        
        return new ShapedRecipes(recipe.getGroup(), recipe.getRecipeWidth(), recipe.getRecipeHeight(), recipe.getIngredients(), stack);
    }
    
}
