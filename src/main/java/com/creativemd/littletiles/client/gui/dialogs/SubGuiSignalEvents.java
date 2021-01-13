package com.creativemd.littletiles.client.gui.dialogs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiListBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.littletiles.client.gui.signal.SubGuiDialogSignal;
import com.creativemd.littletiles.client.gui.signal.SubGuiDialogSignal.GuiSignalComponent;
import com.creativemd.littletiles.client.gui.signal.SubGuiDialogSignal.IConditionConfiguration;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;
import com.creativemd.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputHandler;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SubGuiSignalEvents extends SubGui {
    
    public GuiSignalEventsButton button;
    public List<GuiSignalEvent> events;
    
    public SubGuiSignalEvents(GuiSignalEventsButton button) {
        super(300, 200);
        this.button = button;
        this.events = new ArrayList<>();
        for (GuiSignalEvent event : button.events)
            this.events.add(event.copy());
    }
    
    public void addEntry(GuiSignalEvent event) {
        GuiScrollBox box = (GuiScrollBox) get("content");
        GuiPanel panel = new GuiPanel("event", 2, 2, 158, 30);
        panel.addControl(new GuiLabel("label", 0, 0));
        panel.addControl(new GuiLabel("mode", 0, 16));
        panel.addControl(new GuiButton("edit", 84, 14, 30, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                openClientLayer(new SubGuiDialogSignal(SubGuiSignalEvents.this.button.inputs, event));
            }
        });
        
        panel.addControl(new GuiButton("reset", 122, 14, 30, 10) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                event.reset();
            }
        });
        
        box.addControl(panel);
        event.panel = panel;
        event.update();
        reloadListBox();
    }
    
    public void reloadListBox() {
        GuiScrollBox box = (GuiScrollBox) get("content");
        int height = 2;
        for (GuiControl control : box.controls) {
            control.posY = height;
            height += control.height + 2;
        }
    }
    
    public List<String> getComponents(LittlePreviews previews, SignalComponentType type) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < previews.childrenCount(); i++) {
            LittlePreviews child = previews.getChild(i);
            LittleStructureType structure = child.getStructureType();
            if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == type) {
                String name = child.getStructureName();
                try {
                    values
                        .add(ChatFormatting.BOLD + (type == SignalComponentType.INPUT ? "i" : "o") + i + " " + (name != null ? "(" + name + ") " : "") + "" + ChatFormatting.RESET + ((ISignalComponent) structure)
                            .getBandwidth() + "-bit");
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            }
        }
        return values;
    }
    
    @Override
    public void createControls() {
        
        GuiScrollBox box = new GuiScrollBox("content", 0, 0, 170, 172);
        
        List<String> values = new ArrayList<>();
        values.add("Components:");
        
        for (GuiSignalComponent component : button.inputs)
            values.add(component.display());
        
        GuiListBox components = new GuiListBox("components", 180, 0, 120, 180, values);
        
        controls.add(components);
        controls.add(box);
        
        for (GuiSignalEvent event : events)
            addEntry(event);
        
        controls.add(new GuiButton("save", 146, 180) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                SubGuiSignalEvents.this.button.events = events;
                closeGui();
            }
        });
        controls.add(new GuiButton("cancel", 0, 180) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                closeGui();
            }
        });
    }
    
    public static class GuiSignalEventsButton extends GuiButton {
        
        public SubGuiSignalEvents gui;
        public LittlePreviews previews;
        public LittleStructureType type;
        public LittleStructure activator;
        public final List<GuiSignalComponent> inputs;
        public final List<GuiSignalComponent> outputs;
        public List<GuiSignalEvent> events;
        
        public GuiSignalEventsButton(String name, int x, int y, LittlePreviews previews, LittleStructure structure, LittleStructureType type) {
            super(name, translate("gui.signal.events"), x, y, 40, 7);
            this.previews = previews;
            this.activator = structure;
            this.type = type;
            this.events = null;
            ComponentSearch search = new ComponentSearch(previews, type);
            inputs = search.search(true, true, true);
            outputs = search.search(false, true, false);
            
            events = new ArrayList<>();
            for (GuiSignalComponent output : outputs) {
                NBTTagCompound nbt = previews.structureNBT;
                if (output.external) {
                    if (nbt == null)
                        events.add(new GuiSignalEvent(output, new NBTTagCompound()));
                    else {
                        boolean found = false;
                        NBTTagList list = nbt.getTagList("signal", 10);
                        for (int i = 0; i < list.tagCount(); i++) {
                            NBTTagCompound outputNBT = list.getCompoundTagAt(i);
                            if (outputNBT.getInteger("index") == output.index) {
                                events.add(new GuiSignalEvent(output, outputNBT));
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            events.add(new GuiSignalEvent(output, new NBTTagCompound()));
                    }
                } else
                    events.add(new GuiSignalEvent(output, nbt == null ? new NBTTagCompound() : nbt.getCompoundTag(output.totalName)));
            }
        }
        
        public void setEventsInStructure(LittleStructure structure) {
            HashMap<Integer, SignalExternalOutputHandler> map = new HashMap<>();
            for (GuiSignalEvent event : events) {
                if (event.component.external) {
                    if (event.condition != null)
                        map.put(event.component.index, new SignalExternalOutputHandler(null, event.component.index, event.condition, (x) -> event.getHandler(x, structure)));
                } else {
                    InternalSignalOutput output = structure.getOutput(event.component.index);
                    output.condition = event.condition;
                    output.handler = event.getHandler(output, structure);
                }
            }
            structure.setExternalHandler(map);
        }
        
        @Override
        public void onClicked(int x, int y, int button) {
            openClientLayer(new SubGuiSignalEvents(this));
        }
    }
    
    private static class ComponentSearch {
        
        public LittlePreviews previews;
        public LittleStructureType type;
        
        public ComponentSearch(LittlePreviews previews, LittleStructureType type) {
            this.previews = previews;
            this.type = type;
        }
        
        public List<GuiSignalComponent> search(boolean input, boolean output, boolean includeRelations) {
            List<GuiSignalComponent> list = new ArrayList<>();
            if (input)
                gatherInputs(previews, type, "", "", list, includeRelations, true);
            if (output)
                gatherOutputs(previews, type, "", "", list, includeRelations, true);
            return list;
        }
        
        protected void addInput(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
            if (type != null && type.inputs != null)
                for (int i = 0; i < type.inputs.size(); i++)
                    list.add(new GuiSignalComponent(prefix + "a" + i, totalNamePrefix, type.inputs.get(i), true, false, i));
                
            for (int i = 0; i < previews.childrenCount(); i++) {
                LittlePreviews child = previews.getChild(i);
                if (child == this.previews)
                    continue;
                LittleStructureType structure = child.getStructureType();
                String name = child.getStructureName();
                if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == SignalComponentType.INPUT)
                    list.add(new GuiSignalComponent(prefix + "i" + i, totalNamePrefix + (name != null ? name : "i" + i), (ISignalComponent) structure, true, i));
                else if (includeRelations)
                    gatherInputs(child, child
                        .getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."), list, includeRelations, false);
            }
        }
        
        protected void gatherInputs(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
            if (previews == this.previews)
                addInput(previews, type, "", "", list, includeRelations);
            
            if (searchForParent && previews.hasParent() && includeRelations) {
                gatherInputs(previews.getParent(), previews.getParent().getStructureType(), "p." + prefix, "p." + totalNamePrefix, list, includeRelations, true);
                return;
            }
            
            if (previews != this.previews)
                addInput(previews, type, prefix, totalNamePrefix, list, includeRelations);
        }
        
        protected void addOutput(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
            if (type != null && type.outputs != null)
                for (int i = 0; i < type.outputs.size(); i++)
                    list.add(new GuiSignalComponent(prefix + "b" + i, totalNamePrefix, type.outputs.get(i), false, false, i));
                
            for (int i = 0; i < previews.childrenCount(); i++) {
                LittlePreviews child = previews.getChild(i);
                if (child == this.previews)
                    continue;
                LittleStructureType structure = child.getStructureType();
                String name = child.getStructureName();
                if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getType() == SignalComponentType.OUTPUT)
                    list.add(new GuiSignalComponent(prefix + "o" + i, totalNamePrefix + (name != null ? name : "o" + i), (ISignalComponent) structure, true, i));
                else if (includeRelations)
                    gatherOutputs(child, child
                        .getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."), list, includeRelations, false);
            }
        }
        
        protected void gatherOutputs(LittlePreviews previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
            if (previews == this.previews)
                addOutput(previews, type, "", "", list, includeRelations);
            
            if (searchForParent && previews.hasParent() && includeRelations) {
                gatherOutputs(previews.getParent(), previews.getParent().getStructureType(), "p." + prefix, "p." + totalNamePrefix, list, includeRelations, searchForParent);
                return;
            }
            
            if (previews != this.previews)
                addOutput(previews, type, prefix, totalNamePrefix, list, includeRelations);
            
        }
    }
    
    public static class GuiSignalEvent implements IConditionConfiguration {
        
        public final GuiSignalComponent component;
        public SignalInputCondition condition;
        public GuiSignalModeConfiguration modeConfig;
        public GuiPanel panel;
        
        public GuiSignalEvent(GuiSignalComponent component, NBTTagCompound nbt) {
            this.component = component;
            try {
                if (nbt.hasKey("con"))
                    condition = SignalInputCondition.parseInput(nbt.getString("con"));
                else
                    condition = null;
            } catch (ParseException e) {
                condition = null;
            }
            this.modeConfig = SignalMode.getConfig(nbt, component.defaultMode);
        }
        
        private GuiSignalEvent(GuiSignalComponent component, SignalInputCondition condition, GuiSignalModeConfiguration modeConfig) {
            this.component = component;
            this.condition = condition;
            this.modeConfig = modeConfig;
        }
        
        public void reset() {
            modeConfig = SignalMode.getConfigDefault();
            condition = null;
            update();
        }
        
        @Override
        public void update() {
            GuiLabel label = (GuiLabel) panel.get("label");
            label.setCaption(component.name + ": " + condition);
            GuiLabel mode = (GuiLabel) panel.get("mode");
            int delay = modeConfig.delay;
            if (condition != null)
                delay = Math.max(delay, (int) Math.ceil(condition.calculateDelay()));
            mode.setCaption(translate(modeConfig.getMode().translateKey) + " delay: " + delay);
        }
        
        public SignalOutputHandler getHandler(ISignalComponent component, LittleStructure structure) {
            if (condition != null)
                return modeConfig.getHandler(component, structure);
            return null;
        }
        
        public GuiSignalEvent copy() {
            return new GuiSignalEvent(component, condition, modeConfig.copy());
        }
        
        @Override
        public GuiSignalComponent getOutput() {
            return component;
        }
        
        @Override
        public SignalInputCondition getCondition() {
            return condition;
        }
        
        @Override
        public void setCondition(SignalInputCondition condition) {
            this.condition = condition;
        }
        
        @Override
        public boolean hasModeConfiguration() {
            return true;
        }
        
        @Override
        public GuiSignalModeConfiguration getModeConfiguration() {
            return modeConfig;
        }
        
        @Override
        public void setModeConfiguration(GuiSignalModeConfiguration config) {
            this.modeConfig = config;
        }
        
    }
}
