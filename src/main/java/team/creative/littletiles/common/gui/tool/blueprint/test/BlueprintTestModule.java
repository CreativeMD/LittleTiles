package team.creative.littletiles.common.gui.tool.blueprint.test;

import team.creative.littletiles.common.gui.tool.blueprint.GuiBlueprint;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;

public abstract class BlueprintTestModule {
    
    public abstract void startTest(GuiBlueprint blueprint, BlueprintTestResults results);
    
    public abstract void test(GuiTreeItemStructure item, BlueprintTestResults results);
    
    public abstract void endTest(GuiBlueprint blueprint, BlueprintTestResults results);
    
}
