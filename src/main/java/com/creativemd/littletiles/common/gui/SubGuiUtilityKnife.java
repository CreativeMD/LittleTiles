package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.items.ItemUtilityKnife.UtilityKnifeMode;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3i;

public class SubGuiUtilityKnife extends SubGui {
	
public ItemStack stack;
	
	public SubGuiUtilityKnife(ItemStack stack) {
		super(140, 43);
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiLabel("Mode:", 0, 3));
		controls.add(new GuiStateButton("mode", stack.getTagCompound().getInteger("mode"), 35, 0, 98, 14, UtilityKnifeMode.names()));
		controls.add(new GuiLabel("Size:", 0, 26));
		controls.add(new GuiSteppedSlider("thickness", 35, 25, 98, 10, ItemUtilityKnife.getThickness(stack), 1, LittleTile.gridSize));
	}
	
	@CustomEventSubscribe
	public void onChange(GuiControlChangedEvent event)
	{
		GuiStateButton mode = (GuiStateButton) get("mode");
		GuiSteppedSlider thickness = (GuiSteppedSlider) get("thickness");
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("mode", mode.getState());
		nbt.setInteger("thick", (int) thickness.value);
		sendPacketToServer(nbt);
	}
	
}
