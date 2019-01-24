package com.creativemd.littletiles.common.utils.ingredients;

import net.minecraft.block.Block;

public abstract class BlockIngredientRule {
	
	public abstract BlockIngredient getBlockIngredient(Block block, int meta, double value);
	
	public static class BlockIngredientRuleFixedMeta extends BlockIngredientRule {
		
		public int meta;
		
		public BlockIngredientRuleFixedMeta(int meta) {
			this.meta = meta;
		}
		
		@Override
		public BlockIngredient getBlockIngredient(Block block, int meta, double value) {
			return IngredientUtils.getBlockIngredient(block, this.meta, value);
		}
		
	}
	
	public static class BlockIngredientRuleFixedBlock extends BlockIngredientRule {
		
		public Block block;
		public int meta;
		
		public BlockIngredientRuleFixedBlock(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}
		
		@Override
		public BlockIngredient getBlockIngredient(Block block, int meta, double value) {
			return IngredientUtils.getBlockIngredient(this.block, this.meta, value);
		}
	}
}
