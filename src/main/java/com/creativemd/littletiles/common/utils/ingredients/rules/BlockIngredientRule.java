package com.creativemd.littletiles.common.utils.ingredients.rules;

import com.creativemd.littletiles.common.utils.ingredients.BlockIngredientEntry;
import com.creativemd.littletiles.common.utils.ingredients.IngredientUtils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public abstract class BlockIngredientRule {
	
	public abstract BlockIngredientEntry getBlockIngredient(Block block, int meta, double value);
	
	public static class BlockIngredientRuleFixedMeta extends BlockIngredientRule {
		
		public int meta;
		
		public BlockIngredientRuleFixedMeta(int meta) {
			this.meta = meta;
		}
		
		@Override
		public BlockIngredientEntry getBlockIngredient(Block block, int meta, double value) {
			return IngredientUtils.create(block, this.meta, value);
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
		public BlockIngredientEntry getBlockIngredient(Block block, int meta, double value) {
			return IngredientUtils.create(this.block, this.meta, value);
		}
	}
	
	public static abstract class BlockIngredientRuleMappedState extends BlockIngredientRule {
		
		@Override
		public BlockIngredientEntry getBlockIngredient(Block block, int meta, double value) {
			return IngredientUtils.create(block, getMeta(block.getStateFromMeta(meta), value), value);
		}
		
		public abstract int getMeta(IBlockState state, double value);
		
	}
}
