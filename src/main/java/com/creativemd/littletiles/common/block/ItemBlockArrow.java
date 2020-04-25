package com.creativemd.littletiles.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class ItemBlockArrow extends ItemBlock {
	
	public ItemBlockArrow(Block block, ResourceLocation location) {
		super(block);
		setUnlocalizedName(location.getResourcePath());
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(int meta) {
		return meta;
	}
	
}
