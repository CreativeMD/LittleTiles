package team.creative.littletiles.common.gui.signal;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;

public class GuiComponentSearch {
    
    public GuiTreeItemStructure item;
    
    public GuiComponentSearch(GuiTreeItemStructure item) {
        this.item = item;
    }
    
    public List<GuiSignalComponent> search(boolean input, boolean output, boolean includeRelations) {
        List<GuiSignalComponent> list = new ArrayList<>();
        if (input)
            gatherInputs(item, "", list, includeRelations, true);
        if (output)
            gatherOutputs(item, "", list, includeRelations, true);
        return list;
    }
    
    public GuiSignalComponent[] internalOutputs() {
        LittleStructureType type = item.getStructureType();
        if (type != null && type.outputs != null) {
            GuiSignalComponent[] components = new GuiSignalComponent[type.outputs.size()];
            for (int i = 0; i < type.outputs.size(); i++)
                components[i] = new GuiSignalComponent("b" + i, item.getTitle(), type.outputs.get(i), false, false, i);
            return components;
        }
        return null;
    }
    
    public List<GuiSignalComponent> externalOutputs() {
        List<GuiSignalComponent> components = new ArrayList<>();
        addExternalOutputs(item, "", components, false);
        return components;
    }
    
    protected void addInput(GuiTreeItemStructure item, String prefix, List<GuiSignalComponent> list, boolean includeRelations) {
        LittleStructureType type = item.getStructureType();
        if (type != null && type.inputs != null)
            for (int i = 0; i < type.inputs.size(); i++)
                list.add(new GuiSignalComponent(prefix + "a" + i, item.getTitle(), type.inputs.get(i), true, false, i));
            
        int i = 0;
        for (GuiTreeItem child : item.items()) {
            if (child == this.item)
                continue;
            GuiTreeItemStructure childStructure = (GuiTreeItemStructure) child;
            if (childStructure.structure == null)
                continue;
            
            String name = childStructure.structure.name;
            if (childStructure.structure instanceof ISignalComponent com && com.getComponentType() == SignalComponentType.INPUT)
                list.add(new GuiSignalComponent(prefix + "i" + i, item.getTitle() + "." + (name != null ? name : "i" + i), com, true, i));
            else if (includeRelations)
                gatherInputs(childStructure, prefix + "c" + i + ".", list, includeRelations, false);
            i++;
        }
    }
    
    protected void gatherInputs(GuiTreeItemStructure item, String prefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
        if (item == this.item)
            addInput(item, "", list, includeRelations);
        
        if (searchForParent && includeRelations && item.getParentItem() instanceof GuiTreeItemStructure parent) {
            gatherInputs(parent, "p." + prefix, list, includeRelations, true);
            return;
        }
        
        if (item != this.item)
            addInput(item, prefix, list, includeRelations);
    }
    
    protected void addExternalOutputs(GuiTreeItemStructure item, String prefix, List<GuiSignalComponent> list, boolean includeRelations) {
        int i = 0;
        for (GuiTreeItem child : item.items()) {
            if (child == this.item)
                continue;
            GuiTreeItemStructure childStructure = (GuiTreeItemStructure) child;
            if (childStructure.structure == null)
                continue;
            
            String name = childStructure.structure.name;;
            if (childStructure.structure instanceof ISignalComponent com && com.getComponentType() == SignalComponentType.OUTPUT)
                list.add(new GuiSignalComponent(prefix + "o" + i, item.getTitle() + "." + (name != null ? name : "o" + i), com, true, i));
            else if (includeRelations)
                gatherOutputs(childStructure, prefix + "c" + i + ".", list, includeRelations, false);
        }
    }
    
    protected void addOutput(GuiTreeItemStructure item, String prefix, List<GuiSignalComponent> list, boolean includeRelations) {
        LittleStructureType type = item.getStructureType();
        if (type != null && type.outputs != null)
            for (int i = 0; i < type.outputs.size(); i++)
                list.add(new GuiSignalComponent(prefix + "b" + i, item.getTitle(), type.outputs.get(i), false, false, i));
            
        addExternalOutputs(item, prefix, list, includeRelations);
    }
    
    protected void gatherOutputs(GuiTreeItemStructure item, String prefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
        if (item == this.item)
            addOutput(item, "", list, includeRelations);
        
        if (searchForParent && includeRelations && item.getParentItem() instanceof GuiTreeItemStructure parent) {
            gatherOutputs(parent, "p." + prefix, list, includeRelations, searchForParent);
            return;
        }
        
        if (item != this.item)
            addOutput(item, prefix, list, includeRelations);
        
    }
}