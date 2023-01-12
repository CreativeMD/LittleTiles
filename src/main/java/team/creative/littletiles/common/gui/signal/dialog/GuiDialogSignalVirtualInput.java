package team.creative.littletiles.common.gui.signal.dialog;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiPanel;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiCounter;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButton;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextListBuilder;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.IConditionConfiguration;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeVirtualInput;
import team.creative.littletiles.common.structure.signal.component.SignalComponentType;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputVirtualNumber;
import team.creative.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;

public class GuiDialogSignalVirtualInput extends GuiLayer {
    
    public GuiSignalNodeVirtualInput input;
    public List<GuiSignalComponent> inputs;
    public GuiVirtualInputIndexConfiguration[] config;
    
    public GuiDialogSignalVirtualInput() {
        super("gui.dialog.signal.virtual_input", 100, 100);
        flow = GuiFlow.STACK_Y;
        registerEventChanged(this::changed);
    }
    
    public void init(List<GuiSignalComponent> inputs, GuiSignalNodeVirtualInput input) {
        this.input = input;
        this.inputs = inputs;
        super.init();
    }
    
    @Override
    public void create() {
        if (input == null)
            return;
        add(new GuiCounter("bandwidth", input.conditions.length, 0, 256));
        add(new GuiScrollY("config").setExpandable());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            input.conditions = new SignalInputCondition[config.length];
            for (int i = 0; i < config.length; i++)
                input.conditions[i] = config[i].parse();
            input.updateLabel();
            closeThisLayer();
            
        }).setTranslate("gui.save"));
        loadConditions();
    }
    
    public void changed(GuiControlChangedEvent event) {
        if (event.control.is("bandwidth"))
            loadConditions();
    }
    
    public void loadConditions() {
        GuiScrollY box = get("config");
        box.clear();
        GuiCounter counter = get("bandwidth");
        int bandwidth = counter.getValue();
        config = new GuiVirtualInputIndexConfiguration[bandwidth];
        for (int i = 0; i < bandwidth; i++) {
            GuiVirtualInputIndexConfiguration index = new GuiVirtualInputIndexConfiguration(i < input.conditions.length ? input.conditions[i] : new SignalInputVirtualNumber(0), i);
            index.create(box);
            config[i] = index;
        }
    }
    
    public class GuiVirtualInputIndexConfiguration implements IConditionConfiguration {
        
        public final GuiSignalComponent output;
        public final int index;
        public SignalInputCondition condition;
        public GuiPanel panel;
        
        public GuiVirtualInputIndexConfiguration(SignalInputCondition condition, int index) {
            this.output = new GuiSignalComponent("" + index, "" + index, 1, SignalComponentType.OUTPUT, false, index);
            this.index = index;
            this.condition = condition;
        }
        
        public void create(GuiScrollY box) {
            panel = new GuiPanel(index + "", GuiFlow.STACK_Y);
            box.add(panel.setExpandableX());
            
            panel.registerEventChanged(x -> {
                if (x.control.is("type"))
                    update();
            });
            
            panel.add(new GuiLabel("label").setTitle(Component.literal(index + ": " + (condition != null ? condition.write() : "0"))));
            int state = 0;
            if (condition instanceof SignalInputVirtualNumber virtual)
                state = virtual.number == 1 ? 1 : 0;
            else
                state = 2;
            panel.add(new GuiStateButton("type", state, new TextListBuilder().add("false", "true", "equation")));
            panel.add(new GuiButton("edit", x -> GuiDialogSignal.SIGNAL_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(inputs, GuiVirtualInputIndexConfiguration.this))
                    .setTranslate("gui.edit"));
            update();
        }
        
        @Override
        public void update() {
            GuiLabel label = (GuiLabel) panel.get("label");
            GuiStateButton type = (GuiStateButton) panel.get("type");
            GuiButton edit = (GuiButton) panel.get("edit");
            
            label.setTitle(Component.literal(index + ": " + parse().write()));
            edit.setEnabled(type.getState() == 2);
        }
        
        public SignalInputCondition parse() {
            GuiStateButton type = (GuiStateButton) panel.get("type");
            if (type.getState() == 0)
                return new SignalInputVirtualNumber(0);
            else if (type.getState() == 1)
                return new SignalInputVirtualNumber(1);
            if (condition != null)
                return condition;
            return new SignalInputVirtualNumber(0);
        }
        
        @Override
        public GuiSignalComponent getOutput() {
            return output;
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
            return false;
        }
        
        @Override
        public GuiSignalModeConfiguration getModeConfiguration() {
            return null;
        }
        
        @Override
        public void setModeConfiguration(GuiSignalModeConfiguration config) {
            
        }
        
    }
    
}