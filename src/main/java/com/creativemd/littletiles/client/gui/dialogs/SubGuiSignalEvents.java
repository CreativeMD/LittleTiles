package com.creativemd.littletiles.client.gui.dialogs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiListBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.littletiles.client.gui.signal.SubGuiDialogSignal;
import com.creativemd.littletiles.client.gui.signal.SubGuiDialogSignal.GuiSignalComponent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;
import com.creativemd.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputHandler;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SubGuiSignalEvents extends SubGui {
	
	public GuiSignalEventsButton button;
	public List<GuiSignalEvent> events;
	
	public SubGuiSignalEvents(GuiSignalEventsButton button) {
		super(300, 200);
		this.button = button;
		this.events = new ArrayList<>();
		for (GuiSignalEvent event : button.events)
			this.events.add(event.copy());
	}
	
	public void addEntry(GuiSignalEvent event) {
		GuiScrollBox box = (GuiScrollBox) get("content");
		GuiPanel panel = new GuiPanel("event", 2, 2, 158, 16);
		panel.addControl(new GuiLabel("label", 0, 0));
		panel.addControl(new GuiButton("edit", 100, 0, 30, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("dialog", true);
				SubGuiDialogSignal dialog = new SubGuiDialogSignal(SubGuiSignalEvents.this, event);
				dialog.gui = SubGuiSignalEvents.this.gui;
				PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
				dialog.container = new SubContainerEmpty(getPlayer());
				dialog.gui.addLayer(dialog);
				dialog.onOpened();
			}
		});
		
		panel.addControl(new GuiButton("reset", 145, 0, 6, 6) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				event.reset();
			}
		});
		
		box.addControl(panel);
		event.panel = panel;
		event.updatePanel();
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
		for (int i = 0; i < previews.childrenCount(); i++) {
			LittlePreviews child = previews.getChild(i);
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
		
		GuiScrollBox box = new GuiScrollBox("content", 0, 0, 170, 172);
		
		List<String> values = new ArrayList<>();
		values.add("Inputs:");
		
		for (GuiSignalComponent component : button.inputs)
			values.add(component.display());
		
		values.add("Outputs:");
		
		if (button.events == null) {
			button.events = new ArrayList<>();
			
		}
		GuiListBox components = new GuiListBox("components", 180, 0, 120, 180, values);
		
		controls.add(components);
		controls.add(box);
		
		for (GuiSignalEvent event : button.events)
			addEntry(event);
		
		controls.add(new GuiButton("save", 146, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				SubGuiSignalEvents.this.button.events = events;
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
		public final List<GuiSignalComponent> inputs = new ArrayList<>();
		public final List<GuiSignalComponent> outputs = new ArrayList<>();
		public List<GuiSignalEvent> events;
		
		public GuiSignalEventsButton(String name, int x, int y, LittlePreviews previews, LittleStructure structure, LittleStructureType type) {
			super(name, translate("gui.signal.events"), x, y, 40, 7);
			this.previews = previews;
			this.activator = structure;
			this.type = type;
			this.events = null;
			gatherInputs(previews, type, "", "");
			gatherOutputs(previews, type, "", "");
			
			events = new ArrayList<>();
			for (GuiSignalComponent output : outputs) {
				NBTTagCompound nbt = previews.structureNBT;
				if (output.external) {
					if (nbt == null)
						events.add(new GuiSignalEvent(output, new NBTTagCompound()));
					else {
						boolean found = false;
						NBTTagList list = nbt.getTagList("signal", 10);
						for (int i = 0; i < list.tagCount(); i++) {
							NBTTagCompound outputNBT = list.getCompoundTagAt(i);
							if (outputNBT.getInteger("index") == output.index) {
								events.add(new GuiSignalEvent(output, outputNBT));
								found = true;
								break;
							}
						}
						if (!found)
							events.add(new GuiSignalEvent(output, new NBTTagCompound()));
					}
				} else
					events.add(new GuiSignalEvent(output, nbt == null ? new NBTTagCompound() : nbt.getCompoundTag(output.totalName)));
			}
		}
		
		protected void gatherInputs(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix) {
			if (type != null && type.inputs != null)
				for (int i = 0; i < type.inputs.size(); i++)
					inputs.add(new GuiSignalComponent(prefix + "a" + i, totalNamePrefix, type.inputs.get(i), true, false, i));
				
			for (int i = 0; i < previews.childrenCount(); i++) {
				LittlePreviews child = previews.getChild(i);
				LittleStructureType structure = child.getStructureType();
				String name = child.getStructureName();
				if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == SignalComponentType.INPUT)
					inputs.add(new GuiSignalComponent(prefix + "i" + i, totalNamePrefix + (name != null ? name : "i" + i), (ISignalComponent) structure, true, i));
				
				gatherInputs(child, child.getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."));
			}
		}
		
		protected void gatherOutputs(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix) {
			if (type != null && type.outputs != null)
				for (int i = 0; i < type.outputs.size(); i++)
					outputs.add(new GuiSignalComponent(prefix + "b" + i, totalNamePrefix, type.outputs.get(i), false, false, i));
				
			for (int i = 0; i < previews.childrenCount(); i++) {
				LittlePreviews child = previews.getChild(i);
				LittleStructureType structure = child.getStructureType();
				String name = child.getStructureName();
				if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == SignalComponentType.OUTPUT)
					outputs.add(new GuiSignalComponent(prefix + "o" + i, totalNamePrefix + (name != null ? name : "o" + i), (ISignalComponent) structure, true, i));
				
				gatherOutputs(child, child.getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."));
			}
		}
		
		public void setEventsInStructure(LittleStructure structure) {
			HashMap<Integer, SignalExternalOutputHandler> map = new HashMap<>();
			for (GuiSignalEvent event : events) {
				if (event.component.external) {
					if (event.condition != null)
						map.put(event.component.index, new SignalExternalOutputHandler(structure, event.component.index, event.condition, event.getHandler(structure)));
				} else {
					InternalSignalOutput output = structure.getOutput(event.component.index);
					output.condition = event.condition;
					output.handler = event.getHandler(structure);
				}
			}
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
	
	public static class GuiSignalEvent {
		
		public final GuiSignalComponent component;
		public SignalInputCondition condition;
		public GuiSignalModeConfiguration modeConfig;
		public GuiPanel panel;
		
		public GuiSignalEvent(GuiSignalComponent component, NBTTagCompound nbt) {
			this.component = component;
			try {
				if (nbt.hasKey("con"))
					condition = SignalInputCondition.parseInput(nbt.getString("con"));
				else
					condition = null;
			} catch (ParseException e) {
				condition = null;
			}
			this.modeConfig = SignalMode.getConfig(nbt);
		}
		
		private GuiSignalEvent(GuiSignalComponent component, SignalInputCondition condition, GuiSignalModeConfiguration modeConfig) {
			this.component = component;
			this.condition = condition;
			this.modeConfig = modeConfig;
		}
		
		public void reset() {
			modeConfig = SignalMode.getConfigDefault();
			condition = null;
			updatePanel();
		}
		
		public void updatePanel() {
			GuiLabel label = (GuiLabel) panel.get("label");
			label.caption = component.name + ": " + condition;
			label.width = font.getStringWidth(label.caption) + label.getContentOffset() * 2;
		}
		
		public SignalOutputHandler getHandler(LittleStructure structure) {
			if (condition != null)
				return modeConfig.getHandler(structure);
			return null;
		}
		
		public GuiSignalEvent copy() {
			return new GuiSignalEvent(component, condition, modeConfig.copy());
		}
		
	}
}
