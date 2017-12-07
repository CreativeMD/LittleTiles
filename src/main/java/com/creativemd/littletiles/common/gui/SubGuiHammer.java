package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.items.geo.SelectShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiHammer extends SubGui {
	
public ItemStack stack;
	
	public SubGuiHammer(ItemStack stack) {
		super(140, 150);
		this.stack = stack;
	}
	
	@Override
	public void onClosed() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		SelectShape shape = SelectShape.getShape(box.caption);
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("shape", shape.key);
		shape.saveCustomSettings(scroll, nbt);
		sendPacketToServer(nbt);
		
		super.onClosed();
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
	public void onComboBoxChange(GuiControlChangedEvent event)
	{
		if(event.source.is("shape"))
			onChange();
	}
	
	public void onChange()
	{
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		
		SelectShape shape = SelectShape.getShape(box.caption);
		scroll.controls.clear();
		scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound()));
		scroll.refreshControls();
	}
	
}
