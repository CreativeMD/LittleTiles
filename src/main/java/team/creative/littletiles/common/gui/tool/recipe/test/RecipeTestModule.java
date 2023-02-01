package team.creative.littletiles.common.gui.tool.recipe.test;

import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;

public abstract class RecipeTestModule {
    
    public abstract void startTest(GuiRecipe recipe, RecipeTestResults results);
    
    public abstract void test(GuiTreeItemStructure item, RecipeTestResults results);
    
    public abstract void endTest(GuiRecipe recipe, RecipeTestResults results);
    
}
