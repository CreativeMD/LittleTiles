package team.creative.littletiles.common.gui.tool.recipe;

import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class GuiRecipeMove extends GuiLayer {
    
    public GuiRecipe recipe;
    
    public GuiRecipeMove() {
        super("gui.recipe.move", 300, 200);
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiRecipe recipe) {
        this.recipe = recipe;
        clear();
        init();
    }
    
    @Override
    public void create() {
        // TODO Auto-generated method stub
        
    }
    
}
