package team.creative.littletiles.common.gui.signal.dialog;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiPanel;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiFixedDimension;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.IConditionConfiguration;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.signal.component.ISignalComponent;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;
import team.creative.littletiles.common.structure.signal.output.SignalExternalOutputHandler;
import team.creative.littletiles.common.structure.signal.output.SignalOutputHandler;

public class GuiDialogSignalEvents extends GuiLayer {
    
    public GuiTreeItemStructure item;
    public List<GuiSignalComponent> inputs;
    public List<GuiSignalEvent> events;
    
    public GuiDialogSignalEvents() {
        super("gui.dialog.signal.overview", 300, 200);
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiTreeItemStructure item) {
        this.item = item;
        this.inputs = item.signalSearch.search(true, true, true);
        this.events = new ArrayList<>();
        for (GuiSignalEvent event : item.internalOutputs())
            this.events.add(event.copy());
        for (GuiSignalEvent event : item.externalOutputs())
            this.events.add(event.copy());
        super.init();
    }
    
    public void addEntry(GuiSignalEvent event) {
        GuiScrollY box = get("content");
        GuiPanel panel = new GuiPanel("event", GuiFlow.STACK_Y);
        box.add(panel.setExpandableX());
        panel.add(new GuiLabel("label"));
        panel.add(new GuiLabel("mode"));
        
        GuiParent buttons = new GuiParent();
        panel.add(buttons.setExpandableX());
        buttons.add(new GuiButton("edit", x -> LittleTilesGuiRegistry.SIGNAL_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(inputs, event)).setTranslate("gui.edit"));
        buttons.add(new GuiButton("reset", x -> event.reset()).setTranslate("gui.clear"));
        
        event.panel = panel;
        event.update();
    }
    
    @Override
    public void create() {
        if (item == null)
            return;
        
        GuiParent upper = new GuiParent(GuiFlow.STACK_X);
        add(upper.setExpandable());
        
        GuiScrollY left = new GuiScrollY("content");
        left.flow = GuiFlow.STACK_Y;
        upper.add(left.setExpandable());
        
        GuiScrollY right = new GuiScrollY("components");
        right.flow = GuiFlow.STACK_Y;
        upper.add(right.setDim(new GuiFixedDimension(100)).setExpandableY());
        
        right.add(new GuiLabel("title").setTranslate("gui.signal.components"));
        
        for (GuiSignalComponent component : inputs)
            right.add(new GuiLabel(component.name()).setTitle(Component.literal(component.display())));
        
        for (GuiSignalEvent event : events)
            addEntry(event);
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        
        bottom.addRight(new GuiButton("save", x -> {
            for (GuiSignalEvent event : events)
                GuiDialogSignalEvents.this.item.setSignalOutput(event.component.external(), event.component.index(), event);
            closeThisLayer();
        }).setTranslate("gui.save"));
        
    }
    
    public static class GuiSignalEvent implements IConditionConfiguration {
        
        public final GuiSignalComponent component;
        public SignalInputCondition condition;
        public GuiSignalModeConfiguration modeConfig;
        public GuiPanel panel;
        
        public GuiSignalEvent(GuiSignalComponent component) {
            this.component = component;
            this.condition = null;
            this.modeConfig = component.defaultMode().createConfiguration(null);
        }
        
        public GuiSignalEvent(GuiSignalComponent component, InternalSignalOutput internal) {
            this.component = component;
            if (internal == null) {
                this.condition = null;
                this.modeConfig = component.defaultMode().createConfiguration(null);
            } else {
                this.condition = internal.condition;
                SignalMode mode = internal.handler != null ? internal.handler.getMode() : component.defaultMode();
                this.modeConfig = mode.createConfiguration(internal.handler);
            }
        }
        
        public GuiSignalEvent(GuiSignalComponent component, SignalExternalOutputHandler external) {
            this.component = component;
            if (external == null) {
                this.condition = null;
                this.modeConfig = component.defaultMode().createConfiguration(null);
            } else {
                this.condition = external.condition;
                SignalMode mode = external.handler != null ? external.handler.getMode() : component.defaultMode();
                this.modeConfig = mode.createConfiguration(external.handler);
            }
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
            if (panel == null)
                return;
            
            GuiLabel label = (GuiLabel) panel.get("label");
            label.setTitle(Component.literal(component.name() + ": " + condition));
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