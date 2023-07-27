package com.creativemd.littletiles.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class ItemBlockFlowingLava extends ItemBlock {
    
    public ItemBlockFlowingLava(Block block, ResourceLocation location) {
        super(block);
        setTranslationKey(location.getPath());
        setHasSubtypes(true);
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        String name = "default";
        if (stack.getMetadata() < BlockLTFlowingLava.DIRECTION.getAllowedValues().size())
            name = EnumFacing.byIndex(stack.getMetadata()).getName();
        return getTranslationKey() + "." + name;
    }
    
    @Override
    public int getMetadata(int meta) {
        return meta;
    }
    
}
