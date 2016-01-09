package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.gui.controls.GuiColorPlate;
import com.creativemd.creativecore.common.gui.controls.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.event.ControlChangedEvent;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

public class SubGuiColorTube extends SubGui{
	
	public ItemStack stack;
	
	public SubGuiColorTube(ItemStack stack) {
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		Vec3 color = ColorUtils.IntToRGB(ItemColorTube.getColor(stack));
		controls.add(new GuiSteppedSlider("colorX", 5, 75, 100, 10, 0, 255, (int)color.xCoord));
		controls.add(new GuiSteppedSlider("colorY", 5, 85, 100, 10, 0, 255, (int)color.yCoord));
		controls.add(new GuiSteppedSlider("colorZ", 5, 95, 100, 10, 0, 255, (int)color.zCoord));
		controls.add(new GuiColorPlate("plate", 120, 80, 20, 20, color));
	}
	
	@CustomEventSubscribe
	public void onChange(ControlChangedEvent event)
	{
		GuiColorPlate plate = (GuiColorPlate) getControl("plate");
		plate.color = Vec3.createVectorHelper((int) ((GuiSteppedSlider) getControl("colorX")).value, (int) ((GuiSteppedSlider) getControl("colorY")).value, (int) ((GuiSteppedSlider) getControl("colorZ")).value);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("color", ColorUtils.RGBToInt(plate.color));
		sendPacketToServer(0, nbt);
	}

	@Override
	public void drawOverlay(FontRenderer fontRenderer) {
		
	}

}
