package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.gui.ContainerControl;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.container.SlotControl;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.littletiles.common.container.SubContainerStorage.StorageSize;
import com.creativemd.littletiles.common.structure.LittleStorage;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiStorage extends SubGui {
	
	public LittleStorage storage;
	public final StorageSize size;
	
	public SubGuiStorage(LittleStorage storage) {
		super(250, 250);
		this.size = StorageSize.getSizeFromInventory(storage.inventory);
		this.storage = storage;
		setDimension(size.width, size.height);
	}
	
	@Override
	public void addContainerControls() {
		if (!size.scrollbox) {
			super.addContainerControls();
			return;
		}
		
		GuiScrollBox box = new GuiScrollBox("box", 0, 0, 244, 150);
		controls.add(box);
		for (int i = 0; i < container.controls.size(); i++) {
			ContainerControl control = container.controls.get(i);
			control.onOpened();
			
			if (control instanceof SlotControl && ((SlotControl) control).slot.inventory == storage.inventory) {
				((SlotControl) control).slot.xPos -= 4;
				box.addControl(control.getGuiControl());
			} else
				controls.add(control.getGuiControl());
		}
	}
	
	@Override
	public void createControls() {
		int x = this.size.playerOffsetX + 170;
		int y = this.size.playerOffsetY;
		if (size == StorageSize.SMALL) {
			x = size.playerOffsetX;
			y = size.playerOffsetY - 23;
			
		}
		controls.add(new GuiButton("sort", x, y) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("sort", true);
				sendPacketToServer(nbt);
			}
		});
	}
	
}
