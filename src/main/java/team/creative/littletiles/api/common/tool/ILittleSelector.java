package team.creative.littletiles.api.common.tool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.item.component.SelectionComponent;
import team.creative.littletiles.common.placement.setting.PlacementPlayerSetting;

public interface ILittleSelector {
    
    public boolean hasSelection(ItemStack stack);
    
    public default SelectionComponent getSelection(ItemStack stack) {
        return SelectionComponent.getOrDefault(stack);
    }
    
    public default LittleGrid getSelectorGrid(Player player, ItemStack stack) {
        return PlacementPlayerSetting.grid(player);
    }
    
}
