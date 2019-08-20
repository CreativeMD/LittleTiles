package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.client.style.ColoredDisplayStyle;
import com.creativemd.creativecore.common.gui.client.style.DisplayStyle;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiProgressBar;
import com.creativemd.littletiles.common.container.SubContainerBag;
import com.creativemd.littletiles.common.items.ItemBag;
import com.creativemd.littletiles.common.utils.ingredients.ColorIngredient;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiBag extends SubGui {
	
	public ItemStack stack;
	public LittleIngredients bag;
	
	public SubGuiBag(ItemStack stack) {
		super();
		this.stack = stack;
	}
	
	public static Style blackStyle = new Style("black", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(50, 50, 50));
	public static Style cyanStyle = new Style("cyan", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(0, 255, 255), new ColoredDisplayStyle(50, 50, 50));
	public static Style magentaStyle = new Style("magenta", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(255, 0, 255), new ColoredDisplayStyle(50, 50, 50));
	public static Style yellowStyle = new Style("yellow", new ColoredDisplayStyle(0, 0, 0), new ColoredDisplayStyle(120, 120, 120), DisplayStyle.emptyDisplay, new ColoredDisplayStyle(255, 255, 0), new ColoredDisplayStyle(50, 50, 50));
	
	@Override
	public void createControls() {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		bag = ((ItemBag) stack.getItem()).getInventory(stack);
		ColorIngredient unit = bag.get(ColorIngredient.class);
		
		controls.add(new GuiProgressBar("black", 120, 26, 45, 3, ItemBag.colorUnitMaximum, unit.black).setStyle(blackStyle));
		controls.add(new GuiProgressBar("cyan", 120, 40, 45, 3, ItemBag.colorUnitMaximum, unit.cyan).setStyle(cyanStyle));
		controls.add(new GuiProgressBar("magenta", 120, 54, 45, 3, ItemBag.colorUnitMaximum, unit.magenta).setStyle(magentaStyle));
		controls.add(new GuiProgressBar("yellow", 120, 68, 45, 3, ItemBag.colorUnitMaximum, unit.yellow).setStyle(yellowStyle));
	}
	
	@Override
	public void receiveContainerPacket(NBTTagCompound nbt) {
		if (nbt.getBoolean("reload")) {
			nbt.removeTag("reload");
			stack.setTagCompound(nbt);
			controls.clear();
			createControls();
			refreshControls();
			container.controls.clear();
			((SubContainerBag) container).stack = stack;
			container.createControls();
			container.refreshControls();
			addContainerControls();
			refreshControls();
		}
	}
	
}
