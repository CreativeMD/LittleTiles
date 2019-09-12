package com.creativemd.littletiles.client.gui;

import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.items.ItemLittleGrabber;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.GrabberMode;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class SubGuiGrabber extends SubGuiConfigure {
	
	public final GrabberMode mode;
	public final int index;
	public final GrabberMode[] modes;
	public LittleGridContext context;
	
	public SubGuiGrabber(GrabberMode mode, ItemStack stack, int width, int height, LittleGridContext context) {
		super(width, height, stack);
		this.mode = mode;
		this.modes = ItemLittleGrabber.getModes();
		this.index = ItemLittleGrabber.indexOf(mode);
		this.context = context;
	}
	
	@Override
	public void onClosed() {
		super.onClosed();
		ItemLittleGrabber.setMode(stack, mode);
		saveConfiguration();
		sendPacketToServer(stack.getTagCompound());
	}
	
	public void openNewGui(GrabberMode mode) {
		ItemLittleGrabber.setMode(stack, mode);
		GuiHandler.openGui("grabber", new NBTTagCompound(), getPlayer());
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiButton("<<", 0, 0, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				int newIndex = index - 1;
				if (newIndex < 0)
					newIndex = modes.length - 1;
				openNewGui(modes[newIndex]);
			}
			
		});
		
		controls.add(new GuiButton(">>", 124, 0, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				int newIndex = index + 1;
				if (newIndex >= modes.length)
					newIndex = 0;
				openNewGui(modes[newIndex]);
			}
			
		});
		
		controls.add(new GuiLabel(mode.getLocalizedName(), 20, 3));
	}
	
}
