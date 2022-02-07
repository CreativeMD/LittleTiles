package team.creative.littletiles.common.gui.configure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.filter.TileFilters.TileBlockFilter;
import team.creative.littletiles.common.filter.TileFilters.TileBlockStateFilter;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;

public abstract class GuiGridSelector extends GuiConfigure {
    
    public LittleGrid grid;
    public boolean activeFilter;
    public BiFilter<IParentCollection, LittleTile> selector;
    
    public GuiGridSelector(ContainerSlotView view, LittleGrid grid, boolean activeFilter, BiFilter<IParentCollection, LittleTile> selector) {
        super("grid-selector", 200, 140, view);
        this.grid = grid;
        this.activeFilter = activeFilter;
        this.selector = selector;
        registerEventChanged(x -> {
            if (x.control.is("meta", "filter"))
                ((GuiCheckBox) get("any")).value = false;
        });
    }
    
    public abstract CompoundTag saveConfiguration(CompoundTag nbt, LittleGrid grid, boolean activeFilter, BiFilter<IParentCollection, LittleTile> filter);
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        GuiComboBoxMapped<LittleGrid> contextBox = (GuiComboBoxMapped<LittleGrid>) get("grid");
        grid = contextBox.getSelected(LittleGrid.defaultGrid());
        
        activeFilter = !((GuiCheckBox) get("any")).value;
        GuiStackSelector filter = (GuiStackSelector) get("filter");
        selector = TileFilters.block(Block.byItem(filter.getSelected().getItem()));
        
        return saveConfiguration(nbt, grid, activeFilter, selector);
    }
    
    @Override
    public void create() {
        GuiComboBoxMapped<LittleGrid> gridBox = new GuiComboBoxMapped<>("grid", LittleGrid.mapBuilder());
        gridBox.select(grid);
        add(gridBox);
        
        add(new GuiCheckBox("any", selector == null || !activeFilter).setTranslate("gui.any"));
        
        GuiStackSelector guiSelector = new GuiStackSelector("filter", getPlayer(), LittleGuiUtils.getCollector(getPlayer()));
        if (selector instanceof TileBlockStateFilter stateFilter)
            guiSelector.setSelectedForce(new ItemStack(stateFilter.state.getBlock()));
        else if (selector instanceof TileBlockFilter blockFilter)
            guiSelector.setSelectedForce(new ItemStack(blockFilter.block));
        add(guiSelector);
    }
    
}
