package com.creativemd.littletiles.common.util.tooltip;

import net.minecraft.item.ItemStack;

public interface IItemTooltip {
    
    public Object[] tooltipData(ItemStack stack);
    
}
