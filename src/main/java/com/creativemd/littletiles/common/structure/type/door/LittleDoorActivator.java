package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;

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
			((LittleDoor) children.get(toActivate[i]).getStructure(getWorld())).openDoor(player, uuid, result);
		return null;
	}
	
	@Override
	public int getCompleteDuration() {
		int duration = 0;
		for (int i : toActivate)
			duration = Math.max(duration, ((LittleDoor) children.get(toActivate[i]).getStructure(getWorld())).getCompleteDuration());
		return duration;
	}
	
	@Override
	public List<LittleDoor> collectDoorsToCheck() {
		List<LittleDoor> doors = new ArrayList<>();
		for (int i : toActivate)
			doors.add((LittleDoor) children.get(toActivate[i]).getStructure(getWorld()));
		return doors;
	}
	
	@Override
	public boolean isInMotion() {
		for (int i : toActivate)
			if (((LittleDoor) children.get(toActivate[i]).getStructure(getWorld())).isInMotion())
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
			LittleDoorActivator activator = structure instanceof LittleDoorActivator ? (LittleDoorActivator) structure : null;
			possibleChildren = new ArrayList<>();
			int i = 0;
			int added = 0;
			for (LittlePreviews child : previews.getChildren()) {
				if (LittleDoor.class.isAssignableFrom(LittleStructureRegistry.getStructureClass(child.getStructureId()))) {
					parent.controls.add(new GuiCheckBox("" + i, getDisplayName(child, i), 0, added * 20, activator != null && ArrayUtils.contains(activator.toActivate, i)));
					possibleChildren.add(i);
					added++;
				}
				i++;
			}
		}
		
		@Override
		public LittleStructure parseStructure(LittlePreviews previews) {
			LittleDoorActivator activator = createStructure(LittleDoorActivator.class);
			List<Integer> toActivate = new ArrayList<>();
			for (Integer integer : possibleChildren) {
				GuiCheckBox box = (GuiCheckBox) parent.get("" + integer);
				if (box != null && box.value)
					toActivate.add(integer);
			}
			activator.toActivate = new int[toActivate.size()];
			for (int i : activator.toActivate)
				activator.toActivate[i] = toActivate.get(i);
			return activator;
		}
		
	}
}
