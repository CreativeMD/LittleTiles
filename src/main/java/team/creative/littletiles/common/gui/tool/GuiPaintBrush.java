package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class GuiPaintBrush extends GuiConfigureTool {
    
    public GuiPaintBrush(ContainerSlotView view) {
        super("paintbrush", 140, 173, view);
        registerEventChanged(x -> {
            if (x.control.is("shape"))
                onChange();
        });
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
    }
    
    @Override
    public void create() {
        Color color = new Color(ItemLittlePaintBrush.getColor(tool.get()));
        add(new GuiColorPicker("picker", color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
        GuiComboBoxMapped<LittleShape> box = new GuiComboBoxMapped<>("shape", new TextMapBuilder<LittleShape>()
                .addComponent(ShapeRegistry.REGISTRY.values(), x -> new TranslatableComponent(x.getTranslatableName())));
        add(box);
        box.select(ItemLittlePaintBrush.getShape(tool.get()));
        add(new GuiScrollY("settings").setExpandable());
        onChange();
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        LittleShape shape = box.getSelected(ShapeRegistry.DEFAULT_SHAPE);
        
        nbt.putString("shape", shape == null ? "tile" : shape.getKey());
        GuiColorPicker picker = (GuiColorPicker) get("picker");
        nbt.putInt("color", picker.color.toInt());
        if (shape != null)
            shape.saveCustomSettings(scroll, nbt, getGrid());
        return nbt;
    }
    
    public void onChange() {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        
        scroll.clear();
        for (GuiControl control : box.getSelected(ShapeRegistry.TILE_SHAPE).getCustomSettings(tool.get().getTag(), getGrid()))
            scroll.add(control);
    }
    
}
