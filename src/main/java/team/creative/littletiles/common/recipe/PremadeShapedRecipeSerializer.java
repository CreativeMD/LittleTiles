package team.creative.littletiles.common.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.mixin.common.recipe.ShapedRecipeAccessor;
import team.creative.littletiles.mixin.common.recipe.ShapedRecipeSerializerAccessor;

public class PremadeShapedRecipeSerializer implements RecipeSerializer<ShapedRecipe> {
    
    public static final Codec<ItemStack> PREMADE_RESULT = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("structure").forGetter(
        ItemPremadeStructure::getPremadeId)).apply(instance, ItemPremadeStructure::of));
    
    private static final Codec<ShapedRecipe> CODEC = RawShapedRecipe.CODEC.flatXmap((p_300056_) -> {
        String[] astring = ShapedRecipeAccessor.callShrink(p_300056_.pattern);
        int i = astring[0].length();
        int j = astring.length;
        NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);
        Set<String> set = Sets.newHashSet(p_300056_.key.keySet());
        
        for (int k = 0; k < astring.length; ++k) {
            String s = astring[k];
            
            for (int l = 0; l < s.length(); ++l) {
                String s1 = s.substring(l, l + 1);
                Ingredient ingredient = s1.equals(" ") ? Ingredient.EMPTY : p_300056_.key.get(s1);
                if (ingredient == null) {
                    return DataResult.error(() -> {
                        return "Pattern references symbol '" + s1 + "' but it's not defined in the key";
                    });
                }
                
                set.remove(s1);
                nonnulllist.set(l + i * k, ingredient);
            }
        }
        
        if (!set.isEmpty()) {
            return DataResult.error(() -> {
                return "Key defines symbols that aren't used in pattern: " + set;
            });
        } else {
            ShapedRecipe shapedrecipe = new ShapedRecipe(p_300056_.group, p_300056_.category, i, j, nonnulllist, p_300056_.result, p_300056_.showNotification);
            return DataResult.success(shapedrecipe);
        }
    }, (p_299463_) -> {
        throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
    });
    
    static record RawShapedRecipe(String group, CraftingBookCategory category, Map<String, Ingredient> key, List<String> pattern, ItemStack result, boolean showNotification) {
        public static final Codec<RawShapedRecipe> CODEC = RecordCodecBuilder.create((p_298430_) -> {
            return p_298430_.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((p_300105_) -> {
                return p_300105_.group;
            }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((p_301213_) -> {
                return p_301213_.category;
            }), ExtraCodecs.strictUnboundedMap(ShapedRecipeSerializerAccessor.getSINGLE_CHARACTER_STRING_CODEC(), Ingredient.CODEC_NONEMPTY).fieldOf("key").forGetter(
                (p_297983_) -> {
                    return p_297983_.key;
                }), ShapedRecipeSerializerAccessor.getPATTERN_CODEC().fieldOf("pattern").forGetter((p_300956_) -> {
                    return p_300956_.pattern;
                }), PREMADE_RESULT.fieldOf("result").forGetter((p_299535_) -> {
                    return p_299535_.result;
                }), ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter((p_297368_) -> {
                    return p_297368_.showNotification;
                })).apply(p_298430_, RawShapedRecipe::new);
        });
    }
    
    @Override
    public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
        RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
    }
    
    @Override
    public @Nullable ShapedRecipe fromNetwork(FriendlyByteBuf buffer) {
        return RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer);
    }
    
    @Override
    public Codec<ShapedRecipe> codec() {
        return CODEC;
    }
    
}
