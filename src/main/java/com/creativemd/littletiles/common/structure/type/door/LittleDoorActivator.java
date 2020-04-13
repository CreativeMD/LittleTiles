package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.animation.AnimationTimeline;
import com.creativemd.littletiles.common.structure.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.structure.animation.event.ChildActivateEvent;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class LittleDoorActivator extends LittleDoor {
	
	public int[] toActivate;
	
	public LittleDoorActivator(LittleStructureType type) {
		super(type);
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		nbt.setIntArray("activate", toActivate);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		toActivate = nbt.getIntArray("activate");
	}
	
	public LittleDoor getChildrenDoor(int index) {
		if (index >= 0 && index < children.size()) {
			LittleStructure structure = children.get(index).getStructure(getWorld());
			if (structure instanceof LittleDoor)
				return (LittleDoor) structure;
			return null;
		}
		return null;
	}
	
	@Override
	public EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result, boolean tickOnce) {
		for (int i : toActivate) {
			LittleDoor child = getChildrenDoor(i);
			if (child == null)
				continue;
			EntityAnimation childAnimation = child.openDoor(player, uuid, result, tickOnce);
			if (childAnimation != null)
				childAnimation.controller.onServerApproves();
		}
		return null;
	}
	
	@Override
	public int getCompleteDuration() {
		int duration = 0;
		for (int i : toActivate) {
			LittleDoor child = getChildrenDoor(i);
			if (child == null)
				continue;
			duration = Math.max(duration, child.getCompleteDuration());
		}
		return duration;
	}
	
	@Override
	public List<LittleDoor> collectDoorsToCheck() {
		List<LittleDoor> doors = new ArrayList<>();
		for (int i : toActivate) {
			LittleDoor child = getChildrenDoor(i);
			if (child == null)
				continue;
			doors.add(child);
		}
		return doors;
	}
	
	@Override
	public boolean isInMotion() {
		for (int i : toActivate) {
			LittleDoor child = getChildrenDoor(i);
			if (child == null)
				continue;
			if (child.isInMotion())
				return true;
		}
		return false;
	}
	
	@Override
	protected void fillActivateChildren(BitSet set) {
		for (int i : toActivate)
			set.set(i);
	}
	
	public static class LittleDoorActivatorParser extends LittleStructureGuiParser {
		
		public LittleDoorActivatorParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
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
		
		public List<Integer> possibleChildren;
		
		@Override
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			parent.controls.add(new GuiCheckBox("rightclick", CoreControl.translate("gui.door.rightclick"), 0, 123, structure instanceof LittleDoor ? !((LittleDoor) structure).disableRightClick : true));
			
			GuiScrollBox box = new GuiScrollBox("content", 0, 0, 100, 115);
			parent.controls.add(box);
			LittleDoorActivator activator = structure instanceof LittleDoorActivator ? (LittleDoorActivator) structure : null;
			possibleChildren = new ArrayList<>();
			int i = 0;
			int added = 0;
			for (LittlePreviews child : previews.getChildren()) {
				Class clazz = LittleStructureRegistry.getStructureClass(child.getStructureId());
				if (clazz != null && LittleDoor.class.isAssignableFrom(clazz)) {
					box.addControl(new GuiCheckBox("" + i, getDisplayName(child, i), 0, added * 20, activator != null && ArrayUtils.contains(activator.toActivate, i)));
					possibleChildren.add(i);
					added++;
				}
				i++;
			}
			
			updateTimeline();
		}
		
		@CustomEventSubscribe
		public void onChanged(GuiControlChangedEvent event) {
			if (event.source instanceof GuiCheckBox)
				updateTimeline();
		}
		
		public void updateTimeline() {
			AnimationTimeline timeline = new AnimationTimeline(0, new PairList<>());
			List<AnimationEvent> events = new ArrayList<>();
			for (Integer integer : possibleChildren) {
				GuiCheckBox box = (GuiCheckBox) parent.get("" + integer);
				if (box != null && box.value)
					events.add(new ChildActivateEvent(0, integer));
			}
			handler.setTimeline(timeline, events);
		}
		
		@Override
		public LittleStructure parseStructure(LittlePreviews previews) {
			LittleDoorActivator activator = createStructure(LittleDoorActivator.class);
			
			GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
			activator.disableRightClick = !rightclick.value;
			
			GuiScrollBox box = (GuiScrollBox) parent.get("content");
			List<Integer> toActivate = new ArrayList<>();
			for (Integer integer : possibleChildren) {
				GuiCheckBox checkBox = (GuiCheckBox) box.get("" + integer);
				if (checkBox != null && checkBox.value)
					toActivate.add(integer);
			}
			activator.toActivate = new int[toActivate.size()];
			for (int i = 0; i < activator.toActivate.length; i++)
				activator.toActivate[i] = toActivate.get(i);
			
			return activator;
		}
		
	}
}
