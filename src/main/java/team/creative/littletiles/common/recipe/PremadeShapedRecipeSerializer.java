package team.creative.littletiles.common.recipe;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class PremadeShapedRecipeSerializer implements RecipeSerializer<ShapedRecipe> {
    
    @Override
    public ShapedRecipe fromJson(ResourceLocation recipeLoc, JsonObject recipeJson, IContext context) {
        ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromJson(recipeLoc, recipeJson, context);
        
        return new ShapedRecipe(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), ItemPremadeStructure.of(
            GsonHelper.getAsString(GsonHelper.getAsJsonObject(recipeJson, "result"), "structure")), recipe.showNotification());
    }
    
    @Override
    public ShapedRecipe fromJson(ResourceLocation location, JsonObject object) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public @Nullable ShapedRecipe fromNetwork(ResourceLocation location, FriendlyByteBuf buffer) {
        return RecipeSerializer.SHAPED_RECIPE.fromNetwork(location, buffer);
    }
    
    @Override
    public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
        RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
    }
    
}
