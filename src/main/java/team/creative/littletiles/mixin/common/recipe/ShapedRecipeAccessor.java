package team.creative.littletiles.mixin.common.recipe;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {
    
    @Invoker
    public static String[] callShrink(List<String> data) {
        throw new UnsupportedOperationException();
    }
    
}
