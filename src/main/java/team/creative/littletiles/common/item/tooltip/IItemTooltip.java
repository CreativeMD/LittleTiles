package team.creative.littletiles.common.item.tooltip;

import net.minecraft.world.item.ItemStack;

public interface IItemTooltip {
    
    public default String tooltipTranslateKey(ItemStack stack, String defaultKey) {
        return defaultKey;
    }
    
    public Object[] tooltipData(ItemStack stack);
    
}
