package com.creativemd.littletiles.common.gui.configure;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.shape.DragShape;

import net.minecraft.item.ItemBlock;
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
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		saveConfiguration();
		
		sendPacketToServer(stack.getTagCompound());
		
		super.onClosed();
	}

}
