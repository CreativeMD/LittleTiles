package com.creativemd.littletiles.common.structure;

import com.creativemd.creativecore.gui.container.GuiParent;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class LittleFixedStructure extends LittleStructure {

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {

	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {

	}

	public static class LittleFixedStructureParser extends LittleStructureParser<LittleFixedStructure> {

		public LittleFixedStructureParser(String id, GuiParent parent) {
			super(id, parent);
		}

		@Override
		public void createControls(ItemStack stack, LittleStructure structure) {

		}

		@Override
		public LittleFixedStructure parseStructure(ItemStack stack) {
			return new LittleFixedStructure();
		}

	}
}
