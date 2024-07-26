package team.creative.littletiles.common.ingredient.rules;

import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.filter.premade.BlockFilters;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.LittleTilesRegistry;

public class IngredientRules {
    
    private static PairList<Filter<Block>, BlockIngredientRule> blockRules = new PairList<>();
    
    public static void registerBlockRule(Filter<Block> filter, BlockIngredientRule rule) {
        blockRules.add(filter, rule);
    }
    
    public static PairList<Filter<Block>, BlockIngredientRule> getBlockRules() {
        return blockRules;
    }
    
    public static void loadRules() {
        registerBlockRule(BlockFilters.block(LittleTilesRegistry.FLOWING_WATER.value()), x -> LittleTilesRegistry.WATER.value());
        registerBlockRule(BlockFilters.block(LittleTilesRegistry.FLOWING_LAVA.value()), x -> LittleTilesRegistry.LAVA.value());
        registerBlockRule(BlockFilters.block(LittleTilesRegistry.WHITE_FLOWING_LAVA.value()), x -> LittleTilesRegistry.WHITE_LAVA.value());
    }
}
