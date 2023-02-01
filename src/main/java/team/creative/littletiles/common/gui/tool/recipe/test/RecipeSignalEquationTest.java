package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChild;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetNested;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetParent;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;

public class RecipeSignalEquationTest extends RecipeTestModule {
    
    @Override
    public void startTest(GuiRecipe recipe, RecipeTestResults results) {}
    
    @Override
    public void test(GuiTreeItemStructure item, RecipeTestResults results) {
        if (item.structure == null)
            return;
        
        LittleStructure structure = item.structure;
        
        for (int i = 0; i < structure.internalOutputCount(); i++) {
            SignalTargetNotFound error = checkCondition(item, true, i, structure.getOutput(i).condition, null);
            if (error != null)
                results.reportError(error);
        }
        
        for (Entry<Integer, SignalExternalOutputHandler> entry : structure.externalOutputHandlersEntrySet()) {
            SignalTargetNotFound error = checkCondition(item, false, entry.getKey(), entry.getValue().condition, null);
            if (error != null)
                results.reportError(error);
        }
    }
    
    private SignalTargetNotFound checkCondition(GuiTreeItemStructure item, boolean internal, int index, SignalInputCondition condition, SignalTargetNotFound error) {
        if (condition == null)
            return error;
        
        SignalTarget target = condition.target();
        if (target != null && !searchForTarget(item, target)) {
            if (error == null)
                error = new SignalTargetNotFound(internal, index);
            error.addMissing(target);
        }
        
        for (Iterator<SignalInputCondition> iterator = condition.nested(); iterator.hasNext();)
            error = checkCondition(item, internal, index, iterator.next(), error);
        
        return error;
    }
    
    private boolean searchForTarget(GuiTreeItemStructure item, SignalTarget target) {
        if (target instanceof SignalTargetParent targetParent) {
            GuiTreeItem parent = item.getParentItem();
            if (parent instanceof GuiTreeItemStructure p)
                return searchForTarget(p, targetParent.subTarget);
            return false;
        } else if (target instanceof SignalTargetNested nested) {
            if (nested.child < 0 || nested.child >= item.itemsCount())
                return false;
            return searchForTarget((GuiTreeItemStructure) item.getItem(nested.child), nested.subTarget);
        } else if (target instanceof SignalTargetChild) {
            if (item.structure == null)
                return false;
            return target.getTarget(item.structure) != null;
        }
        return true;
    }
    
    @Override
    public void endTest(GuiRecipe recipe, RecipeTestResults results) {}
    
    public static class SignalTargetNotFound extends RecipeTestError {
        
        private final int index;
        private final boolean internal;
        
        private final HashSet<String> targets = new HashSet<>();
        
        public SignalTargetNotFound(boolean internal, int index) {
            this.index = index;
            this.internal = internal;
        }
        
        public void addMissing(SignalTarget target) {
            this.targets.add(target.writeBase());
        }
        
    }
    
}
