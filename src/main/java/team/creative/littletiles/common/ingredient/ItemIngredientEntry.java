package team.creative.littletiles.common.ingredient;

import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;

public class ItemIngredientEntry {
    
    public final CreativeIngredient ingredient;
    public int count;
    
    public ItemIngredientEntry(CreativeIngredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }
    
    @Override
    public String toString() {
        return ingredient.toString();
    }
    
    public boolean is(ItemIngredientEntry entry) {
        return this.ingredient.is(entry.ingredient);
    }
    
    public boolean is(ItemStack stack) {
        return this.ingredient.is(ingredient);
    }
    
    public ItemIngredientEntry copy() {
        return new ItemIngredientEntry(ingredient.copy(), count);
    }
    
    public boolean isEmpty() {
        return count <= 0;
    }
    
    public void scale(int count) {
        this.count *= count;
    }
    
    public void scaleAdvanced(double scale) {
        this.count = (int) Math.ceil(this.count * scale);
    }
    
}
