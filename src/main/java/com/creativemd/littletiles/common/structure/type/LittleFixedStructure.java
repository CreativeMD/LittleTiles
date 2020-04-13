package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

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
		
		public LittleFixedStructureParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			
		}
		
		@Override
		public LittleStructure parseStructure(LittlePreviews previews) {
			return createStructure(LittleFixedStructure.class);
		}
		
	}
}
