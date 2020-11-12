package com.creativemd.littletiles.client.gui.dialogs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiListBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponent;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.logic.event.SignalEvent;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiSignalEvents extends SubGui {
	
	public GuiSignalEventsButton button;
	
	public SubGuiSignalEvents(GuiSignalEventsButton button) {
		super(300, 200);
		this.button = button;
	}
	
	public static String getDisplayName(LittlePreviews previews, int childId) {
		String name = previews.getStructureName();
		if (name == null)
			if (previews.hasStructure())
				name = previews.getStructureId();
			else
				name = "none";
		return childId + ": " + name;
	}
	
	public void addEntry(@Nullable SignalEvent event) {
		GuiScrollBox box = (GuiScrollBox) get("content");
		GuiPanel panel = new GuiPanel("event", 2, 2, 158, 16);
		panel.addControl(new GuiTextfield("pattern", event != null ? event.write() : "", 0, 0, 135, 10));
		panel.addControl(new GuiButton("x", 145, 0, 6, 6) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				box.removeControl(panel);
				reloadListBox();
			}
		});
		
		box.addControl(panel);
		reloadListBox();
	}
	
	public void reloadListBox() {
		GuiScrollBox box = (GuiScrollBox) get("content");
		int height = 2;
		for (GuiControl control : box.controls) {
			control.posY = height;
			height += control.height + 2;
		}
	}
	
	public List<String> getComponents(LittlePreviews previews, SignalComponentType type) {
		List<String> values = new ArrayList<>();
		for (int i = 0; i < previews.getChildren().size(); i++) {
			LittlePreviews child = previews.getChildren().get(i);
			LittleStructureType structure = child.getStructureType();
			if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == type) {
				String name = child.getStructureName();
				values.add(ChatFormatting.BOLD + (type == SignalComponentType.INPUT ? "i" : "o") + i + " " + (name != null ? "(" + name + ") " : "") + "" + ChatFormatting.RESET + ((ISignalComponent) structure).getBandwidth() + "-bit");
			}
		}
		return values;
	}
	
	@Override
	public void createControls() {
		List<String> values = new ArrayList<>();
		values.add("Inputs:");
		
		if (button.type.inputs != null)
			for (int i = 0; i < button.type.inputs.size(); i++) {
				InternalComponent component = button.type.inputs.get(i);
				values.add(ChatFormatting.BOLD + "a" + i + " " + component.identifier + ChatFormatting.RESET + component.bandwidth + "-bit");
			}
		
		values.addAll(getComponents(button.previews, SignalComponentType.INPUT));
		
		values.add("Outputs:");
		
		if (button.type.outputs != null)
			for (int i = 0; i < button.type.outputs.size(); i++) {
				InternalComponent component = button.type.outputs.get(i);
				values.add(ChatFormatting.BOLD + "b" + i + " " + component.identifier + ChatFormatting.RESET + component.bandwidth + "-bit");
			}
		
		values.addAll(getComponents(button.previews, SignalComponentType.OUTPUT));
		
		GuiListBox components = new GuiListBox("components", 180, 0, 120, 180, values);
		
		controls.add(components);
		GuiScrollBox box = new GuiScrollBox("content", 0, 0, 170, 172);
		controls.add(box);
		controls.add(new GuiButton("add", 110, 180, 20, 14) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				addEntry(null);
			}
		});
		for (SignalEvent event : button.events)
			addEntry(event);
		
		controls.add(new GuiButton("save", 146, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				SubGuiSignalEvents.this.button.events.clear();
				for (GuiControl control : box.controls) {
					if (control instanceof GuiPanel) {
						GuiTextfield textfield = (GuiTextfield) ((GuiPanel) control).get("pattern");
						try {
							SubGuiSignalEvents.this.button.events.add(new SignalEvent(textfield.text));
						} catch (ParseException e) {}
					}
				}
				SubGuiSignalEvents.this.button.events.sort(null);
				closeGui();
				SubGuiSignalEvents.this.button.raiseEvent(new GuiControlChangedEvent(SubGuiSignalEvents.this.button));
			}
		});
		controls.add(new GuiButton("cancel", 0, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				closeGui();
			}
		});
	}
	
	public static class GuiSignalEventsButton extends GuiButton {
		
		public SubGuiSignalEvents gui;
		public LittlePreviews previews;
		public LittleStructureType type;
		public LittleStructure activator;
		public List<SignalEvent> events;
		
		public GuiSignalEventsButton(String name, int x, int y, LittlePreviews previews, LittleStructure structure, LittleStructureType type) {
			super(name, translate("gui.signal.events"), x, y, 40, 7);
			this.previews = previews;
			this.activator = structure;
			this.type = type;
			if (activator != null)
				this.events = activator.getSignalEvents();
			if (this.events == null)
				this.events = new ArrayList<>();
		}
		
		@Override
		public void onClicked(int x, int y, int button) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("dialog", true);
			SubGuiSignalEvents dialog = new SubGuiSignalEvents(this);
			dialog.gui = getParent().getOrigin().gui;
			PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
			dialog.container = new SubContainerEmpty(getPlayer());
			dialog.gui.addLayer(dialog);
			dialog.onOpened();
		}
	}
	
}
