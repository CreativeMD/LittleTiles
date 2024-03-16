package team.creative.littletiles.common.gui.controls.filter;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.filter.BiFilter.BiFilterAnd;
import team.creative.creativecore.common.util.filter.BiFilter.BiFilterNot;
import team.creative.creativecore.common.util.filter.BiFilter.BiFilterOr;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters.TileBlockFilter;
import team.creative.littletiles.common.filter.TileFilters.TileColorFilter;
import team.creative.littletiles.common.filter.TileFilters.TileTagFilter;
import team.creative.littletiles.common.gui.controls.filter.GuiElementFilterGroup.GuiElementFilterOperator;

public abstract class GuiElementFilter extends GuiParent {
    
    public static GuiElementFilter ofGroup(Player player, BiFilter<IParentCollection, LittleTile> filter) {
        if (filter instanceof BiFilterNot || filter instanceof BiFilterAnd || filter instanceof BiFilterOr<IParentCollection, LittleTile>)
            return of(player, filter);
        if (filter == null)
            return new GuiElementFilterGroup(player, GuiElementFilterOperator.OR);
        return new GuiElementFilterGroup(player, GuiElementFilterOperator.OR, filter);
    }
    
    public static GuiElementFilter of(Player player, BiFilter<IParentCollection, LittleTile> filter) {
        if (filter instanceof BiFilterNot<IParentCollection, LittleTile> not) {
            if (not.filter() instanceof BiFilterAnd<IParentCollection, LittleTile> and)
                return new GuiElementFilterGroup(player, GuiElementFilterOperator.NOT_AND, and.filters());
            if (not.filter() instanceof BiFilterOr<IParentCollection, LittleTile> or)
                return new GuiElementFilterGroup(player, GuiElementFilterOperator.NOT_OR, or.filters());
            if (not.filter() instanceof BiFilterNot<IParentCollection, LittleTile> not2)
                return of(player, not2.filter());
            return new GuiElementFilterGroup(player, GuiElementFilterOperator.NOT_OR, not.filter());
        }
        if (filter instanceof BiFilterAnd<IParentCollection, LittleTile> and)
            return new GuiElementFilterGroup(player, GuiElementFilterOperator.AND, and.filters());
        if (filter instanceof BiFilterOr<IParentCollection, LittleTile> or)
            return new GuiElementFilterGroup(player, GuiElementFilterOperator.OR, or.filters());
        if (filter instanceof TileBlockFilter block)
            return new GuiElementFilterBlock(player, block.block);
        if (filter instanceof TileColorFilter color)
            return new GuiElementFilterColor(color.color);
        if (filter instanceof TileTagFilter tag)
            return new GuiElementFilterTag(tag.tag);
        return new GuiElementFilterGroup(player, GuiElementFilterOperator.OR);
    }
    
    public GuiElementFilter() {
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
    }
    
    public abstract BiFilter<IParentCollection, LittleTile> get();
}
