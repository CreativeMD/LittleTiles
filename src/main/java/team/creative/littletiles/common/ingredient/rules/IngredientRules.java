package team.creative.littletiles.common.ingredient.rules;

import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorAnd;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorBlock;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorClass;
import com.creativemd.creativecore.common.utils.sorting.BlockSelector.BlockSelectorProperty;
import com.creativemd.littletiles.common.block.BlockLittleDyeable;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.creativemd.littletiles.common.util.ingredient.rules.BlockIngredientRule.BlockIngredientRuleFixedBlock;
import com.creativemd.littletiles.common.util.ingredient.rules.BlockIngredientRule.BlockIngredientRuleFixedMeta;
import com.creativemd.littletiles.common.util.ingredient.rules.BlockIngredientRule.BlockIngredientRuleMappedState;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.filter.block.BlockFilters;
import team.creative.creativecore.common.util.type.PairList;
import team.creative.littletiles.LittleTiles;

public class IngredientRules {
    
    private static PairList<Filter<Block>, BlockIngredientRule> blockRules = new PairList<>();
    
    public static void registerBlockRule(Filter<Block> filter, BlockIngredientRule rule) {
        blockRules.add(filter, rule);
    }
    
    public static PairList<Filter<Block>, BlockIngredientRule> getBlockRules() {
        return blockRules;
    }
    
    public static void loadRules() {
        registerBlockRule(BlockFilters
                .block(LittleTiles.flowingWater), new BlockIngredientRuleFixedBlock(LittleTiles.dyeableBlockTransparent, BlockLittleDyeableTransparent.LittleDyeableTransparent.WATER
                        .ordinal()));
        registerBlockRule(new BlockSelectorBlock(LittleTiles.whiteFlowingWater), new BlockIngredientRuleFixedBlock(LittleTiles.dyeableBlockTransparent, BlockLittleDyeableTransparent.LittleDyeableTransparent.WHITE_WATER
                .ordinal()));
        registerBlockRule(new BlockSelectorBlock(LittleTiles.flowingLava), new BlockIngredientRuleFixedBlock(LittleTiles.dyeableBlock, BlockLittleDyeable.LittleDyeableType.LAVA
                .ordinal()));
        registerBlockRule(new BlockSelectorBlock(LittleTiles.whiteFlowingLava), new BlockIngredientRuleFixedBlock(LittleTiles.dyeableBlock, BlockLittleDyeable.LittleDyeableType.WHITE_LAVA
                .ordinal()));
        
        registerBlockRule(BlockFilters.and(BlockFilters.instance(RotatedPillarBlock.class), BlockFilters.instance(HorizontalDirectionalBlock.class), rule);
    }
}
