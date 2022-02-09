package team.creative.littletiles.common.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import team.creative.creativecore.common.util.filter.Filter;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.registry.LittleMCBlock;
import team.creative.littletiles.common.ingredient.rules.BlockIngredientRule;
import team.creative.littletiles.common.ingredient.rules.IngredientRules;

public class IngredientUtils {
    
    public static BlockIngredientEntry getBlockIngredient(LittleBlock block, double value) {
        if (block instanceof LittleMCBlock) {
            Block mcBlock = ((LittleMCBlock) block).block;
            for (Pair<Filter<Block>, BlockIngredientRule> pair : IngredientRules.getBlockRules())
                if (pair.key.is(mcBlock))
                    return new BlockIngredientEntry(LittleBlockRegistry.get(pair.value.getBlockIngredient(mcBlock)), value);
        }
        return create(block, value);
    }
    
    @Deprecated
    /** Don't use it, except if it's about BlockIngredientRule */
    public static BlockIngredientEntry create(LittleBlock block, double value) {
        return new BlockIngredientEntry(block, value);
    }
    
    public static BlockIngredientEntry loadBlockIngredient(CompoundTag nbt) {
        LittleBlock block = LittleBlockRegistry.get(nbt.getString("block"));
        if (nbt.getDouble("volume") > 0)
            return new BlockIngredientEntry(block, nbt.getDouble("volume"));
        return null;
    }
}
