package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.dialog.GuiDialogSignalEvents.GuiSignalEvent;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalStructureComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChild;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetNested;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetParent;

public class RecipeSignalEquationTest extends RecipeTestModule {
    
    @Override
    public void startTest(GuiRecipe recipe, RecipeTestResults results) {}
    
    @Override
    public void test(GuiTreeItemStructure item, RecipeTestResults results) {
        if (item.structure == null)
            return;
        
        GuiSignalEvent[] internal = item.internalOutputs();
        for (int i = 0; i < internal.length; i++) {
            GuiSignalEvent event = internal[i];
            if (event == null)
                continue;
            SignalTargetNotFound error = checkCondition(item, false, i, event.condition, null, event);
            if (error != null)
                results.reportError(error);
        }
        
        for (GuiSignalEvent event : item.externalOutputs()) {
            SignalTargetNotFound error = checkCondition(item, true, event.component.index(), event.condition, null, event);
            if (error != null)
                results.reportError(error);
        }
    }
    
    private SignalTargetNotFound checkCondition(GuiTreeItemStructure item, boolean external, int index, SignalInputCondition condition, SignalTargetNotFound error, GuiSignalEvent event) {
        if (condition == null)
            return error;
        
        SignalTarget target = condition.target();
        if (target != null && !searchForTarget(item, target)) {
            if (error == null)
                error = new SignalTargetNotFound(item, event);
            error.addMissing(target);
        }
        
        for (Iterator<SignalInputCondition> iterator = condition.nested(); iterator.hasNext();)
            error = checkCondition(item, external, index, iterator.next(), error, event);
        
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
        } else if (target instanceof SignalTargetChild c) {
            if (item.structure == null)
                return false;
            if (c.external) {
                if (c.child < 0 || c.child >= item.itemsCount())
                    return false;
                GuiTreeItemStructure child = (GuiTreeItemStructure) item.getItem(c.child);
                if (child.structure instanceof ISignalStructureComponent s)
                    return c.input ? s.getComponentType() == SignalComponentType.INPUT : s.getComponentType() == SignalComponentType.OUTPUT;
                return false;
            }
            return target.getTarget(item.structure) != null;
        }
        return true;
    }
    
    @Override
    public void endTest(GuiRecipe recipe, RecipeTestResults results) {}
    
    public static class SignalTargetNotFound extends RecipeTestError {
        
        private final GuiSignalEvent event;
        
        private final HashSet<String> targets = new HashSet<>();
        private final GuiTreeItemStructure structure;
        
        public SignalTargetNotFound(GuiTreeItemStructure structure, GuiSignalEvent event) {
            this.structure = structure;
            this.event = event;
        }
        
        public void addMissing(SignalTarget target) {
            this.targets.add(target.writeBase());
        }
        
        @Override
        public Component header() {
            return GuiControl.translatable("gui.recipe.test.signal.title", event.component.name());
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
        public void create(GuiRecipe recipe, GuiParent parent, Runnable refresh) {
            List<GuiSignalComponent> inputs = structure.signalSearch.search(true, true, true);
            
            parent.add(new GuiButton("edit", x -> LittleTilesGuiRegistry.SIGNAL_DIALOG.open(parent.getIntegratedParent(), new CompoundTag()).init(inputs, event)).setTranslate(
                "gui.edit"));
            parent.add(new GuiButton("reset", x -> {
                event.reset();
                refresh.run();
            }).setTranslate("gui.clear"));
        }
        
    }
    
}
