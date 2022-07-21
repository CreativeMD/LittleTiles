package team.creative.littletiles.common.api.ingredient;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

/** must be implemented by an item * */
public interface ILittleIngredientInventory {
    
    public default boolean shouldBeMerged() {
        return false;
    }
    
    public LittleIngredients getInventory(ItemStack stack);
    
    public void setInventory(ItemStack stack, LittleIngredients ingredients, @Nullable LittleInventory inventory);
    
}
