package team.creative.littletiles.common.ingredient.rules;

import net.minecraft.world.level.block.Block;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;

public abstract class BlockIngredientRule {
    
    public abstract BlockIngredientEntry getBlockIngredient(Block block, double value);
    
}
