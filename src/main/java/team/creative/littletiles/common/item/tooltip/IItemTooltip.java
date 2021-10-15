package team.creative.littletiles.common.item.tooltip;

import net.minecraft.world.item.ItemStack;

public interface IItemTooltip {
    
    public Object[] tooltipData(ItemStack stack);
    
}
