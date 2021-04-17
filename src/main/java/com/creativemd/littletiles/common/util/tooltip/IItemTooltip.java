package com.creativemd.littletiles.common.util.tooltip;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IItemTooltip {
    
    public Object[] tooltipData(ItemStack stack);
    
}
