package team.creative.littletiles.common.structure.registry;

import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.tile.group.LittleGroup;

public interface IStructureIngredientRule {
    
    public void add(LittleGroup group, LittleIngredients ingredients);
    
}
