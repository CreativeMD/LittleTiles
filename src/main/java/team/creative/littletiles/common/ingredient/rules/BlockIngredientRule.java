package team.creative.littletiles.common.ingredient.rules;

import net.minecraft.world.level.block.Block;

@FunctionalInterface
public interface BlockIngredientRule {
    
    public Block getBlockIngredient(Block block);
    
}
