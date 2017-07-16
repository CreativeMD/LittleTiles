package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.geo.ChiselShape;
import com.creativemd.littletiles.common.items.geo.SelectShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiChisel extends SubGui {
	
	public ItemStack stack;
	
	public SubGuiChisel(ItemStack stack) {
		super(140, 150);
		this.stack = stack;
	}
	
	@Override
	public void onClosed() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		ChiselShape shape = ChiselShape.getShape(box.caption);
		
		NBTTagCompound nbt = new NBTTagCompound();
		
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		nbt.setInteger("color", ColorUtils.RGBAToInt(picker.color));
		
		IBlockState state = ItemLittleChisel.getBlockState(stack);
		nbt.setInteger("state", Block.getStateId(state));
		
		nbt.setString("shape", shape.key);
		shape.saveCustomSettings(scroll, nbt);
		sendPacketToServer(nbt);
		
		super.onClosed();
	}

	@Override
	public void createControls() {
		
		Color color = ColorUtils.IntToRGBA(ItemColorTube.getColor(stack));
		color.setAlpha(255);
		controls.add(new GuiColorPicker("picker", 2, 2, color));
		
		GuiComboBox box = new GuiComboBox("shape", 0, 40, 134, new ArrayList<>(ChiselShape.shapes.keySet()));
		box.select(ItemLittleChisel.getShape(stack).key);
		GuiScrollBox scroll = new GuiScrollBox("settings", 0, 63, 134, 80);
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
		
		ChiselShape shape = ChiselShape.getShape(box.caption);
		scroll.controls.clear();
		scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound()));
		scroll.refreshControls();
	}
}
