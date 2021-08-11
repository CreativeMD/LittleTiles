package team.creative.littletiles.common.item;

import com.creativemd.littletiles.LittleTiles;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemLittleRubberMallet extends Item {
    
    public ItemLittleRubberMallet() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        
    }
    
}
