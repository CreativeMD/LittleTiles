package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.gui.ContainerControl;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.container.SlotControl;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.littletiles.common.structure.LittleStorage;

public class SubGuiStorage extends SubGui{
	
	public LittleStorage storage;
	
	public SubGuiStorage(LittleStorage storage) {
		super(250, 250);
		this.storage = storage;
	}
	
	@Override
	public void addContainerControls()
	{
		GuiScrollBox box = new GuiScrollBox("box", 0, 0, 245, 150);
		controls.add(box);
		for (int i = 0; i < container.controls.size(); i++) {
			ContainerControl control = container.controls.get(i);
			control.onOpened();
			
			if(control instanceof SlotControl && ((SlotControl) control).slot.inventory == storage.inventory)
				box.addControl(control.getGuiControl());
			else
				controls.add(control.getGuiControl());
		}
	}

	@Override
	public void createControls() {
		
	}

}
