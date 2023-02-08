package team.creative.littletiles.common.gui.tool.recipe.test;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiPanel;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;

public class GuiRecipeTest extends GuiLayer {
    
    public RecipeTestResults results;
    public GuiRecipe recipe;
    
    public GuiRecipeTest() {
        super("gui.recipe.test", 300, 200);
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiRecipe recipe) {
        this.recipe = recipe;
        this.results = RecipeTest.STANDARD.test(recipe);
        clear();
        init();
    }
    
    @Override
    public void becameTopLayer() {
        if (recipe != null)
            init(recipe);
    }
    
    @Override
    public void create() {
        if (results == null)
            return;
        
        GuiScrollY box = new GuiScrollY();
        add(box.setExpandable());
        
        for (RecipeTestError error : results) {
            GuiPanel content = new GuiPanel(GuiFlow.STACK_Y);
            box.add(content);
            content.add(new GuiLabel("header").setTitle(error.header()));
            content.add(new GuiLabel("desc").setTitle(error.description()));
            GuiParent bottomLine = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER).setVAlign(VAlign.CENTER);
            content.add(bottomLine.setExpandableX());
            error.create(recipe, bottomLine, () -> init(recipe));
        }
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        bottom.addLeft(new GuiButton("refresh", x -> init(recipe)).setTranslate("gui.recipe.test.recheck"));
        bottom.addRight(new GuiButton("okay", x -> closeThisLayer()).setTranslate("gui.okay"));
    }
    
}
