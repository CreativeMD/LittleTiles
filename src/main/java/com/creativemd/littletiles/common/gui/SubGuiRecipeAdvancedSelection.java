package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.gui.GuiControl;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiListBox;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.selection.mode.SelectionMode;
import com.creativemd.littletiles.common.utils.selection.mode.SelectionMode.SelectionResult;
import com.mojang.realmsclient.gui.ChatFormatting;

import ibxm.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiRecipeAdvancedSelection extends SubGuiConfigure {

	public SubGuiRecipeAdvancedSelection(ItemStack stack) {
		super(200, 200, stack);
	}

	@Override
	public void saveConfiguration() {
		
	}

	@Override
	public void createControls() {
		SelectionMode mode = ItemRecipeAdvanced.getSelectionMode(stack);
		GuiComboBox box = new GuiComboBox("selection_mode", 0, 0, 100, translate(SelectionMode.names()));
		box.select(mode.name);
		controls.add(box);
		
		SelectionResult result = mode.generateResult(getPlayer().world, stack);
		
		GuiCheckBox vanilla = new GuiCheckBox("includeVanilla", translate("selection.include.vanilla"), 0, 20, false);
		if(result != null && result.blocks > 0)
			vanilla.setCustomTooltip(result.blocks + " block(s)");
		else
			vanilla.enabled = false;
		controls.add(vanilla);
		
		GuiCheckBox cb = new GuiCheckBox("includeCB", translate("selection.include.cb"), 0, 40, true);
		if(result != null && result.cbBlocks > 0)
			cb.setCustomTooltip(result.cbBlocks + " block(s)", result.cbTiles + " tile(s)", result.minCBContext.size + " grid");
		else
			cb.enabled = false;
		controls.add(cb);
		
		GuiCheckBox lt = new GuiCheckBox("includeLT", translate("selection.include.lt"), 0, 60, true);
		if(result != null && result.ltBlocks > 0)
			lt.setCustomTooltip(result.ltBlocks + " block(s)", result.ltTiles + " tile(s)", result.minLtContext.size + " grid");
		else
			lt.enabled = false;		
		controls.add(lt);
		
		controls.add((GuiControl) new GuiCheckBox("remember_structure", translate("selection.include.structure"), 0, 80, false).setEnabled(false));
		// accurate
		// scale slider
		
		controls.add((GuiControl) new GuiButton("save", translate("selection.save"), 114, 180, 80) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				mode.saveSelection(stack);
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("save_selection", true);
				nbt.setBoolean("includeVanilla", ((GuiCheckBox) get("includeVanilla")).value);
				nbt.setBoolean("includeCB", ((GuiCheckBox) get("includeCB")).value);
				nbt.setBoolean("includeLT", ((GuiCheckBox) get("includeLT")).value);
				sendPacketToServer(nbt);
			}
		}.setEnabled(result != null));
	}
	
	@Override
	public void receiveContainerPacket(NBTTagCompound nbt) {
		stack.setTagCompound(nbt);
	}

}
