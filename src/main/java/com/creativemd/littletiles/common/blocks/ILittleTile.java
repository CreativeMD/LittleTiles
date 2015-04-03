package com.creativemd.littletiles.common.blocks;

import net.minecraft.item.ItemStack;

import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;

public interface ILittleTile {
	
	public boolean canSplit(LittleTile tile);
	
	public LittleTile getLittleTile(ItemStack stack);
	
	public LittleTileSize getSize(ItemStack stack);
}
