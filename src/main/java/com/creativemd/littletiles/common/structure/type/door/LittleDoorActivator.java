package com.creativemd.littletiles.common.structure.type.door;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.utils.type.PairList;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;

import net.minecraft.entity.player.EntityPlayer;

public class LittleDoorActivator extends LittleDoor {
	
	public LittleDoorActivator(LittleStructureType type) {
		super(type);
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
					parent.controls.add(new GuiCheckBox("" + i, getDisplayName(child, i), 0, added * 20, activator != null && activator.doesActivateChild(i)));
					possibleChildren.add(i);
					added++;
				}
				i++;
			}
		}
		
		@Override
		public LittleStructure parseStructure(LittlePreviews previews) {
			LittleDoorActivator activator = createStructure(LittleDoorActivator.class);
			activator.childActivation = new PairList<>();
			for (Integer integer : possibleChildren) {
				GuiCheckBox box = (GuiCheckBox) parent.get("" + integer);
				if (box != null && box.value)
					activator.childActivation.add(integer, 0);
			}
			return activator;
		}
		
	}
	
	@Override
	public EntityAnimation openDoor(EntityPlayer player, UUIDSupplier uuid, DoorOpeningResult result) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
