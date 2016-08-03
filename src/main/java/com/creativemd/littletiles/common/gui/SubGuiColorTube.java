package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
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
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		Vec3i color = ColorUtils.IntToRGB(ItemColorTube.getColor(stack));
		controls.add(new GuiSteppedSlider("colorX", 5, 75, 100, 10, 0, 255, (int)color.getX()));
		controls.add(new GuiSteppedSlider("colorY", 5, 85, 100, 10, 0, 255, (int)color.getY()));
		controls.add(new GuiSteppedSlider("colorZ", 5, 95, 100, 10, 0, 255, (int)color.getZ()));
		controls.add(new GuiColorPlate("plate", 120, 80, 20, 20, color));
	}
	
	@CustomEventSubscribe
	public void onChange(GuiControlChangedEvent event)
	{
		GuiColorPlate plate = (GuiColorPlate) get("plate");
		plate.setColor(new Vec3i((int) ((GuiSteppedSlider) get("colorX")).value, (int) ((GuiSteppedSlider) get("colorY")).value, (int) ((GuiSteppedSlider) get("colorZ")).value));
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("color", ColorUtils.RGBAToInt(plate.getColor()));
		sendPacketToServer(nbt);
	}

}
