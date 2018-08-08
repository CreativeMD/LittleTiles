package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.shape.SelectShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SubGuiColorTube extends SubGui{
	
	public ItemStack stack;
	
	public SubGuiColorTube(ItemStack stack) {
		super(140, 173);
		this.stack = stack;
	}
	
	public LittleGridContext getContext()
	{
		return ((ISpecialBlockSelector) stack.getItem()).getContext(stack);
	}
	
	@Override
	public void createControls() {
		Color color = ColorUtils.IntToRGBA(ItemColorTube.getColor(stack));
		//color.setAlpha(255);
		controls.add(new GuiColorPicker("picker", 2, 2, color, SpecialServerConfig.isTransparencyEnabled(getPlayer()), SpecialServerConfig.getMinimumTransparencty(getPlayer())));
		
		ArrayList<String> shapes = new ArrayList<>(SelectShape.shapes.keySet());
		shapes.add(0, "tile");
		GuiComboBox box = new GuiComboBox("shape", 0, 50, 134, shapes);
		SelectShape shape = ItemColorTube.getShape(stack);
		box.select(shape == null ? "tile" : shape.key);
		GuiScrollBox scroll = new GuiScrollBox("settings", 0, 73, 134, 90);
		controls.add(box);
		controls.add(scroll);
		onChange();
	}
	
	@Override
	public void onClosed() {
		GuiComboBox box = (GuiComboBox) get("shape");
		GuiScrollBox scroll = (GuiScrollBox) get("settings");
		SelectShape shape = box.caption.equals("tile") || box.caption.equals("") ? null : SelectShape.getShape(box.caption);
		
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("shape", shape == null ? "tile" : shape.key);
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		nbt.setInteger("color", ColorUtils.RGBAToInt(picker.color));
		if(shape != null)
			shape.saveCustomSettings(scroll, nbt, getContext());
		sendPacketToServer(nbt);
		
		super.onClosed();
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
		
		scroll.controls.clear();
		SelectShape shape =  box.caption.equals("tile") || box.caption.equals("") ? null : SelectShape.getShape(box.caption);
		if(shape != null)
		{
			scroll.controls.addAll(shape.getCustomSettings(stack.getTagCompound(), getContext()));
			scroll.refreshControls();
		}
	}

}
