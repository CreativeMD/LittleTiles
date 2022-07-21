package team.creative.littletiles.common.api.ingredient;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

public interface ILittleIngredientSupplier {
    
    /** can be used to add itemstacks to the collection
     * 
     * @param collected
     *            multiple categories represented by a string (which is translatable)
     * @param stack
     *            the ingredient supplier itself
     * @param player
     *            the player if there is one */
    public void collect(HashMapList<String, ItemStack> collected, ItemStack stack, @Nullable PlayerEntity player);
    
    /** Requests ingredients which are not available directly
     * 
     * @param stack
     *            the ingredient supplier itself
     * @param ingredients
     *            the ingredients that are requested
     * @param overflow
     *            the overflow ingredients
     * @param inventory
     *            the player that is requesting the material */
    public void requestIngredients(ItemStack stack, LittleIngredients ingredients, LittleIngredients overflow, LittleInventory inventory);
}
