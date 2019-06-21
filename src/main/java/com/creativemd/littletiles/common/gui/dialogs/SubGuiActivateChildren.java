package com.creativemd.littletiles.common.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.mc.ContainerSub;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiActivateChildren extends SubGui {
	
	public GuiActivateChildButton button;
	
	public SubGuiActivateChildren(GuiActivateChildButton button) {
		this.button = button;
	}
	
	public String getDisplayName(LittlePreviews previews, int childId) {
		String name = previews.getStructureName();
		if (name == null)
			if (previews.hasStructure())
				name = previews.getStructureId();
			else
				name = "none";
		return name + " " + childId;
	}
	
	@Override
	public void onClosed() {
		super.onClosed();
		button.childActivation = new PairList<>();
		for (Integer integer : button.possibleChildren) {
			GuiCheckBox box = (GuiCheckBox) get("" + integer);
			GuiTextfield textfield = (GuiTextfield) get("time" + integer);
			if (box != null && box.value)
				button.childActivation.add(integer, textfield.parseInteger());
		}
		button.raiseEvent(new GuiControlChangedEvent(button));
	}
	
	@Override
	public void createControls() {
		if (button.childActivation == null)
			button.childActivation = new PairList<>();
		button.possibleChildren = new ArrayList<>();
		int i = 0;
		int added = 0;
		for (LittlePreviews child : button.previews.getChildren()) {
			if (LittleDoor.class.isAssignableFrom(LittleStructureRegistry.getStructureClass(child.getStructureId()))) {
				boolean doesActivate = button.childActivation.containsKey(i);
				GuiCheckBox box = new GuiCheckBox("" + i, getDisplayName(child, i), 0, added * 20, doesActivate);
				controls.add(box);
				controls.add(new GuiTextfield("time" + i, (doesActivate ? button.childActivation.getValue(i) : 0) + "", box.width + 10, added * 20, 40, 12).setNumbersOnly());
				button.possibleChildren.add(i);
				added++;
			}
			i++;
		}
	}
	
	public static class GuiActivateChildButton extends GuiButton {
		
		public SubGuiActivateChildren gui;
		public LittlePreviews previews;
		public LittleDoor activator;
		public PairList<Integer, Integer> childActivation;
		public List<Integer> possibleChildren;
		
		public GuiActivateChildButton(String name, int x, int y, LittlePreviews previews, LittleDoor door) {
			super(name, translate("gui.door.children"), x, y, 40, 7);
			this.previews = previews;
			this.activator = door;
			if (activator != null)
				this.childActivation = activator.childActivation;
		}
		
		@Override
		public void onClicked(int x, int y, int button) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("dialog", true);
			SubGuiActivateChildren dialog = new SubGuiActivateChildren(this);
			dialog.gui = getParent().getOrigin().gui;
			PacketHandler.sendPacketToServer(new GuiLayerPacket(nbt, dialog.gui.getLayers().size() - 1, false));
			dialog.gui.addLayer(dialog);
			dialog.container = new SubContainerEmpty(getPlayer());
			((ContainerSub) dialog.gui.inventorySlots).layers.add(dialog.container);
			dialog.onOpened();
			dialog.gui.resize();
		}
	}
	
}
