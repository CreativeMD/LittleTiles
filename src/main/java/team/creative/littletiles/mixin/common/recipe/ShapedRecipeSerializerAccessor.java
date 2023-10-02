package team.creative.littletiles.mixin.common.recipe;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.Codec;

import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.Serializer.class)
public interface ShapedRecipeSerializerAccessor {
    
    @Accessor
    public static Codec<List<String>> getPATTERN_CODEC() {
        throw new UnsupportedOperationException();
    }
    
    @Accessor
    public static Codec<String> getSINGLE_CHARACTER_STRING_CODEC() {
        throw new UnsupportedOperationException();
    }
    
}
