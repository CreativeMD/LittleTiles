package team.creative.littletiles.common.gui.controls.filter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.gui.LittleGuiUtils;

public class GuiElementFilterBlock extends GuiElementFilter {
    
    protected GuiStackSelector selector;
    
    public GuiElementFilterBlock(Player player, Block block) {
        add(selector = new GuiStackSelector("filter", player, LittleGuiUtils.getCollector(player), true));
        if (block != null)
            selector.setSelectedForce(new ItemStack(block));
        selector.setExpandableX();
    }
    
    @Override
    public BiFilter<IParentCollection, LittleTile> get() {
        Block filterBlock = Block.byItem(selector.getSelected().getItem());
        if (filterBlock != null && !(filterBlock instanceof AirBlock) && LittleAction.isBlockValid(filterBlock.defaultBlockState()))
            return TileFilters.block(filterBlock);
        return null;
    }
    
}
