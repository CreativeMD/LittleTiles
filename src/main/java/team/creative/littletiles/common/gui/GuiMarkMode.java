package team.creative.littletiles.common.gui;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.gui.configure.GuiConfigure;
import team.creative.littletiles.common.placement.mark.MarkMode;

public class GuiMarkMode extends GuiConfigure {
    
    public MarkMode mode;
    
    public GuiMarkMode(MarkMode mode) {
        super("mark_mode", ContainerSlotView.EMPTY);
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
