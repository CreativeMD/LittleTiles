package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.utils.animation.AnimationTimeline;
import com.creativemd.littletiles.common.utils.animation.event.AnimationEvent;
import com.creativemd.littletiles.common.utils.animation.event.ChildActivateEvent;
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
	
	@Override
	public EntityAnimation openDoor(@Nullable EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result) {
		for (int i : toActivate)
			((LittleDoor) children.get(i).getStructure(getWorld())).openDoor(player, uuid, result);
		return null;
	}
	
	@Override
	public int getCompleteDuration() {
		int duration = 0;
		for (int i : toActivate)
			duration = Math.max(duration, ((LittleDoor) children.get(i).getStructure(getWorld())).getCompleteDuration());
		return duration;
	}
	
	@Override
	public List<LittleDoor> collectDoorsToCheck() {
		List<LittleDoor> doors = new ArrayList<>();
		for (int i : toActivate)
			doors.add((LittleDoor) children.get(i).getStructure(getWorld()));
		return doors;
	}
	
	@Override
	public boolean isInMotion() {
		for (int i : toActivate)
			if (((LittleDoor) children.get(i).getStructure(getWorld())).isInMotion())
				return true;
		return false;
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
			GuiScrollBox box = new GuiScrollBox("content", 0, 0, 100, 120);
			parent.controls.add(box);
			LittleDoorActivator activator = structure instanceof LittleDoorActivator ? (LittleDoorActivator) structure : null;
			possibleChildren = new ArrayList<>();
			int i = 0;
			int added = 0;
			for (LittlePreviews child : previews.getChildren()) {
				if (LittleDoor.class.isAssignableFrom(LittleStructureRegistry.getStructureClass(child.getStructureId()))) {
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
			
			List<LittlePreviews> previewChildren = previews.getChildren();
			
			if (!previewChildren.isEmpty()) {
				for (int i = 0; i < previewChildren.size(); i++)
					if (ArrayUtils.contains(activator.toActivate, i))
						previewChildren.get(i).getStructureData().setBoolean("activateParent", true);
					else
						previewChildren.get(i).getStructureData().removeTag("activateParent");
			}
			return activator;
		}
		
	}
}
