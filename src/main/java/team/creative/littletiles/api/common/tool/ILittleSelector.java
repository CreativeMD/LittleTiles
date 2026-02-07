package team.creative.littletiles.api.common.tool;

import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.item.component.SelectionComponent;

public interface ILittleSelector {
    
    public boolean hasSelection(ItemStack stack);
    
    public default SelectionComponent getSelection(ItemStack stack) {
        return SelectionComponent.getOrDefault(stack);
    }
    
}
