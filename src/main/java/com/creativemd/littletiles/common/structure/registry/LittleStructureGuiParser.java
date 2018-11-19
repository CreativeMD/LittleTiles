package com.creativemd.littletiles.common.structure.registry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.littletiles.common.structure.LittleStructure;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructureGuiParser {
	
	public final GuiParent parent;
	
	public LittleStructureGuiParser(GuiParent parent) {
		this.parent = parent;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract void createControls(ItemStack stack, @Nullable LittleStructure structure);
	
	@SideOnly(Side.CLIENT)
	public abstract LittleStructure parseStructure(ItemStack stack);
	
	public <T extends LittleStructure> T createStructure(Class<T> structureClass) {
		LittleStructureType type = LittleStructureRegistry.getStructureType(structureClass);
		if (type == null)
			throw new RuntimeException("Could find structure for " + structureClass);
		return (T) type.createStructure();
	}
	
}
