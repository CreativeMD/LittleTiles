package team.creative.littletiles.common.item;

import net.minecraft.world.item.Item;
import team.creative.littletiles.LittleTiles;

public class ItemLittleUtilityKnife extends Item {
    
    public ItemLittleUtilityKnife() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
}
