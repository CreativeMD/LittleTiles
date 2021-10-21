package team.creative.littletiles.common.ingredient.rules;

import net.minecraft.world.level.block.Block;
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
        registerBlockRule(BlockFilters.block(LittleTiles.FLOWING_WATER), x -> LittleTiles.WATER);
        registerBlockRule(BlockFilters.block(LittleTiles.WHITE_FLOWING_WATER), x -> LittleTiles.WHITE_WATER);
        registerBlockRule(BlockFilters.block(LittleTiles.FLOWING_LAVA), x -> LittleTiles.LAVA);
        registerBlockRule(BlockFilters.block(LittleTiles.WHITE_FLOWING_LAVA), x -> LittleTiles.WHITE_LAVA);
    }
}
