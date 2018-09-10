package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructureParser;
import com.creativemd.littletiles.common.structure.LittleStructure.LittleStructureEntry;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;

public class SubGuiRecipeAdvancedStructure extends SubGuiConfigure {
	
	public LittleStructure structure;
	public LittleStructureParser parser;
	
	public SubGuiRecipeAdvancedStructure(ItemStack stack) {
		super(200, 200, stack);
	}

	@Override
	public void saveConfiguration() {
		
	}

	@Override
	public void createControls() {
		controls.add(new GuiButton("clear", translate("selection.clear"), 10, 176, 100) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				openYesNoDialog(translate("selection.dialog.clear"));
			}
		});
		
		ArrayList<String> lines = new ArrayList<>();
		lines.add("none");
		lines.addAll(LittleStructure.getStructureTypeNames());
		controls.add(new GuiLabel("type:", 2, 7));
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
		controls.add(new GuiButton("save", 140, 176, 50){
			@Override
			public void onClicked(int x, int y, int button){
				if(SubGuiRecipeAdvancedStructure.this.parser != null)
				{
					LittleStructure structure = SubGuiRecipeAdvancedStructure.this.parser.parseStructure(stack);
					if(structure != null)
					{
						
						NBTTagCompound structureNBT = new NBTTagCompound();
						structure.writeToNBT(structureNBT);
						stack.getTagCompound().setTag("structure", structureNBT);
						//ItemStack multiTiles = new ItemStack(LittleTiles.multiTiles);
						//multiTiles.stackTagCompound = stack.stackTagCompound;
						//WorldUtils.dropItem(container.player, multiTiles);
					}else
						stack.getTagCompound().removeTag("structure");
					
				}else
					stack.getTagCompound().removeTag("structure");
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("set_structure", true);
				nbt.setTag("stack", stack.getTagCompound());
				sendPacketToServer(nbt);
				closeGui();
			}
		});
		onChanged();
	}
	
	public void onChanged()
	{
		removeControls("type:", "types", "save", "clear");
		String id = ((GuiComboBox) get("types")).caption;
		
		if(parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if(saved != null && !saved.getIDOfStructure().equals(id))
			saved = null;
		LittleStructureEntry entry = LittleStructure.getStructureEntryByID(id); 
		if(entry != null)
		{
			parser = entry.createParser(this);
			if(parser != null)
			{
				parser.createControls(stack, saved);
				this.refreshControls();
				addListener(parser);
			}
		}else
			parser = null;
	}

	@CustomEventSubscribe
	public void onComboChange(GuiControlChangedEvent event)
	{
		if(event.source.is("types"))
			onChanged();
	}
	
	@Override
	public void onDialogClosed(String text, String[] buttons, String clicked) {
		if(clicked.equalsIgnoreCase("yes"))
		{
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
