package team.creative.littletiles.common.gui.signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.littletiles.common.gui.signal.dialog.GuiSignalEvents;
import team.creative.littletiles.common.gui.signal.dialog.GuiSignalEvents.GuiSignalEvent;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;

public class GuiSignalEventsButton extends GuiButton {
    
    public GuiSignalEvents gui;
    public GuiTreeItemStructure item;
    public final List<GuiSignalComponent> inputs;
    public final List<GuiSignalComponent> outputs;
    public List<GuiSignalEvent> events;
    
    public GuiSignalEventsButton(String name, GuiTreeItemStructure item) {
        super(name, null);
        setTranslate("gui.signal.events");
        pressed = x -> GuiSignalEvents.SIGNAL_EVENTS_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(this);
        this.item = item;
        this.events = null;
        ComponentSearch search = new ComponentSearch(item);
        inputs = search.search(true, true, true);
        outputs = search.search(false, true, false);
        
        events = new ArrayList<>();
        
        for (GuiSignalComponent output : outputs)
            if (item.structure == null)
                events.add(new GuiSignalEvent(output, (InternalSignalOutput) null));
            else if (output.external())
                events.add(new GuiSignalEvent(output, item.structure.getExternalOutput(output.index())));
            else
                events.add(new GuiSignalEvent(output, item.structure.getOutput(output.index())));
            
    }
    
    public void setEventsInStructure(LittleStructure structure) {
        HashMap<Integer, SignalExternalOutputHandler> map = new HashMap<>();
        for (GuiSignalEvent event : events)
            if (event.component.external()) {
                if (event.condition != null)
                    map.put(event.component.index(), new SignalExternalOutputHandler(null, event.component.index(), event.condition, (x) -> event.getHandler(x, structure)));
            } else {
                InternalSignalOutput output = structure.getOutput(event.component.index());
                output.condition = event.condition;
                output.handler = event.getHandler(output, structure);
            }
        structure.setExternalOutputs(map);
    }
    
    private static class ComponentSearch {
        
        public GuiTreeItemStructure item;
        
        public ComponentSearch(GuiTreeItemStructure item) {
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
        
        protected void addInput(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
            if (item.gui.type() != null && item.gui.type().inputs != null)
                for (int i = 0; i < item.gui.type().inputs.size(); i++)
                    list.add(new GuiSignalComponent(prefix + "a" + i, totalNamePrefix, item.gui.type().inputs.get(i), true, false, i));
                
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
        
        protected void addOutput(GuiTreeItemStructure item, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
            if (item.gui.type() != null && item.gui.type().outputs != null)
                for (int i = 0; i < item.gui.type().outputs.size(); i++)
                    list.add(new GuiSignalComponent(prefix + "b" + i, totalNamePrefix, item.gui.type().outputs.get(i), false, false, i));
                
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
}
