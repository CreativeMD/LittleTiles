package com.creativemd.littletiles.common.blocks;

import net.minecraft.item.ItemStack;

import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

public interface ILittleTile {
	
	public boolean canSplit(LittleTile tile);
	
	public LittleTile getLittleTile(ItemStack stack);
	
	/**Return null if this LittleTile does not have a fixed size*/
	public LittleTileVec getSize(ItemStack stack);
	
	/**Return null if this LittleTile does not have a fixed pos*/
	public LittleTileVec getPos(ItemStack stack);
}
