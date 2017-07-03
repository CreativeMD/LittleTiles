package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.shapes.ChiselShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiChisel extends SubGui {
	
	public ItemStack stack;
	
	public SubGuiChisel(ItemStack stack) {
		super(140, 150);
		this.stack = stack;
	}

	@Override
	public void createControls() {
		GuiComboBox box = new GuiComboBox("shape", 0, 0, 134, new ArrayList<>(ChiselShape.shapes.keySet()));
		box.select(ItemLittleChisel.getShape(stack).key);
		GuiScrollBox scroll = new GuiScrollBox("settings", 0, 23, 134, 100);
		controls.add(box);
		controls.add(scroll);
		controls.add(new GuiButton("save", 110, 130) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				ChiselShape shape = ChiselShape.getShape(box.caption);
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("shape", shape.key);
				shape.saveCustomSettings(scroll, nbt);
				sendPacketToServer(nbt);
				closeGui();
			}
		});
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
		
		ChiselShape shape = ChiselShape.getShape(box.caption);
		scroll.controls.clear();
		scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound()));
		scroll.refreshControls();
	}
}
