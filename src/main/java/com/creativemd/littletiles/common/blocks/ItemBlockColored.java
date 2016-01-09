package com.creativemd.littletiles.common.blocks;

import com.creativemd.littletiles.LittleTiles;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockColored extends ItemBlock{

	public ItemBlockColored(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
    {
		String name = "default";
		if(stack.getItemDamage() < BlockLTColored.subBlocks.length)
			name = BlockLTColored.subBlocks[stack.getItemDamage()];
		return getUnlocalizedName() + "." + name;
    }
	
	@Override
    public int getMetadata(int meta)
    {
		return meta;
    }

}
