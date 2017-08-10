package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.gui.client.style.ColoredDisplayStyle;
import com.creativemd.creativecore.gui.client.style.DisplayStyle;
import com.creativemd.creativecore.gui.client.style.Style;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiProgressBar;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.littletiles.common.container.SubContainerTileContainer;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.items.ItemTileContainer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiTileContainer extends SubGui{
	
	public ItemStack stack;
	
	public SubGuiTileContainer(ItemStack stack)
	{
		super();
		this.stack = stack;
	}
	
	public static Style blackStyle = new Style("black", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(50, 50, 50));
	public static Style redStyle = new Style("red", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(255, 0, 0), new ColoredDisplayStyle(50, 50, 50));
	public static Style greenStyle = new Style("green", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 255, 0), new ColoredDisplayStyle(50, 50, 50));
	public static Style blueStyle = new Style("blue", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 0, 255), new ColoredDisplayStyle(50, 50, 50));
	@Override
	public void createControls() {
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		ColorUnit unit = ItemTileContainer.loadColorUnit(stack);
		
		controls.add(new GuiProgressBar("black", 120, 26, 45, 3, ItemTileContainer.colorUnitMaximum, unit.BLACK).setStyle(blackStyle));
		controls.add(new GuiProgressBar("red", 120, 40, 45, 3, ItemTileContainer.colorUnitMaximum, unit.RED).setStyle(redStyle));
		controls.add(new GuiProgressBar("green", 120, 54, 45, 3, ItemTileContainer.colorUnitMaximum, unit.GREEN).setStyle(greenStyle));
		controls.add(new GuiProgressBar("blue", 120, 68, 45, 3, ItemTileContainer.colorUnitMaximum, unit.BLUE).setStyle(blueStyle));
	}
	
	@Override
	public void receiveContainerPacket(NBTTagCompound nbt)
	{
		if(nbt.getBoolean("reload"))
		{
			nbt.removeTag("reload");
			stack.setTagCompound(nbt);
			controls.clear();
			createControls();
			refreshControls();
			container.controls.clear();
			((SubContainerTileContainer) container).stack = stack;
			container.createControls();
			container.refreshControls();
			addContainerControls();
			refreshControls();
		}
	}
	
}
