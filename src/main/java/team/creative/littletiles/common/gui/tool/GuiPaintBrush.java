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
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.filter.TileFilters;
import team.creative.littletiles.common.filter.TileFilters.TileBlockFilter;
import team.creative.littletiles.common.filter.TileFilters.TileBlockStateFilter;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.controls.GuiGridConfig;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class GuiPaintBrush extends GuiConfigureTool {
    
    public GuiPaintBrush(ContainerSlotView view) {
        super("paint_brush", 140, 200, view);
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
        
        GuiColorPicker picker = get("picker", GuiColorPicker.class);
        nbt.putInt("color", picker.color.toInt());
        
        GuiStackSelector filter = get("filter", GuiStackSelector.class);
        ItemLittleHammer.setFilter(!((GuiCheckBox) get("any")).value, TileFilters.block(Block.byItem(filter.getSelected().getItem())));
        
        return nbt;
    }
    
    @Override
    public void create() {
        Color color = new Color(ItemLittlePaintBrush.getColor(tool.get()));
        add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        GuiComboBoxMapped<LittleShape> box = new GuiComboBoxMapped<>("shape", new TextMapBuilder<LittleShape>()
                .addComponent(ShapeRegistry.REGISTRY.values(), x -> Component.translatable(x.getTranslatableName())));
        box.select(ItemLittleHammer.getShape(tool.get()));
        GuiScrollY scroll = new GuiScrollY("settings").setExpandable();
        add(box);
        add(scroll);
        
        add(new GuiGridConfig("grid", ItemMultiTiles.currentGrid, x -> {
            ItemMultiTiles.currentGrid = x;
            if (ItemLittlePaintBrush.selection != null)
                ItemLittlePaintBrush.selection.convertTo(x);
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
        GuiScrollY scroll = get("settings", GuiScrollY.class);
        
        LittleShape shape = box.getSelected(ShapeRegistry.TILE_SHAPE);
        scroll.clear();
        for (GuiControl control : shape.getCustomSettings(tool.get().getTag(), getGrid()))
            scroll.add(control);
        
        reflow();
    }
    
}
