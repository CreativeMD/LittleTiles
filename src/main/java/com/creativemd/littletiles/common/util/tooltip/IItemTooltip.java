package com.creativemd.littletiles.common.util.tooltip;

import net.minecraft.world.item.ItemStack;

public interface IItemTooltip {
    
    public Object[] tooltipData(ItemStack stack);
    
}
