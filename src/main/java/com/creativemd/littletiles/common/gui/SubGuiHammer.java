package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.shape.SelectShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiHammer extends SubGuiConfigure {
	
	public SubGuiHammer(ItemStack stack) {
		super(140, 150, stack);
	}
	
	public LittleGridContext getContext() {
		return ((ISpecialBlockSelector) stack.getItem()).getContext(stack);
	}
	
	@Override
	public void saveConfiguration() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		SelectShape shape = SelectShape.getShape(box.caption);
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("shape", shape.key);
		shape.saveCustomSettings(scroll, nbt, getContext());
		stack.setTagCompound(nbt);
	}
	
	@Override
	public void createControls() {
		GuiComboBox box = new GuiComboBox("shape", 0, 0, 134, new ArrayList<>(SelectShape.shapes.keySet()));
		box.select(ItemHammer.getShape(stack).key);
		GuiScrollBox scroll = new GuiScrollBox("settings", 0, 23, 134, 120);
		controls.add(box);
		controls.add(scroll);
		onChange();
	}
	
	@CustomEventSubscribe
	public void onComboBoxChange(GuiControlChangedEvent event) {
		if (event.source.is("shape"))
			onChange();
	}
	
	public void onChange() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		
		SelectShape shape = SelectShape.getShape(box.caption);
		scroll.controls.clear();
		scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound(), getContext()));
		scroll.refreshControls();
	}
	
}
