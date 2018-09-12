package com.creativemd.littletiles.common.gui.configure;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiTextBox;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public abstract class SubGuiModeSelector extends SubGuiConfigure {
	
	public LittleGridContext context;
	public PlacementMode mode;
	
	public SubGuiModeSelector(ItemStack stack, LittleGridContext context, PlacementMode mode) {
		super(150, 150, stack);
		this.context = context;
		this.mode = mode;
	}
	
	public List<String> names;
	
	@Override
	public void createControls() {
		names = new ArrayList<>(PlacementMode.getModeNames());
		GuiComboBox box = new GuiComboBox("mode", 0, 0, 120, new ArrayList<>(PlacementMode.getLocalizedModeNames()));
		box.select(I18n.translateToLocal(mode.name));
		controls.add(box);
		controls.add(new GuiTextBox("text", "", 0, 22, 120));
		GuiComboBox contextBox = new GuiComboBox("grid", 128, 0, 15, LittleGridContext.getNames());
		contextBox.select(ItemMultiTiles.currentContext.size + "");
		controls.add(contextBox);
		onControlChanged(new GuiControlChangedEvent(box));
	}
	
	public PlacementMode getMode() {
		GuiComboBox box = (GuiComboBox) get("mode");
		if (box.index == -1)
			return PlacementMode.getDefault();
		return PlacementMode.getModeOrDefault(names.get(box.index));
	}
	
	@CustomEventSubscribe
	public void onControlChanged(GuiControlChangedEvent event) {
		if (event.source.is("mode")) {
			PlacementMode mode = getMode();
			((GuiTextBox) get("text")).setText((mode.canPlaceStructures() ? ChatFormatting.BOLD + I18n.translateToLocal("placement.mode.placestructure") + '\n' + ChatFormatting.WHITE : "") + I18n.translateToLocal(mode.name + ".tooltip"));
		}
	}
	
	public abstract void saveConfiguration(LittleGridContext context, PlacementMode mode);
	
	@Override
	public void saveConfiguration() {
		mode = getMode();
		GuiComboBox contextBox = (GuiComboBox) get("grid");
		try {
			context = LittleGridContext.get(Integer.parseInt(contextBox.caption));
		} catch (NumberFormatException e) {
			context = LittleGridContext.get();
		}
		saveConfiguration(context, mode);
	}
	
}
