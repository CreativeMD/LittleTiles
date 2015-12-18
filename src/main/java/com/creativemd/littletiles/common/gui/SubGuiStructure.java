package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.Iterator;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.creativecore.common.gui.controls.GuiButton;
import com.creativemd.creativecore.common.gui.controls.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.GuiListBox;
import com.creativemd.creativecore.common.gui.event.ControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.ControlClickEvent;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructure.LittleStructureEntry;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiStructure extends SubGui{
	
	public ItemStack stack;
	public LittleStructure structure;
	
	public SubGuiStructure(ItemStack stack) {
		super();
		this.stack = stack;
	}

	@Override
	public void createControls() {
		ArrayList<String> lines = new ArrayList<>();
		lines.add("none");
		lines.addAll(LittleStructure.getStructureNames());
		controls.add(new GuiLabel("type:", 5, 7));
		GuiComboBox comboBox = new GuiComboBox("types", 32, 5, 70, lines);
		LittleStructure structure = ItemMultiTiles.getLTStructure(stack);
		if(structure != null)
		{
			this.structure = structure;
			comboBox.index = lines.indexOf(structure.getIDOfStructure());
			if(comboBox.index == -1)
				comboBox.index = 0;
			else
				comboBox.caption = structure.getIDOfStructure();
		}
		controls.add(comboBox);
		controls.add(new GuiButton("save", 120, 140, 50));
		onChanged();
	}
	
	public Object lastListener = null;
	
	public void onChanged()
	{
		removeControls("type:", "types", "save");
		String id = ((GuiComboBox) getControl("types")).caption;
		
		if(lastListener != null)
			removeListener(lastListener);
		
		LittleStructure saved = this.structure;
		if(saved != null && !saved.getIDOfStructure().equals(id))
			saved = null;
		LittleStructure parser = null;
		LittleStructureEntry entry = LittleStructure.getEntryByID(id); 
		if(entry != null)
			parser = entry.parser;
		if(parser != null)
		{
			parser.createControls(this, saved);
			this.refreshControls();
			addListener(parser);
			lastListener = parser;
		}
	}
	
	@CustomEventSubscribe
	public void onComboChange(ControlChangedEvent event)
	{
		if(event.source.is("types"))
			onChanged();
	}
	
	@CustomEventSubscribe
	public void onButtonClicked(ControlClickEvent event)
	{
		if(event.source.is("save"))
		{
			String id = ((GuiComboBox) getControl("types")).caption;
			LittleStructure parser = null;
			LittleStructureEntry entry = LittleStructure.getEntryByID(id); 
			if(entry != null)
				parser = entry.parser;
			if(parser != null)
			{
				LittleStructure structure = parser.parseStructure(this);
				if(structure != null)
				{
					
					NBTTagCompound structureNBT = new NBTTagCompound();
					structure.writeToNBT(structureNBT);
					stack.stackTagCompound.setTag("structure", structureNBT);
					//ItemStack multiTiles = new ItemStack(LittleTiles.multiTiles);
					//multiTiles.stackTagCompound = stack.stackTagCompound;
					//WorldUtils.dropItem(container.player, multiTiles);
				}else
					stack.stackTagCompound.removeTag("structure");
				
			}else
				stack.stackTagCompound.removeTag("structure");
			
			sendPacketToServer(0, stack.stackTagCompound);
			closeGui();
		}
	}

	@Override
	public void drawOverlay(FontRenderer fontRenderer) {
		
	}

}
