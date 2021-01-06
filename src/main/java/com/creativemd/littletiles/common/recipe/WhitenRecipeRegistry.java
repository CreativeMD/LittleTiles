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
import com.creativemd.littletiles.common.block.BlockLTColored.EnumType;
import com.creativemd.littletiles.common.block.BlockLTColored2.ColoredEnumType2;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class WhitenRecipeRegistry {
	
	public static int whitenerTotalVolume = 1000;
	private static List<WhitenRecipe> recipes = new ArrayList<>();
	private static List<WhitenVolume> whitener = new ArrayList<>();
	
	public static void registerWhitenRecipe(WhitenRecipe recipe) {
		recipes.add(recipe);
	}
	
	public static void registerWhitener(InfoStack stack, int volume) {
		whitener.add(new WhitenVolume(stack, volume));
	}
	
	public static List<WhitenRecipe> getRecipe(ItemStack stack) {
		IBlockState state = BlockUtils.getState(stack);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		List<WhitenRecipe> results = new ArrayList<>();
		for (int i = 0; i < recipes.size(); i++)
			if (recipes.get(i).is(block, meta))
				results.add(recipes.get(i));
		return results;
	}
	
	public static int getVolume(ItemStack stack) {
		for (int i = 0; i < whitener.size(); i++)
			if (whitener.get(i).stack.isInstanceIgnoreSize(stack))
				return whitener.get(i).volume;
		return 0;
	}
	
	static {
		
		registerWhitener(new InfoOre("dyeWhite"), 4);
		registerWhitener(new InfoOre("dyeLightGray"), 2);
		registerWhitener(new InfoOre("dyeGray"), 1);
		registerWhitener(new InfoOre("woolWhite"), 8);
		registerWhitener(new InfoOre("woolLightGray"), 4);
		registerWhitener(new InfoOre("woolGray"), 2);
		registerWhitener(new InfoItemStack(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta())), 4); // Azure Bluet
		registerWhitener(new InfoItemStack(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta())), 4); // Oxeye Daisy
		registerWhitener(new InfoItemStack(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta())), 4);
		
		registerWhitener(new InfoItem(Items.SUGAR), 1);
		
		registerWhitenRecipe(new WhitenRecipe(new BlockSelectorBlock(Blocks.COBBLESTONE), 1, LittleTiles.coloredBlock.get(EnumType.grainy), LittleTiles.coloredBlock.get(EnumType.grainy_big)));
		registerWhitenRecipe(new WhitenRecipe(new BlockSelectorBlock(Blocks.COBBLESTONE), 2, LittleTiles.coloredBlock.get(EnumType.grainy_low)));
		
		registerWhitenRecipe(new WhitenRecipe(new BlockSelectorBlock(Blocks.STONE), 1, LittleTiles.coloredBlock2.get(ColoredEnumType2.gravel), LittleTiles.coloredBlock2.get(ColoredEnumType2.sand), LittleTiles.coloredBlock2.get(ColoredEnumType2.stone), LittleTiles.coloredBlock.get(EnumType.clay)));
		registerWhitenRecipe(new WhitenRecipe(new BlockSelectorBlock(Blocks.STONE), 2, LittleTiles.coloredBlock2.get(ColoredEnumType2.wood)));
		
		BlockSelectorBlocks selector = new BlockSelectorBlocks(Blocks.STONEBRICK, Blocks.BRICK_BLOCK);
		registerWhitenRecipe(new WhitenRecipe(selector, 1, LittleTiles.coloredBlock.get(EnumType.brick), LittleTiles.coloredBlock.get(EnumType.brick_big), LittleTiles.coloredBlock.get(EnumType.broken_brick_big), LittleTiles.coloredBlock.get(EnumType.structured), LittleTiles.coloredBlock.get(EnumType.plank)));
		registerWhitenRecipe(new WhitenRecipe(selector, 2, LittleTiles.coloredBlock.get(EnumType.bordered), LittleTiles.coloredBlock.get(EnumType.floor)));
		
		registerWhitenRecipe(new WhitenRecipe(new BlockSelectorMaterial(Material.ROCK), 4, LittleTiles.coloredBlock.get(EnumType.clean)));
	}
	
	public static class WhitenVolume {
		
		public InfoStack stack;
		public int volume;
		
		public WhitenVolume(InfoStack stack, int volume) {
			this.stack = stack;
			this.volume = volume;
		}
	}
	
	public static class WhitenRecipe {
		
		public final BlockSelector selector;
		public final IBlockState[] results;
		public final int needed;
		
		public WhitenRecipe(BlockSelector selector, int needed, IBlockState... results) {
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
