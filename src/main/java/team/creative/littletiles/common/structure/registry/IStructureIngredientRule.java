package team.creative.littletiles.common.structure.registry;

import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.ingredient.LittleIngredients;

public interface IStructureIngredientRule {
    
    public void add(LittleGroup group, LittleIngredients ingredients);
    
}
