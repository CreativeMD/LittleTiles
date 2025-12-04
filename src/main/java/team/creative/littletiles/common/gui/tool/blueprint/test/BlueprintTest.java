package team.creative.littletiles.common.gui.tool.blueprint.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import team.creative.creativecore.common.gui.control.tree.GuiTreeItem;
import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprint;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;

public class BlueprintTest {
    
    public static final BlueprintOverlapTest OVERLAP_TEST = new BlueprintOverlapTest();
    public static final BlueprintSignalEquationTest SIGNAL_TEST = new BlueprintSignalEquationTest();
    public static final BlueprintTest STANDARD = new BlueprintTest(Arrays.asList(OVERLAP_TEST, SIGNAL_TEST));
    
    public static BlueprintTestResults testModule(GuiBlueprint blueprint, BlueprintTestModule module) {
        resetBeforeTest(blueprint);
        
        BlueprintTestResults results = new BlueprintTestResults();
        
        module.startTest(blueprint, results);
        
        for (GuiTreeItem child : blueprint.tree.root().items())
            testStructure(module, (GuiTreeItemStructure) child, results);
        
        module.endTest(blueprint, results);
        
        return results;
    }
    
    protected static void testStructure(BlueprintTestModule module, GuiTreeItemStructure item, BlueprintTestResults results) {
        module.test(item, results);
        
        for (GuiTreeItem child : item.items())
            testStructure(module, (GuiTreeItemStructure) child, results);
    }
    
    public static void resetBeforeTest(GuiBlueprint blueprint) {
        blueprint.storage.resetOverlap();
    }
    
    private final List<BlueprintTestModule> modules;
    
    public BlueprintTest(List<BlueprintTestModule> modules) {
        this.modules = new ArrayList<>(modules);
    }
    
    public void addModule(BlueprintTestModule module) {
        modules.add(module);
    }
    
    public BlueprintTestResults test(GuiBlueprint blueprint) {
        resetBeforeTest(blueprint);
        
        BlueprintTestResults results = new BlueprintTestResults();
        
        for (BlueprintTestModule module : modules)
            module.startTest(blueprint, results);
        
        for (GuiTreeItem child : blueprint.tree.root().items())
            testStructure((GuiTreeItemStructure) child, results);
        
        for (BlueprintTestModule module : modules)
            module.endTest(blueprint, results);
        
        return results;
    }
    
    protected void testStructure(GuiTreeItemStructure item, BlueprintTestResults results) {
        for (BlueprintTestModule module : modules)
            module.test(item, results);
        
        for (GuiTreeItem child : item.items())
            testStructure((GuiTreeItemStructure) child, results);
    }
    
}
