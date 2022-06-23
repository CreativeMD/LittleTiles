package team.creative.littletiles.common.gui.dialogs;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.handler.GuiLayerHandler;
import team.creative.creativecore.common.gui.packet.LayerOpenPacket;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.signal.SubGuiDialogSignal;
import team.creative.littletiles.common.gui.signal.SubGuiDialogSignal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.SubGuiDialogSignal.IConditionConfiguration;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class GuiLayerSignalEvents extends GuiLayer {
    
    public static final GuiLayerHandler SIGNAL_LAYER = (parent, nbt) -> new GuiLayerSignalEvents();
    
    private GuiSignalEventsButton button;
    public List<GuiSignalEvent> events;
    
    public GuiLayerSignalEvents() {
        super("events", 300, 200);
    }
    
    public void setButton(GuiSignalEventsButton button) {
        this.button = button;
        this.events = new ArrayList<>();
        for (GuiSignalEvent event : button.events)
            this.events.add(event.copy());
    }
    
    @Override
    public void create() {
        if (button == null)
            return;
        
        flow = GuiFlow.STACK_Y;
        GuiParent upperBox = new GuiParent(GuiFlow.STACK_X);
        add(upperBox.setExpandableY());
        
        upperBox.add(new GuiScrollY("content").setAlign(Align.STRETCH).setExpandable());
        
        GuiParent components = new GuiParent(GuiFlow.STACK_Y);
        upperBox.add(components.setExpandable());
        
        components.add(new GuiLabel("components").setTranslate("gui.components"));
        for (GuiSignalComponent component : button.inputs)
            components.add(new GuiLabel("components").setTitle(Component.literal(component.display())));
        
        GuiLeftRightBox lowerBox = new GuiLeftRightBox();
        add(lowerBox);
        
        lowerBox.addRight(new GuiButton("save", x -> {
            GuiLayerSignalEvents.this.button.events = events;
            closeTopLayer();
        }).setTranslate("gui.save")).addLeft(new GuiButton("cancel", x -> closeTopLayer()).setTranslate("gui.cancel"));
        
        for (GuiSignalEvent event : events)
            addEvent(event);
    }
    
    public void addEvent(GuiSignalEvent event) {
        GuiScrollY box = (GuiScrollY) get("content");
        
        GuiParent panel = new GuiParent(event.component.name, GuiFlow.STACK_Y);
        box.add(panel);
        
        GuiParent upper = new GuiParent(GuiFlow.STACK_X).setAlign(Align.STRETCH);
        panel.add(upper);
        upper.add(new GuiLabel("label").setExpandableX());
        upper.add(new GuiLabel("mode"));
        
        GuiParent lower = new GuiParent(GuiFlow.STACK_X).setAlign(Align.STRETCH);
        panel.add(lower);
        lower.add(new GuiButton("edit", x -> {
            SubGuiDialogSignal layer = (SubGuiDialogSignal) this.getParent().openLayer(new LayerOpenPacket(SubGuiDialogSignal.SIGNAL_DIALOG, new CompoundTag()));
            layer.set(GuiLayerSignalEvents.this.button.inputs, event);
            layer.init();
        }).setTranslate("gui.edit"));
        
        lower.add(new GuiButton("reset", x -> event.reset()).setTranslate("gui.reset"));
        
        event.panel = panel;
        event.update();
    }
    
    public List<String> getComponents(LittleGroup previews, SignalComponentType type) {
        List<String> values = new ArrayList<>();
        int i = 0;
        for (LittleGroup child : previews.children.children()) {
            LittleStructureType structure = child.getStructureType();
            if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getComponentType() == type) {
                String name = child.getStructureName();
                try {
                    values.add(ChatFormatting.BOLD + (type == SignalComponentType.INPUT ? "i" : "o") + i + " " + (name != null ? "(" + name + ") " : "") + "" + ChatFormatting.RESET + ((ISignalComponent) structure)
                            .getBandwidth() + "-bit");
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            }
            i++;
        }
        return values;
    }
    
    public static class GuiSignalEventsButton extends GuiButton {
        
        public GuiLayerSignalEvents gui;
        public LittleGroup previews;
        public LittleStructureType type;
        public LittleStructure activator;
        public final List<GuiSignalComponent> inputs;
        public final List<GuiSignalComponent> outputs;
        public List<GuiSignalEvent> events;
        
        public GuiSignalEventsButton(String name, LittleGroup previews, LittleStructure structure, LittleStructureType type) {
            super(name, null);
            pressed = button -> {
                GuiLayerSignalEvents layer = (GuiLayerSignalEvents) this.getParent().openLayer(new LayerOpenPacket(SIGNAL_LAYER, new CompoundTag()));
                layer.setButton(this);
                layer.init();
            };
            setTranslate("gui.signal.events");
            this.previews = previews;
            this.activator = structure;
            this.type = type;
            this.events = null;
            ComponentSearch search = new ComponentSearch(previews, type);
            inputs = search.search(true, true, true);
            outputs = search.search(false, true, false);
            
            events = new ArrayList<>();
            for (GuiSignalComponent output : outputs) {
                CompoundTag nbt = previews.getStructureTag();
                if (output.external) {
                    if (nbt == null)
                        events.add(new GuiSignalEvent(output, new CompoundTag()));
                    else {
                        boolean found = false;
                        ListTag list = nbt.getList("signal", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag outputNBT = list.getCompound(i);
                            if (outputNBT.getInt("index") == output.index) {
                                events.add(new GuiSignalEvent(output, outputNBT));
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            events.add(new GuiSignalEvent(output, new CompoundTag()));
                    }
                } else
                    events.add(new GuiSignalEvent(output, nbt == null ? new CompoundTag() : nbt.getCompound(output.totalName)));
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
    }
    
    private static class ComponentSearch {
        
        public LittleGroup previews;
        public LittleStructureType type;
        
        public ComponentSearch(LittleGroup previews, LittleStructureType type) {
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
        
        protected void addInput(LittleGroup previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
            if (type != null && type.inputs != null)
                for (int i = 0; i < type.inputs.size(); i++)
                    list.add(new GuiSignalComponent(prefix + "a" + i, totalNamePrefix, type.inputs.get(i), true, false, i));
                
            int i = 0;
            for (LittleGroup child : previews.children.children()) {
                if (child == this.previews)
                    continue;
                LittleStructureType structure = child.getStructureType();
                String name = child.getStructureName();
                if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getComponentType() == SignalComponentType.INPUT)
                    list.add(new GuiSignalComponent(prefix + "i" + i, totalNamePrefix + (name != null ? name : "i" + i), (ISignalComponent) structure, true, i));
                else if (includeRelations)
                    gatherInputs(child, child
                            .getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."), list, includeRelations, false);
                i++;
            }
        }
        
        protected void gatherInputs(LittleGroup previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
            if (previews == this.previews)
                addInput(previews, type, "", "", list, includeRelations);
            
            if (searchForParent && previews.hasParent() && includeRelations) {
                gatherInputs(previews.getParent(), previews.getParent().getStructureType(), "p." + prefix, "p." + totalNamePrefix, list, includeRelations, true);
                return;
            }
            
            if (previews != this.previews)
                addInput(previews, type, prefix, totalNamePrefix, list, includeRelations);
        }
        
        protected void addOutput(LittleGroup previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations) {
            if (type != null && type.outputs != null)
                for (int i = 0; i < type.outputs.size(); i++)
                    list.add(new GuiSignalComponent(prefix + "b" + i, totalNamePrefix, type.outputs.get(i), false, false, i));
                
            int i = 0;
            for (LittleGroup child : previews.children.children()) {
                if (child == this.previews)
                    continue;
                LittleStructureType structure = child.getStructureType();
                String name = child.getStructureName();
                if (structure instanceof ISignalComponent && ((ISignalComponent) structure).getComponentType() == SignalComponentType.OUTPUT)
                    list.add(new GuiSignalComponent(prefix + "o" + i, totalNamePrefix + (name != null ? name : "o" + i), (ISignalComponent) structure, true, i));
                else if (includeRelations)
                    gatherOutputs(child, child
                            .getStructureType(), prefix + "c" + i + ".", totalNamePrefix + (name != null ? name + "." : "c" + i + "."), list, includeRelations, false);
                
                i++;
            }
        }
        
        protected void gatherOutputs(LittleGroup previews, LittleStructureType type, String prefix, String totalNamePrefix, List<GuiSignalComponent> list, boolean includeRelations, boolean searchForParent) {
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
        public GuiParent panel;
        
        public GuiSignalEvent(GuiSignalComponent component, CompoundTag nbt) {
            this.component = component;
            try {
                if (nbt.contains("con"))
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
            label.setTitle(Component.literal(component.name + ": " + condition));
            GuiLabel mode = (GuiLabel) panel.get("mode");
            int delay = modeConfig.delay;
            if (condition != null)
                delay = Math.max(delay, (int) Math.ceil(condition.calculateDelay()));
            mode.setTitle(Component.translatable(modeConfig.getMode().translateKey).append(" ").append(Component.translatable("gui.delay")).append(": " + delay));
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
