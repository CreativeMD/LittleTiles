package team.creative.littletiles.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class PremadeShapelessRecipeSerializer implements RecipeSerializer<ShapelessRecipe> {
    
    public static final MapCodec<ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(x -> x
            .getGroup()), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(x -> x.category()), ItemStack.STRICT_CODEC.fieldOf("result")
                    .forGetter(x -> x.getResultItem(null)), Codec.STRING.fieldOf("structure").forGetter(x -> ""), Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                            .flatXmap(x -> {
                                Ingredient[] aingredient = x.toArray(Ingredient[]::new); // Neo skip the empty check and immediately create the array.
                                if (aingredient.length == 0) {
                                    return DataResult.error(() -> "No ingredients for shapeless recipe");
                                } else {
                                    return aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth() ? DataResult.error(
                                        () -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern
                                                .getMaxWidth())) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                }
                            }, DataResult::success).forGetter(x -> x.getIngredients())).apply(instance, (group, category, result, structure,
                                    ingredients) -> new ShapelessRecipe(group, category, ItemPremadeStructure.of(result, structure), ingredients)));
    
    @Override
    public MapCodec<ShapelessRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapelessRecipe> streamCodec() {
        return ShapelessRecipe.Serializer.STREAM_CODEC;
    }
    
}
