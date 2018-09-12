package com.creativemd.littletiles.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemBlockColored extends ItemBlock {

	public ItemBlockColored(Block block, ResourceLocation location) {
		super(block);
		setUnlocalizedName(location.getResourcePath());
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String name = "default";
		if (stack.getItemDamage() < BlockLTColored.EnumType.values().length)
			name = BlockLTColored.EnumType.values()[stack.getItemDamage()].getName();
		return getUnlocalizedName() + "." + name;
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

}
