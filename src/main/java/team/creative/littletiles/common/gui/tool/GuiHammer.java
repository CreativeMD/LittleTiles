package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.filter.TileFilters.TileBlockFilter;
import team.creative.littletiles.common.filter.TileFilters.TileBlockStateFilter;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.controls.GuiGridConfig;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class GuiHammer extends GuiConfigureTool {
    
    public GuiHammer(ContainerSlotView view) {
        super("hammer", 140, 150, view);
        registerEventChanged(x -> {
            if (x.control.is("shape"))
                onChange();
        });
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        LittleShape shape = box.getSelected();
        nbt.putString("shape", shape.getKey());
        shape.saveCustomSettings(scroll, nbt, getGrid());
        
        GuiStackSelector filter = get("filter", GuiStackSelector.class);
        ItemLittleHammer.setFilter(!((GuiCheckBox) get("any")).value, TileFilters.block(Block.byItem(filter.getSelected().getItem())));
        
        return nbt;
    }
    
    @Override
    public void create() {
        if (!isClient())
            return;
        
        GuiComboBoxMapped<LittleShape> box = new GuiComboBoxMapped<>("shape", new TextMapBuilder<LittleShape>().addComponent(ShapeRegistry.notTileShapes(), x -> Component
                .translatable(x.getTranslatableName())));
        box.select(ItemLittleHammer.getShape(tool.get()));
        GuiScrollY scroll = new GuiScrollY("settings").setExpandable();
        add(box);
        add(scroll);
        
        add(new GuiGridConfig("grid", LittleTilesClient.grid(), x -> {
            LittleTilesClient.grid(x);
            if (ItemLittleHammer.selection != null)
                ItemLittleHammer.selection.convertTo(x);
        }));
        
        BiFilter<IParentCollection, LittleTile> selector = ItemLittleHammer.getFilter();
        boolean activeFilter = ItemLittleHammer.isFiltered();
        add(new GuiCheckBox("any", selector == null || !activeFilter).setTranslate("gui.any"));
        
        GuiStackSelector guiSelector = new GuiStackSelector("filter", getPlayer(), LittleGuiUtils.getCollector(getPlayer()));
        if (selector instanceof TileBlockStateFilter stateFilter)
            guiSelector.setSelectedForce(new ItemStack(stateFilter.state.getBlock()));
        else if (selector instanceof TileBlockFilter blockFilter)
            guiSelector.setSelectedForce(new ItemStack(blockFilter.block));
        
        add(guiSelector);
        onChange();
    }
    
    public void onChange() {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        
        LittleShape shape = box.getSelected();
        scroll.clear();
        for (GuiControl control : shape.getCustomSettings(tool.get().getTag(), getGrid()))
            scroll.add(control);
        
        reflow();
    }
    
}
