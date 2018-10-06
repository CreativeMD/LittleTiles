package com.creativemd.littletiles.common.structure;

import com.creativemd.creativecore.gui.container.GuiParent;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructureParser<T extends LittleStructure> {
	
	public final String structureID;
	public final GuiParent parent;
	
	public LittleStructureParser(String id, GuiParent parent) {
		this.structureID = id;
		this.parent = parent;
	}
	
	@SideOnly(Side.CLIENT)
	public abstract void createControls(ItemStack stack, LittleStructure structure);
	
	@SideOnly(Side.CLIENT)
	public abstract T parseStructure(ItemStack stack);
}
