package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
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
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        LittleShape shape = box.getSelected();
        nbt.putString("shape", shape.getKey());
        shape.saveCustomSettings(scroll, nbt, getGrid());
        return nbt;
    }
    
    @Override
    public void create() {
        GuiComboBoxMapped<LittleShape> box = new GuiComboBoxMapped<>("shape", new TextMapBuilder<LittleShape>()
                .addComponent(ShapeRegistry.notTileShapes(), x -> Component.translatable(x.getTranslatableName())));
        box.select(ItemLittleHammer.getShape(tool.get()));
        GuiScrollY scroll = new GuiScrollY("settings").setExpandable();
        add(box);
        add(scroll);
        onChange();
    }
    
    public void onChange() {
        GuiComboBoxMapped<LittleShape> box = (GuiComboBoxMapped<LittleShape>) get("shape");
        GuiScrollY scroll = (GuiScrollY) get("settings");
        
        LittleShape shape = box.getSelected();
        scroll.clear();
        for (GuiControl control : shape.getCustomSettings(tool.get().getTag(), getGrid()))
            scroll.add(control);
    }
    
}
