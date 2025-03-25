package team.creative.littletiles.common.gui.control.filter;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRules;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.item.component.TileFilterComponent;

public class GuiFilterConfiguration extends GuiParent {
    
    protected final ContainerSlotView view;
    protected final GuiElementFilter filterElement;
    
    public GuiFilterConfiguration(Player player, String name, ContainerSlotView view) {
        super(name);
        this.flow = GuiFlow.STACK_Y;
        this.view = view;
        
        var filter = TileFilterComponent.getOrCreate(view.get());
        BiFilter<IParentCollection, LittleTile> selector = filter.getFilter();
        boolean activeFilter = filter.hasFilter();
        add(new GuiLabel("filter_label").setTranslate("gui.filter"));
        add(new GuiCheckBox("any", selector == null || !activeFilter).setTranslate("gui.any"));
        add(filterElement = (GuiElementFilter) GuiElementFilter.ofGroup(player, selector).setExpandableX().setDim(new GuiSizeRules().prefHeight(100)));
    }
    
    public TileFilterComponent save() {
        return TileFilterComponent.of(!get("any", GuiCheckBox.class).value, filterElement.get());
    }
}
