package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
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
            SignalTargetNotFound error = checkCondition(item, false, i, structure.getOutput(i).condition, null);
            if (error != null)
                results.reportError(error);
        }
        
        if (structure.hasExternalOutputs())
            for (Entry<Integer, SignalExternalOutputHandler> entry : structure.externalOutputEntrySet()) {
                SignalTargetNotFound error = checkCondition(item, true, entry.getKey(), entry.getValue().condition, null);
                if (error != null)
                    results.reportError(error);
            }
    }
    
    private SignalTargetNotFound checkCondition(GuiTreeItemStructure item, boolean external, int index, SignalInputCondition condition, SignalTargetNotFound error) {
        if (condition == null)
            return error;
        
        SignalTarget target = condition.target();
        if (target != null && !searchForTarget(item, target)) {
            if (error == null)
                error = new SignalTargetNotFound(item, external, index);
            error.addMissing(target);
        }
        
        for (Iterator<SignalInputCondition> iterator = condition.nested(); iterator.hasNext();)
            error = checkCondition(item, external, index, iterator.next(), error);
        
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
        private final boolean external;
        
        private final HashSet<String> targets = new HashSet<>();
        private final GuiTreeItemStructure structure;
        
        public SignalTargetNotFound(GuiTreeItemStructure structure, boolean external, int index) {
            this.structure = structure;
            this.index = index;
            this.external = external;
        }
        
        public void addMissing(SignalTarget target) {
            this.targets.add(target.writeBase());
        }
        
        @Override
        public Component header() {
            return GuiControl.translatable("gui.recipe.test.signal.title", SignalTarget.name(external, false, index));
        }
        
        @Override
        public Component description() {
            return GuiControl.translatable("gui.recipe.test.signal.desc", String.join(",", targets));
        }
        
        @Override
        public Component tooltip(GuiTreeItemStructure structure) {
            return header();
        }
        
        @Override
        public Iterator<GuiTreeItemStructure> iterator() {
            return new SingleIterator<>(structure);
        }
        
        @Override
        public void create(GuiRecipe recipe, GuiParent parent) {
            parent.add(new GuiButton("edit", x -> {}).setTranslate("gui.edit"));
            parent.add(new GuiButton("reset", x -> {}).setTranslate("gui.reset"));
        }
        
    }
    
}
