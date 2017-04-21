package com.creativemd.littletiles.common.gui;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SubGuiColorTube extends SubGui{
	
	public ItemStack stack;
	
	public SubGuiColorTube(ItemStack stack) {
		super(150, 43);
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		Color color = ColorUtils.IntToRGBA(ItemColorTube.getColor(stack));
		color.setAlpha(255);
		controls.add(new GuiColorPicker("picker", 5, 5, color));
	}
	
	@CustomEventSubscribe
	public void onChange(GuiControlChangedEvent event)
	{
		GuiColorPicker picker = (GuiColorPicker) get("picker");
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("color", ColorUtils.RGBAToInt(picker.color));
		sendPacketToServer(nbt);
	}

}
