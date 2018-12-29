package com.creativemd.littletiles.common.structure.type;

import java.util.UUID;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.relative.StructureAbsolute;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LittleAdvancedDoor extends LittleDoorBase {
	
	public LittleAdvancedDoor(LittleStructureType type) {
		super(type);
	}
	
	@Override
	public boolean tryToPlacePreviews(World world, EntityPlayer player, UUID uuid, StructureAbsolute absolute) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StructureAbsolute getAbsoluteAxis() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class LittleAdvancedDoorParser extends LittleStructureGuiParser {
		
		public LittleAdvancedDoorParser(GuiParent parent) {
			super(parent);
		}
		
		@Override
		public void createControls(ItemStack stack, LittleStructure structure) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public LittleStructure parseStructure(ItemStack stack) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
