package com.creativemd.littletiles.common.util.ingredient.rules;

import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.IngredientUtils;

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
            return IngredientUtils.create(block, getMeta(BlockUtils.getState(block, meta), value), value);
        }
        
        public abstract int getMeta(IBlockState state, double value);
        
    }
}
