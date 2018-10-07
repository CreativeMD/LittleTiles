package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.LittleStructureRegistry.LittleStructureEntry;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiRecipe extends SubGui {
	
	public ItemStack stack;
	public LittleStructure structure;
	public LittleStructureGuiParser parser;
	
	public SubGuiRecipe(ItemStack stack) {
		super(176, 186);
		this.stack = stack;
	}
	
	@Override
	public void createControls() {
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
		controls.add(comboBox);
		controls.add(new GuiButton("save", 115, 163, 50) {
			@Override
			public void onClicked(int x, int y, int button) {
				if (SubGuiRecipe.this.parser != null) {
					LittleStructure structure = SubGuiRecipe.this.parser.parseStructure(stack);
					if (structure != null) {
						
						NBTTagCompound structureNBT = new NBTTagCompound();
						structure.writeToNBT(structureNBT);
						stack.getTagCompound().setTag("structure", structureNBT);
						//ItemStack multiTiles = new ItemStack(LittleTiles.multiTiles);
						//multiTiles.stackTagCompound = stack.stackTagCompound;
						//WorldUtils.dropItem(container.player, multiTiles);
					} else
						stack.getTagCompound().removeTag("structure");
					
				} else
					stack.getTagCompound().removeTag("structure");
				
				sendPacketToServer(stack.getTagCompound());
				closeGui();
			}
		});
		onChanged();
	}
	
	public void onChanged() {
		removeControls("type:", "types", "save", "clear");
		String id = ((GuiComboBox) get("types")).caption;
		
		if (parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if (saved != null && !saved.structureID.equals(id))
			saved = null;
		LittleStructureEntry entry = LittleStructureRegistry.getStructureEntry(id);
		if (entry != null) {
			parser = entry.createParser(this);
			if (parser != null) {
				parser.createControls(stack, saved);
				this.refreshControls();
				addListener(parser);
			}
		} else
			parser = null;
	}
	
	@CustomEventSubscribe
	public void onComboChange(GuiControlChangedEvent event) {
		if (event.source.is("types"))
			onChanged();
	}
}
