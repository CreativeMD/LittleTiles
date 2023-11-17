package team.creative.littletiles.common.ingredient;

import net.minecraft.world.item.Item;
import team.creative.littletiles.LittleTiles;

public class LittleBagInventory extends LittleIngredients {
    
    public LittleBagInventory() {
        content[LittleIngredient.indexOf(BlockIngredient.class)] = new BlockIngredient().setLimits(LittleTiles.CONFIG.general.bag.inventorySize, Item.MAX_STACK_SIZE);
        content[LittleIngredient.indexOf(ColorIngredient.class)] = new ColorIngredient().setLimit(LittleTiles.CONFIG.general.bag.colorStorage);
    }
    
    @Override
    public LittleBagInventory copy() {
        LittleBagInventory bag = new LittleBagInventory();
        bag.assignContent(this);
        return bag;
    }
    
    @Override
    protected boolean removeEmptyIngredients() {
        return false;
    }
    
    @Override
    protected boolean canAddNewIngredients() {
        return false;
    }
    
}
