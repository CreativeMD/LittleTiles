package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.api.tool.ILittleEditor;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;

public class GuiHammer extends GuiConfigure {
    
    public GuiHammer(ItemStack stack) {
        super("hammer", 140, 150, stack);
        registerEventChanged(x -> {
            if (x.control.is("shape"))
                onChange();
        });
    }
    
    public LittleGrid getGrid() {
        return ((ILittleEditor) stack.getItem()).getPositionGrid(stack);
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
                .addComponent(ShapeRegistry.notTileShapes(), x -> new TranslatableComponent(x.getTranslatableName())));
        box.select(ItemLittleHammer.getShape(stack));
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
        for (GuiControl control : shape.getCustomSettings(stack.getTag(), getGrid()))
            scroll.add(control);
    }
    
}
