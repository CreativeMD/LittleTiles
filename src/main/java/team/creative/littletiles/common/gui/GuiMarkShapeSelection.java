package team.creative.littletiles.common.gui;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.gui.tool.GuiConfigure;
import team.creative.littletiles.common.placement.shape.ShapeSelection;

public class GuiMarkShapeSelection extends GuiConfigure {
    
    public ShapeSelection mode;
    
    public GuiMarkShapeSelection(ShapeSelection mode) {
        super("mark_mode_shape", ContainerSlotView.EMPTY);
        this.mode = mode;
    }
    
    @Override
    public void create() {
        add(new GuiCheckBox("resolution", mode.allowLowResolution).setTranslate("markmode.gui.allowlowresolution"));
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        GuiCheckBox box = (GuiCheckBox) get("resolution");
        mode.allowLowResolution = box.value;
        return null;
    }
}