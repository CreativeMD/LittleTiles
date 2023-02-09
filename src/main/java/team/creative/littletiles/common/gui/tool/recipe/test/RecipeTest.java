package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;

public class RecipeTest {
    
    public static final RecipeOverlapTest OVERLAP_TEST = new RecipeOverlapTest();
    public static final RecipeSignalEquationTest SIGNAL_TEST = new RecipeSignalEquationTest();
    public static final RecipeTest STANDARD = new RecipeTest(Arrays.asList(OVERLAP_TEST, SIGNAL_TEST));
    
    public static RecipeTestResults testModule(GuiRecipe recipe, RecipeTestModule module) {
        resetBeforeTest(recipe);
        
        RecipeTestResults results = new RecipeTestResults();
        
        module.startTest(recipe, results);
        
        for (GuiTreeItem child : recipe.tree.root().items())
            testStructure(module, (GuiTreeItemStructure) child, results);
        
        module.endTest(recipe, results);
        
        return results;
    }
    
    protected static void testStructure(RecipeTestModule module, GuiTreeItemStructure item, RecipeTestResults results) {
        module.test(item, results);
        
        for (GuiTreeItem child : item.items())
            testStructure(module, (GuiTreeItemStructure) child, results);
    }
    
    public static void resetBeforeTest(GuiRecipe recipe) {
        recipe.storage.resetOverlap();
    }
    
    private final List<RecipeTestModule> modules;
    
    public RecipeTest(List<RecipeTestModule> modules) {
        this.modules = new ArrayList<>(modules);
    }
    
    public void addModule(RecipeTestModule module) {
        modules.add(module);
    }
    
    public RecipeTestResults test(GuiRecipe recipe) {
        resetBeforeTest(recipe);
        
        RecipeTestResults results = new RecipeTestResults();
        
        for (RecipeTestModule module : modules)
            module.startTest(recipe, results);
        
        for (GuiTreeItem child : recipe.tree.root().items())
            testStructure((GuiTreeItemStructure) child, results);
        
        for (RecipeTestModule module : modules)
            module.endTest(recipe, results);
        
        return results;
    }
    
    protected void testStructure(GuiTreeItemStructure item, RecipeTestResults results) {
        for (RecipeTestModule module : modules)
            module.test(item, results);
        
        for (GuiTreeItem child : item.items())
            testStructure((GuiTreeItemStructure) child, results);
    }
    
}
