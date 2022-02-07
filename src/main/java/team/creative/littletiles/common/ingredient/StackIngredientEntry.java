package team.creative.littletiles.common.ingredient;

import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.inventory.InventoryUtils;

public class StackIngredientEntry {
    
    public final ItemStack stack;
    public int count;
    
    public StackIngredientEntry(ItemStack stack, int count) {
        this.stack = stack;
        this.count = count;
    }
    
    @Override
    public int hashCode() {
        return stack.getItem().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StackIngredientEntry)
            return InventoryUtils.isItemStackEqual(stack, ((StackIngredientEntry) obj).stack);
        return false;
    }
    
    @Override
    public String toString() {
        return stack.toString();
    }
    
    public StackIngredientEntry copy() {
        return new StackIngredientEntry(stack, count);
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
