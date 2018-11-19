package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;

public class SubGuiRecipe extends SubGui {
	
	public ItemStack stack;
	public LittleStructure structure;
	public LittleStructureGuiParser parser;
	public ArrayList<String> lines;
	
	public SubGuiRecipe(ItemStack stack) {
		super(176, 186);
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
		lines = new ArrayList<>();
		lines.add("none");
		lines.addAll(LittleStructureRegistry.getParserIds());
		ArrayList<String> translatedLines = new ArrayList<>();
		for (String string : lines) {
			translatedLines.add(I18n.translateToLocal("structure." + string + ".name"));
		}
		
		GuiComboBox comboBox = new GuiComboBox("types", 0, 5, 100, translatedLines);
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
		controls.add(comboBox);
		controls.add(new GuiButton("save", 115, 163, 50) {
			@Override
			public void onClicked(int x, int y, int button) {
				if (SubGuiRecipe.this.parser != null) {
					GuiTextfield textfield = (GuiTextfield) get("name");
					LittleStructure structure = SubGuiRecipe.this.parser.parseStructure(stack);
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
				
				sendPacketToServer(stack.getTagCompound());
				closeGui();
			}
		});
		controls.add(new GuiLabel("name:", 0, 165));
		controls.add(new GuiTextfield("name", (structure != null && structure.name != null) ? structure.name : "", 32, 163, 65, 14));
		onChanged();
	}
	
	public void onChanged() {
		removeControls("type:", "types", "save", "clear", "name:", "name");
		String id = ((GuiComboBox) get("types")).caption;
		
		if (parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if (saved != null && !saved.structureID.equals(id))
			saved = null;
		parser = LittleStructureRegistry.getParser(this, id);
		if (parser != null) {
			parser.createControls(stack, saved);
			this.refreshControls();
			addListener(parser);
		} else
			parser = null;
		
		get("name").setEnabled(parser != null);
	}
	
	@CustomEventSubscribe
	public void onComboChange(GuiControlChangedEvent event) {
		if (event.source.is("types"))
			onChanged();
	}
}
