package team.creative.littletiles.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class PremadeShapedRecipeSerializer implements RecipeSerializer<ShapedRecipe> {
    
    public static final MapCodec<ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(x -> x
            .getGroup()), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(x -> x.category()), ShapedRecipePattern.MAP_CODEC.forGetter(
                x -> x.pattern), Codec.STRING.fieldOf("structure").forGetter(x -> ""), Codec.BOOL.optionalFieldOf("show_notification", Boolean.valueOf(true)).forGetter(x -> x
                        .showNotification())).apply(instance, (a, b, c, structure, e) -> new ShapedRecipe(a, b, c, ItemPremadeStructure.of(structure), e)));
    
    @Override
    public MapCodec<ShapedRecipe> codec() {
        return CODEC;
    }
    
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapedRecipe> streamCodec() {
        return ShapedRecipe.Serializer.STREAM_CODEC;
    }
    
}
