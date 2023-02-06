package team.creative.littletiles.common.gui.tool.recipe;

import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class GuiRecipeMerge extends GuiLayer {
    
    public GuiRecipe recipe;
    
    public GuiRecipeMerge() {
        super("gui.recipe.merge", 300, 200);
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
