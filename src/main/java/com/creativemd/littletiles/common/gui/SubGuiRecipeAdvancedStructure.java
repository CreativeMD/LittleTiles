package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry.LittleStructureEntry;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiRecipeAdvancedStructure extends SubGuiConfigure {
	
	public LittleStructure structure;
	public LittleStructureGuiParser parser;
	
	public SubGuiRecipeAdvancedStructure(ItemStack stack) {
		super(350, 200, stack);
	}
	
	@Override
	public void saveConfiguration() {
		
	}
	
	@Override
	public void createControls() {
		controls.add(new GuiButton("clear", translate("selection.clear"), 105, 176, 38) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				openYesNoDialog(translate("selection.dialog.clear"));
			}
		});
		
		ArrayList<String> lines = new ArrayList<>();
		lines.add("none");
		lines.addAll(LittleStructureRegistry.getStructureTypeNames());
		controls.add(new GuiLabel("type:", 2, 7));
		GuiComboBox comboBox = new GuiComboBox("types", 32, 5, 70, lines);
		LittlePreviews previews = LittleTilePreview.getPreview(stack);
		LittleStructure structure = previews.getStructure();
		if (structure != null) {
			this.structure = structure;
			comboBox.index = lines.indexOf(structure.structureID);
			if (comboBox.index == -1)
				comboBox.index = 0;
			else
				comboBox.caption = structure.structureID;
		}
		controls.add(new GuiPanel("renderer", 208, 30, 136, 135));
		controls.add(new GuiPanel("panel", 0, 30, 200, 135));
		controls.add(new GuiLabel("tiles", previews.totalSize() + " tile(s)", 110, 7));
		controls.add(comboBox);
		controls.add(new GuiButton("save", 150, 176, 40) {
			@Override
			public void onClicked(int x, int y, int button) {
				if (SubGuiRecipeAdvancedStructure.this.parser != null) {
					GuiTextfield textfield = (GuiTextfield) get("name");
					LittleStructure structure = SubGuiRecipeAdvancedStructure.this.parser.parseStructure(stack);
					if (structure != null) {
						structure.name = textfield.text.isEmpty() ? null : textfield.text;
						NBTTagCompound structureNBT = new NBTTagCompound();
						structure.writeToNBT(structureNBT);
						stack.getTagCompound().setTag("structure", structureNBT);
						// ItemStack multiTiles = new ItemStack(LittleTiles.multiTiles);
						// multiTiles.stackTagCompound = stack.stackTagCompound;
						// WorldUtils.dropItem(container.player, multiTiles);
					} else
						stack.getTagCompound().removeTag("structure");
					
				} else
					stack.getTagCompound().removeTag("structure");
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("set_structure", true);
				nbt.setTag("stack", stack.getTagCompound());
				sendPacketToServer(nbt);
				closeGui();
			}
		});
		controls.add(new GuiLabel("name:", 0, 179));
		controls.add(new GuiTextfield("name", (structure != null && structure.name != null) ? structure.name : "", 32, 176, 65, 14));
		onChanged();
	}
	
	public void onChanged() {
		GuiPanel panel = (GuiPanel) get("panel");
		panel.controls.clear();
		
		String id = ((GuiComboBox) get("types")).caption;
		
		if (parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if (saved != null && !saved.structureID.equals(id))
			saved = null;
		LittleStructureEntry entry = LittleStructureRegistry.getStructureEntry(id);
		if (entry != null) {
			parser = entry.createParser(panel);
			if (parser != null) {
				parser.createControls(stack, saved);
				panel.refreshControls();
				addListener(parser);
			}
		} else
			parser = null;
		
		get("name").setEnabled(parser != null);
	}
	
	@CustomEventSubscribe
	public void onComboChange(GuiControlChangedEvent event) {
		if (event.source.is("types"))
			onChanged();
	}
	
	@Override
	public void onDialogClosed(String text, String[] buttons, String clicked) {
		if (clicked.equalsIgnoreCase("yes")) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("clear_content", true);
			sendPacketToServer(nbt);
		}
	}
	
	@Override
	public void receiveContainerPacket(NBTTagCompound nbt) {
		stack.setTagCompound(nbt);
	}
}
