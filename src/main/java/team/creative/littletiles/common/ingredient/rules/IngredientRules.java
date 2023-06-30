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
        registerBlockRule(BlockFilters.block(LittleTilesRegistry.FLOWING_WATER.get()), x -> LittleTilesRegistry.WATER.get());
        registerBlockRule(BlockFilters.block(LittleTilesRegistry.FLOWING_LAVA.get()), x -> LittleTilesRegistry.LAVA.get());
        registerBlockRule(BlockFilters.block(LittleTilesRegistry.WHITE_FLOWING_LAVA.get()), x -> LittleTilesRegistry.WHITE_LAVA.get());
    }
}
