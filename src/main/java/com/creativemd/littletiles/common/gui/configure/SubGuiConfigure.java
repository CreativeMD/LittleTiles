package com.creativemd.littletiles.common.gui.configure;

import com.creativemd.creativecore.gui.container.SubGui;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class SubGuiConfigure extends SubGui {
	
	public ItemStack stack;
	
	public SubGuiConfigure(int width, int height, ItemStack stack) {
		super(width, height);
		this.stack = stack;
	}
	
	public abstract void saveConfiguration();
	
	@Override
	public void onClosed() {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		saveConfiguration();
		
		sendPacketToServer(stack.getTagCompound());
		
		super.onClosed();
	}
	
}
