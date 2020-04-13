package com.creativemd.littletiles.client.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEventGuiParser;
import com.creativemd.littletiles.common.structure.type.door.LittleDoorBase;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiDoorEvents extends SubGui {
	
	public GuiDoorEventsButton button;
	
	public SubGuiDoorEvents(GuiDoorEventsButton button) {
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
	
	public void addEntry(@Nullable AnimationEvent event, String type) {
		GuiScrollBox box = (GuiScrollBox) get("content");
		
		AnimationEventGuiParser parser = AnimationEvent.getParser(type);
		
		GuiPanel panel = new GuiPanel(type, 2, 2, 158, parser.getHeight());
		panel.addControl(new GuiTextfield("tick", "" + (event != null ? event.getTick() : 0), 0, 0, 30, 10).setNumbersOnly().setCustomTooltip("tick"));
		panel.addControl(new GuiButton("x", 145, 0, 6, 6) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				box.removeControl(panel);
				reloadListBox();
			}
		});
		
		parser.createControls(panel, event, button.previews);
		
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
	
	@Override
	public void createControls() {
		GuiScrollBox box = new GuiScrollBox("content", 0, 0, 170, 110);
		controls.add(box);
		GuiComboBox type = new GuiComboBox("type", 0, 120, 100, AnimationEvent.typeNamestranslated());
		controls.add(type);
		controls.add(new GuiButton("+", 110, 123, 10, 8) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				addEntry(null, AnimationEvent.typeNames().get(type.index));
			}
		});
		for (AnimationEvent event : button.events) {
			addEntry(event, AnimationEvent.getId(event.getClass()));
		}
		
		controls.add(new GuiButton("save", 140, 143) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				SubGuiDoorEvents.this.button.events.clear();
				for (GuiControl control : box.controls) {
					if (control instanceof GuiPanel) {
						AnimationEventGuiParser parser = AnimationEvent.getParser(control.name);
						GuiTextfield textfield = (GuiTextfield) ((GuiPanel) control).get("tick");
						AnimationEvent event = AnimationEvent.create(textfield.parseInteger(), control.name);
						event = parser.parse((GuiParent) control, event);
						if (event != null)
							SubGuiDoorEvents.this.button.events.add(event);
					}
				}
				SubGuiDoorEvents.this.button.events.sort(null);
				closeGui();
				SubGuiDoorEvents.this.button.raiseEvent(new GuiControlChangedEvent(SubGuiDoorEvents.this.button));
			}
		});
		controls.add(new GuiButton("cancel", 0, 143) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				closeGui();
			}
		});
	}
	
	public static class GuiDoorEventsButton extends GuiButton {
		
		public SubGuiDoorEvents gui;
		public LittlePreviews previews;
		public LittleDoorBase activator;
		public List<AnimationEvent> events;
		
		public GuiDoorEventsButton(String name, int x, int y, LittlePreviews previews, LittleDoorBase door) {
			super(name, translate("gui.door.events"), x, y, 40, 7);
			this.previews = previews;
			this.activator = door;
			if (activator != null)
				this.events = activator.events;
			if (this.events == null)
				this.events = new ArrayList<>();
		}
		
		@Override
		public void onClicked(int x, int y, int button) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("dialog", true);
			SubGuiDoorEvents dialog = new SubGuiDoorEvents(this);
			dialog.gui = getParent().getOrigin().gui;
			PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
			dialog.container = new SubContainerEmpty(getPlayer());
			dialog.gui.addLayer(dialog);
			dialog.onOpened();
		}
	}
	
}
