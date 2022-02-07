package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.controls.simple.GuiShowItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.element.LittleElement.NotBlockException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class GuiChisel extends GuiConfigure {
    
    public GuiChisel(ItemStack stack) {
        super("chisel", 140, 180, stack);
        this.stack = stack;
        registerEventChanged(x -> {
            if (x.control.is("shape"))
                onChange();
            else if (x.control.is("picker", "preview"))
                updateLabel();
        });
        flow = GuiFlow.STACK_Y;
    }
    
    public LittleGrid getGrid() {
        return ((ILittlePlacer) stack.getItem()).getPositionGrid(stack);
    }
    
    @Override
    public void create() {
        LittleElement element = ItemLittleChisel.getElement(stack);
        Color color = new Color(element.color);
        add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        GuiShowItem show = new GuiShowItem("preview", 60, 60);
        add(show);
        
        GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(element.getBlock().getStack());
        add(selector);
        GuiComboBoxMapped<LittleShape> box = new GuiComboBoxMapped<>("shape", new TextMapBuilder<LittleShape>()
                .addComponent(ShapeRegistry.placingShapes(), x -> new TranslatableComponent(x.getTranslatableName())));
        box.select(ItemLittleChisel.getShape(stack));
        add(box);
        add(new GuiScrollY("settings").setExpandable());
        onChange();
        
        updateLabel();
    }
    
    public void onChange() {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        
        scroll.clear();
        for (GuiControl control : box.getSelected(ShapeRegistry.DEFAULT_SHAPE).getCustomSettings(stack.getTag(), getGrid()))
            scroll.add(control);
    }
    
    public void updateLabel() {
        GuiStackSelector selector = (GuiStackSelector) get("preview");
        ItemStack selected = selector.getSelected();
        GuiColorPicker picker = (GuiColorPicker) get("picker");
        
        LittleElement element;
        try {
            element = LittleElement.of(selected, picker.color.toInt());
        } catch (NotBlockException e) {
            element = new LittleElement(ItemLittleChisel.getElement(stack), picker.color.toInt());
        }
        
        ((GuiShowItem) get("preview")).stack = ItemMultiTiles.of(element);
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        LittleShape shape = box.getSelected(ShapeRegistry.DEFAULT_SHAPE);
        
        GuiColorPicker picker = (GuiColorPicker) get("picker");
        
        GuiStackSelector selector = (GuiStackSelector) get("preview");
        ItemStack selected = selector.getSelected();
        LittleElement element;
        try {
            element = LittleElement.of(selected, picker.color.toInt());
        } catch (NotBlockException e) {
            element = new LittleElement(ItemLittleChisel.getElement(stack), picker.color.toInt());
        }
        
        ItemLittleChisel.setElement(nbt, element);
        ItemLittleChisel.setShape(nbt, shape);
        
        shape.saveCustomSettings(scroll, nbt, getGrid());
        return nbt;
    }
}
