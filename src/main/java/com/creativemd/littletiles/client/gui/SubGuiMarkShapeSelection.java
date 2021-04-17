package com.creativemd.littletiles.client.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;

import net.minecraft.util.text.translation.I18n;

public class SubGuiMarkShapeSelection extends SubGui {
    
    public ShapeSelection mode;
    
    public SubGuiMarkShapeSelection(ShapeSelection mode) {
        super();
        this.mode = mode;
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiCheckBox("resolution", I18n.translateToLocal("markmode.gui.allowlowresolution"), 0, 0, mode.allowLowResolution));
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
        GuiCheckBox box = (GuiCheckBox) get("resolution");
        mode.allowLowResolution = box.value;
    }
}
