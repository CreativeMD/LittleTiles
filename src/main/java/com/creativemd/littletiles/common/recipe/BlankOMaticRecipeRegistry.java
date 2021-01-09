package com.creativemd.littletiles.common.recipe;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorBlock;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorBlocks;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorMaterial;
import com.creativemd.creativecore.common.utils.stack.InfoItem;
import com.creativemd.creativecore.common.utils.stack.InfoItemStack;
import com.creativemd.creativecore.common.utils.stack.InfoOre;
import com.creativemd.creativecore.common.utils.stack.InfoStack;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.block.BlockLittleDyeable.LittleDyeableType;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2.LittleDyeableType2;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BlankOMaticRecipeRegistry {
    
    public static int bleachTotalVolume = 1000;
    private static List<BleachRecipe> recipes = new ArrayList<>();
    private static List<BleachVolume> bleacher = new ArrayList<>();
    
    public static void registerBleachRecipe(BleachRecipe recipe) {
        recipes.add(recipe);
    }
    
    public static void registerBleacher(InfoStack stack, int volume) {
        bleacher.add(new BleachVolume(stack, volume));
    }
    
    public static List<BleachRecipe> getRecipe(ItemStack stack) {
        IBlockState state = BlockUtils.getState(stack);
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);
        List<BleachRecipe> results = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++)
            if (recipes.get(i).is(block, meta))
                results.add(recipes.get(i));
        return results;
    }
    
    public static int getVolume(ItemStack stack) {
        for (int i = 0; i < bleacher.size(); i++)
            if (bleacher.get(i).stack.isInstanceIgnoreSize(stack))
                return bleacher.get(i).volume;
        return 0;
    }
    
    static {
        
        registerBleacher(new InfoOre("dyeWhite"), 4);
        registerBleacher(new InfoOre("dyeLightGray"), 2);
        registerBleacher(new InfoOre("dyeGray"), 1);
        registerBleacher(new InfoOre("woolWhite"), 8);
        registerBleacher(new InfoOre("woolLightGray"), 4);
        registerBleacher(new InfoOre("woolGray"), 2);
        registerBleacher(new InfoItemStack(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta())), 4); // Azure Bluet
        registerBleacher(new InfoItemStack(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta())), 4); // Oxeye Daisy
        registerBleacher(new InfoItemStack(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta())), 4);
        
        registerBleacher(new InfoItem(Items.SUGAR), 1);
        
        registerBleachRecipe(new BleachRecipe(new BlockSelectorBlock(Blocks.COBBLESTONE), 1, LittleTiles.dyeableBlock.get(LittleDyeableType.GRAINY), LittleTiles.dyeableBlock
            .get(LittleDyeableType.GRAINY_BIG)));
        registerBleachRecipe(new BleachRecipe(new BlockSelectorBlock(Blocks.COBBLESTONE), 2, LittleTiles.dyeableBlock.get(LittleDyeableType.GRAINY_LOW)));
        
        registerBleachRecipe(new BleachRecipe(new BlockSelectorBlock(Blocks.STONE), 1, LittleTiles.dyeableBlock2.get(LittleDyeableType2.GRAVEL), LittleTiles.dyeableBlock2
            .get(LittleDyeableType2.SAND), LittleTiles.dyeableBlock2.get(LittleDyeableType2.STONE), LittleTiles.dyeableBlock.get(LittleDyeableType.CLAY)));
        registerBleachRecipe(new BleachRecipe(new BlockSelectorBlock(Blocks.STONE), 2, LittleTiles.dyeableBlock2.get(LittleDyeableType2.CORK)));
        
        BlockSelectorBlocks selector = new BlockSelectorBlocks(Blocks.STONEBRICK, Blocks.BRICK_BLOCK);
        registerBleachRecipe(new BleachRecipe(selector, 1, LittleTiles.dyeableBlock.get(LittleDyeableType.BRICK), LittleTiles.dyeableBlock
            .get(LittleDyeableType.BRICK_BIG), LittleTiles.dyeableBlock.get(LittleDyeableType.BROKEN_BRICK_BIG), LittleTiles.dyeableBlock
                .get(LittleDyeableType.CHISELED), LittleTiles.dyeableBlock.get(LittleDyeableType.STRIPS)));
        registerBleachRecipe(new BleachRecipe(selector, 2, LittleTiles.dyeableBlock.get(LittleDyeableType.BORDERED), LittleTiles.dyeableBlock.get(LittleDyeableType.FLOOR)));
        
        registerBleachRecipe(new BleachRecipe(new BlockSelectorMaterial(Material.ROCK), 4, LittleTiles.dyeableBlock.get(LittleDyeableType.CLEAN)));
    }
    
    public static class BleachVolume {
        
        public InfoStack stack;
        public int volume;
        
        public BleachVolume(InfoStack stack, int volume) {
            this.stack = stack;
            this.volume = volume;
        }
    }
    
    public static class BleachRecipe {
        
        public final BlockSelector selector;
        public final IBlockState[] results;
        public final int needed;
        
        public BleachRecipe(BlockSelector selector, int needed, IBlockState... results) {
            this.selector = selector;
            this.needed = needed;
            this.results = results;
        }
        
        public boolean is(Block block, int meta) {
            if (selector.is(block, meta))
                return true;
            
            for (int i = 0; i < results.length; i++) {
                IBlockState state = results[i];
                if (state.getBlock() == block && state.getBlock().getMetaFromState(state) == meta)
                    return true;
            }
            return false;
        }
        
        public boolean isResult(ItemStack stack) {
            IBlockState otherState = BlockUtils.getState(stack);
            for (int i = 0; i < results.length; i++) {
                IBlockState state = results[i];
                if (state.getBlock() == otherState.getBlock() && state.getBlock().getMetaFromState(state) == otherState.getBlock().getMetaFromState(otherState))
                    return true;
            }
            return false;
        }
    }
    
}
