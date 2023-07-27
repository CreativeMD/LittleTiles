package com.creativemd.littletiles.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemBlockTransparentColored extends ItemBlock {
    
    public ItemBlockTransparentColored(Block block, ResourceLocation location) {
        super(block);
        setTranslationKey(location.getPath());
        setHasSubtypes(true);
    }
    
    @Override
    public String getTranslationKey(ItemStack stack) {
        String name = "default";
        if (stack.getMetadata() < BlockLittleDyeableTransparent.LittleDyeableTransparent.values().length)
            name = BlockLittleDyeableTransparent.LittleDyeableTransparent.values()[stack.getMetadata()].getName();
        return getTranslationKey() + "." + name;
    }
    
    @Override
    public int getMetadata(int meta) {
        return meta;
    }
    
}
