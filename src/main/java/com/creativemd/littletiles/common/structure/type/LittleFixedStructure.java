package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class LittleFixedStructure extends LittleStructure {
	
	public LittleFixedStructure(LittleStructureType type) {
		super(type);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		
	}
	
	public static class LittleFixedStructureParser extends LittleStructureGuiParser {
		
		public LittleFixedStructureParser(GuiParent parent) {
			super(parent);
		}
		
		@Override
		public void createControls(ItemStack stack, LittleStructure structure) {
			
		}
		
		@Override
		public LittleStructure parseStructure(ItemStack stack) {
			return createStructure(LittleFixedStructure.class);
		}
		
	}
}
