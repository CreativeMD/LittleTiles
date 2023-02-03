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
            gatherInputs(item, "", "", list, includeRelations, true);
        if (output)
            gatherOutputs(item, "", "", list, includeRelations, true);
        return list;
    }
    
    public GuiSignalComponent[] internalOutputs() {
        LittleStructureType type = item.getStructureType();
        if (type != null && type.outputs != null) {
            GuiSignalComponent[] components = new GuiSignalComponent[type.outputs.size()];
            for (int i = 0; i < type.outputs.size(); i++)
                components[i] = new GuiSignalComponent("b" + i, "", type.outputs.get(i), false, false, i);
            return components;
        }
        return null;
    }
    
    public List<GuiSignalComponent> externalOutputs() {
        List<GuiSignalComponent> components = new ArrayList<>();
        addExternalOutputs(item, "", "", components, false);
        return components;
    }
    
    protected void addInput(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
        LittleStructureType type = item.getStructureType();
        if (type != null && type.inputs != null)
            for (int i = 0; i < type.inputs.size(); i++)
                list.add(new GuiSignalComponent(prefix + "a" + i, totalNamePrefix, type.inputs.get(i), true, false, i));
            
        int i = 0;
        for (GuiTreeItem child : item.items()) {
            if (child == this.item)
                continue;
            GuiTreeItemStructure childStructure = (GuiTreeItemStructure) child;
            if (childStructure.structure == null)
                continue;
            
            String name = childStructure.structure.name;
            if (childStructure.structure instanceof ISignalComponent com && com.getComponentType() == SignalComponentType.INPUT)
                list.add(new GuiSignalComponent(prefix + "i" + i, totalNamePrefix + (name != null ? name : "i" + i), com, true, i));
            else if (includeRelations)
                gatherInputs(childStructure, prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."), list, includeRelations, false);
            i++;
        }
    }
    
    protected void gatherInputs(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
        if (item == this.item)
            addInput(item, "", "", list, includeRelations);
        
        if (searchForParent && includeRelations && item.getParentItem() instanceof GuiTreeItemStructure parent) {
            gatherInputs(parent, "p." + prefix, "p." + totalNamePrefix, list, includeRelations, true);
            return;
        }
        
        if (item != this.item)
            addInput(item, prefix, totalNamePrefix, list, includeRelations);
    }
    
    protected void addExternalOutputs(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
        int i = 0;
        for (GuiTreeItem child : item.items()) {
            if (child == this.item)
                continue;
            GuiTreeItemStructure childStructure = (GuiTreeItemStructure) child;
            if (childStructure.structure == null)
                continue;
            
            String name = childStructure.structure.name;;
            if (childStructure.structure instanceof ISignalComponent com && com.getComponentType() == SignalComponentType.OUTPUT)
                list.add(new GuiSignalComponent(prefix + "o" + i, totalNamePrefix + (name != null ? name : "o" + i), com, true, i));
            else if (includeRelations)
                gatherOutputs(childStructure, prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."), list, includeRelations, false);
        }
    }
    
    protected void addOutput(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
        LittleStructureType type = item.getStructureType();
        if (type != null && type.outputs != null)
            for (int i = 0; i < type.outputs.size(); i++)
                list.add(new GuiSignalComponent(prefix + "b" + i, totalNamePrefix, type.outputs.get(i), false, false, i));
            
        addExternalOutputs(item, prefix, totalNamePrefix, list, includeRelations);
    }
    
    protected void gatherOutputs(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
        if (item == this.item)
            addOutput(item, "", "", list, includeRelations);
        
        if (searchForParent && includeRelations && item.getParentItem() instanceof GuiTreeItemStructure parent) {
            gatherOutputs(parent, "p." + prefix, "p." + totalNamePrefix, list, includeRelations, searchForParent);
            return;
        }
        
        if (item != this.item)
            addOutput(item, prefix, totalNamePrefix, list, includeRelations);
        
    }
}