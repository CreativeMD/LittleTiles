package com.creativemd.littletiles.common.gui.configure;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiListBox;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.controls.gui.GuiTextBox;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.geo.DragShape;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class SubGuiModeSelector extends SubGuiConfigure {
	
	public SubGuiModeSelector(ItemStack stack) {
		super(140, 150, stack);
	}
	
	public static List<String> names;
	
	@Override
	public void createControls() {
		names = new ArrayList<>(PlacementMode.getModeNames());
		GuiComboBox box = new GuiComboBox("mode", 0, 0, 120, new ArrayList<>(PlacementMode.getLocalizedModeNames()));
		box.select(I18n.translateToLocal(ItemMultiTiles.currentMode.name));
		controls.add(box);
		controls.add(new GuiTextBox("text", "", 0, 22, 120));
		onControlChanged(new GuiControlChangedEvent(box));
	}
	
	public PlacementMode getMode()
	{
		GuiComboBox box = (GuiComboBox) get("mode");
		if(box.index == -1)
			return PlacementMode.getDefault();
		return PlacementMode.getModeOrDefault(names.get(box.index));
	}
	
	@CustomEventSubscribe
	public void onControlChanged(GuiControlChangedEvent event)
	{
		if(event.source.is("mode"))
		{
			PlacementMode mode = getMode();
			((GuiTextBox) get("text")).setText((mode.canPlaceStructures() ? ChatFormatting.BOLD + I18n.translateToLocal("placement.mode.placestructure") + '\n' + ChatFormatting.WHITE : "") + I18n.translateToLocal(mode.name + ".tooltip"));
		}
	}

	@Override
	public void saveConfiguration() {
		ItemMultiTiles.currentMode = getMode();
	}

}
