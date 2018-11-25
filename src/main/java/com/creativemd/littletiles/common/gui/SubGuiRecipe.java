package com.creativemd.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBoxCategory;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.gui.controls.GuiAnimationViewer;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiRecipe extends SubGuiConfigure {
	
	public LittleStructure structure;
	public LittleStructureGuiParser parser;
	public PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> craftables;
	
	public SubGuiRecipe(ItemStack stack) {
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
		
		PairList<String, Class<? extends LittleStructureGuiParser>> noneCategory = new PairList<>();
		noneCategory.add("structure.none.name", null);
		craftables = new PairList<>(LittleStructureRegistry.getCraftables());
		craftables.add(0, new Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>>("", noneCategory));
		GuiComboBoxCategory comboBox = new GuiComboBoxCategory<Class<? extends LittleStructureGuiParser>>("types", 0, 5, 80, craftables);
		LittlePreviews previews = LittleTilePreview.getPreview(stack);
		LittleStructure structure = previews.getStructure();
		if (structure != null) {
			this.structure = structure;
			int index = 0;
			for (Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>> category : craftables) {
				int currentIndex = category.value.indexOfKey(structure.structureID);
				if (currentIndex != -1) {
					comboBox.select(currentIndex + index);
					break;
				}
				index += category.value.size();
			}
		}
		int size = previews.totalSize();
		controls.add(new GuiLabel("tiles", previews.totalSize() + " " + translate(size == 1 ? "selection.structure.tile" : "selection.structure.tiles"), 208, 158));
		controls.add(new GuiAnimationViewer("renderer", 208, 30, 136, 135, stack));
		controls.add(new GuiPanel("panel", 0, 30, 200, 135));
		controls.add(comboBox);
		controls.add(new GuiButton("save", 150, 176, 40) {
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
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("set_structure", true);
				nbt.setTag("stack", stack.getTagCompound());
				sendPacketToServer(nbt);
				closeGui();
			}
		});
		controls.add(new GuiTextfield("name", (structure != null && structure.name != null) ? structure.name : "", 2, 176, 95, 14).setCustomTooltip(translate("selection.structure.name")));
		onChanged();
	}
	
	public void onChanged() {
		GuiPanel panel = (GuiPanel) get("panel");
		panel.controls.clear();
		
		GuiComboBoxCategory<Class<? extends LittleStructureGuiParser>> types = (GuiComboBoxCategory) get("types");
		Pair<String, Class<? extends LittleStructureGuiParser>> selected = types.getSelected();
		
		if (parser != null)
			removeListener(parser);
		
		LittleStructure saved = this.structure;
		if (saved != null && !saved.structureID.equals(selected.key))
			saved = null;
		
		parser = LittleStructureRegistry.getParser(panel, selected.value);
		if (parser != null) {
			parser.createControls(stack, saved);
			panel.refreshControls();
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
