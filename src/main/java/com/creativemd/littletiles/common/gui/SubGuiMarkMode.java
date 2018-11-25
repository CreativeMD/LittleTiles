package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.littletiles.common.utils.placing.MarkMode;

import net.minecraft.util.text.translation.I18n;

public class SubGuiMarkMode extends SubGui {
	
	public MarkMode mode;
	
	public SubGuiMarkMode(MarkMode mode) {
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
