package team.creative.littletiles.common.gui.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.collection.GuiStackSelector;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiShowItem;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.element.LittleElement.NotBlockException;
import team.creative.littletiles.common.gui.LittleGuiUtils;
import team.creative.littletiles.common.gui.controls.GuiGridConfig;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class GuiChisel extends GuiConfigureTool {
    
    public GuiChisel(ContainerSlotView view) {
        super("chisel", 230, 200, view);
        registerEventChanged(x -> {
            if (x.control.is("shape"))
                onChange();
            else if (x.control.is("picker", "preview"))
                updateLabel();
        });
        flow = GuiFlow.STACK_X;
        valign = VAlign.STRETCH;
        
        registerEventChanged(x -> {
            if (x.control.is("mode")) {
                GuiComboBoxMapped<PlacementMode> modeBox = (GuiComboBoxMapped<PlacementMode>) x.control;
                TextBuilder builder = new TextBuilder();
                if (modeBox.getSelected().canPlaceStructures())
                    builder.text("" + ChatFormatting.BOLD).translate("placement.mode.placestructure").text("" + ChatFormatting.WHITE).newLine();
                builder.translate(modeBox.getSelected().translatableKey() + ".tooltip");
                ((GuiLabel) get("text")).setTitle(builder.build());
                ItemMultiTiles.currentMode = modeBox.getSelected();
            }
        });
    }
    
    @Override
    public void create() {
        GuiParent left = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        add(left);
        
        LittleElement element = ItemLittleChisel.getElement(tool.get());
        Color color = new Color(element.color);
        left.add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        
        GuiParent parent = new GuiParent(GuiFlow.STACK_X).setVAlign(VAlign.CENTER);
        left.add(parent);
        parent.add(new GuiShowItem("item").setDim(60, 60));
        parent.add(new GuiGridConfig("grid", ItemMultiTiles.currentGrid, x -> {
            ItemMultiTiles.currentGrid = x;
            if (ItemLittleChisel.selection != null)
                ItemLittleChisel.selection.convertTo(x);
        }));
        
        GuiStackSelector selector = new GuiStackSelector("preview", getPlayer(), LittleGuiUtils.getCollector(getPlayer()), true);
        selector.setSelectedForce(element.getBlock().getStack());
        left.add(selector);
        GuiComboBoxMapped<LittleShape> box = new GuiComboBoxMapped<>("shape", new TextMapBuilder<LittleShape>()
                .addComponent(ShapeRegistry.placingShapes(), x -> Component.translatable(x.getTranslatableName())));
        box.select(ItemLittleChisel.getShape(tool.get()));
        left.add(box);
        left.add(new GuiScrollY("settings").setDim(20, 60).setExpandable());
        
        GuiParent right = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        add(right);
        
        GuiComboBoxMapped<PlacementMode> modeBox = new GuiComboBoxMapped<>("mode", PlacementMode.map());
        modeBox.select(ItemMultiTiles.currentMode);
        right.add(modeBox);
        right.add(new GuiLabel("text"));
        raiseEvent(new GuiControlChangedEvent(modeBox));
        
        onChange();
        
        updateLabel();
    }
    
    public void onChange() {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        
        scroll.clear();
        for (GuiControl control : box.getSelected(ShapeRegistry.DEFAULT_SHAPE).getCustomSettings(tool.get().getTag(), getGrid()))
            scroll.add(control);
        scroll.reflow();
    }
    
    public void updateLabel() {
        GuiStackSelector selector = (GuiStackSelector) get("preview");
        ItemStack selected = selector.getSelected();
        GuiColorPicker picker = (GuiColorPicker) get("picker");
        
        LittleElement element;
        try {
            element = LittleElement.of(selected, picker.color.toInt());
        } catch (NotBlockException e) {
            element = new LittleElement(ItemLittleChisel.getElement(tool.get()), picker.color.toInt());
        }
        
        get("item", GuiShowItem.class).stack = ItemMultiTiles.of(element);
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
            element = new LittleElement(ItemLittleChisel.getElement(tool.get()), picker.color.toInt());
        }
        
        ItemLittleChisel.setElement(nbt, element);
        ItemLittleChisel.setShape(nbt, shape);
        
        shape.saveCustomSettings(scroll, nbt, getGrid());
        return nbt;
    }
}
