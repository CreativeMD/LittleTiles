package com.creativemd.littletiles.client.gui.signal;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ChatFormatting;
import com.creativemd.littletiles.client.gui.signal.GuiSignalController.GeneratePatternException;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponent;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.InternalComponentOutput;
import com.creativemd.littletiles.common.structure.signal.component.ISignalComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.structure.signal.input.SignalInputCondition;
import com.creativemd.littletiles.common.structure.signal.logic.SignalLogicOperator;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode.GuiSignalModeConfiguration;
import com.creativemd.littletiles.common.structure.signal.logic.SignalTarget;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

public class SubGuiDialogSignal extends SubGui {
    
    public final List<GuiSignalComponent> inputs;
    protected final IConditionConfiguration event;
    
    public SubGuiDialogSignal(List<GuiSignalComponent> inputs, IConditionConfiguration event) {
        super(300, 200);
        this.inputs = inputs;
        this.event = event;
    }
    
    public GuiSignalComponent getInput(SignalTarget target) throws ParseException {
        for (GuiSignalComponent component : inputs)
            if (component.name.equals(target.writeBase()))
                return component;
        throw new ParseException("input not found", 0);
    }
    
    @Override
    public void createControls() {
        controls.add(new GuiLabel("result", translate("gui.signal.configuration.result"), 0, 0));
        
        GuiSignalController controller = new GuiSignalController("controller", 0, 22, 294, 150, event.getOutput(), inputs);
        controls.add(controller);
        List<String> inputLines = new ArrayList<>();
        for (GuiSignalComponent entry : inputs)
            inputLines.add(entry.info());
        inputLines.add("[]");
        inputLines.add("number");
        controls.add(new GuiComboBox("inputs", 0, 180, 80, inputLines));
        controls.add(new GuiButton("add", translate("gui.signal.configuration.add"), 88, 180) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                GuiComboBox inputsBox = (GuiComboBox) SubGuiDialogSignal.this.get("inputs");
                if (inputsBox.index < inputs.size())
                    controller.addInput(inputs.get(inputsBox.index));
                else if (inputsBox.index == inputs.size())
                    controller.addVirtualInput();
                else
                    controller.addVirtualNumberInput();
            }
        });
        
        List<String> opteratorLines = new ArrayList<>();
        opteratorLines.add(SignalLogicOperator.AND.display);
        opteratorLines.add(SignalLogicOperator.OR.display);
        opteratorLines.add(SignalLogicOperator.XOR.display);
        opteratorLines.add("not");
        opteratorLines.add(SignalLogicOperator.BITWISE_AND.display);
        opteratorLines.add(SignalLogicOperator.BITWISE_OR.display);
        opteratorLines.add(SignalLogicOperator.BITWISE_XOR.display);
        opteratorLines.add("b-not");
        opteratorLines.add(SignalLogicOperator.ADD.display);
        opteratorLines.add(SignalLogicOperator.SUB.display);
        opteratorLines.add(SignalLogicOperator.MUL.display);
        opteratorLines.add(SignalLogicOperator.DIV.display);
        controls.add(new GuiComboBox("operators", 103, 180, 40, opteratorLines));
        controls.add(new GuiButton("add", translate("gui.signal.configuration.addop"), 150, 180) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                GuiComboBox operatorsBox = (GuiComboBox) SubGuiDialogSignal.this.get("operators");
                int index = operatorsBox.index;
                if (index < 3)
                    controller.addOperator(SignalLogicOperator.values()[index]);
                else if (index == 3)
                    controller.addNotOperator(false);
                else if (index == 7)
                    controller.addNotOperator(true);
                else if (index > 7)
                    controller.addOperator(SignalLogicOperator.values()[index - 2]);
                else
                    controller.addOperator(SignalLogicOperator.values()[index - 1]);
            }
        });
        
        if (event.getCondition() != null)
            controller.setCondition(event.getCondition(), this);
        
        controls.add(new GuiLabel("delay", 210, 182));
        
        changed(new GuiControlChangedEvent(controller));
        
        if (event.hasModeConfiguration())
            controls.add(new GuiButton("mode", 250, 0) {
                
                @Override
                public void onClicked(int x, int y, int button) {
                    openClientLayer(new SubGuiDialogSignalMode(SubGuiDialogSignal.this, event));
                }
            });
        
        controls.add(new GuiButton("save", translate("gui.signal.configuration.save"), 270, 180) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                try {
                    event.setCondition(controller.generatePattern());
                    event.update();
                    closeGui();
                } catch (GeneratePatternException e) {}
            }
        });
        modeChanged();
    }
    
    public void modeChanged() {
        if (event.hasModeConfiguration()) {
            GuiButton button = (GuiButton) get("mode");
            button.setCaption(translate(event.getModeConfiguration().getMode().translateKey));
            button.posX = 300 - button.width;
        }
    }
    
    @CustomEventSubscribe
    public void changed(GuiControlChangedEvent event) {
        if (event.source.is("controller")) {
            GuiLabel label = (GuiLabel) get("result");
            GuiLabel delay = (GuiLabel) get("delay");
            try {
                SignalInputCondition condition = ((GuiSignalController) event.source).generatePattern();
                label.setCaption(translate("gui.signal.configuration.result") + " " + condition.toString());
                DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(5);
                delay.setCaption(df.format(condition.calculateDelay()) + " ticks");
            } catch (GeneratePatternException e) {
                label.setCaption(translate("gui.signal.configuration.result") + " " + translate(e.getMessage()));
                delay.setCaption(0 + " ticks");
            }
        }
        
    }
    
    public static class GuiSignalComponent {
        
        public final String name;
        public final boolean input;
        public final boolean external;
        public final int index;
        public final int bandwidth;
        public final String totalName;
        public final SignalMode defaultMode;
        
        public GuiSignalComponent(String name, String prefix, InternalComponent component, boolean input, boolean external, int index) {
            this.name = name;
            this.bandwidth = component.bandwidth;
            this.totalName = prefix + component.identifier;
            this.input = input;
            this.external = external;
            this.index = index;
            if (component instanceof InternalComponentOutput)
                this.defaultMode = ((InternalComponentOutput) component).defaultMode;
            else
                this.defaultMode = SignalMode.EQUAL;
        }
        
        public GuiSignalComponent(String name, String totalName, ISignalComponent component, boolean external, int index) {
            this.name = name;
            try {
                this.bandwidth = component.getBandwidth();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                throw new RuntimeException(e);
            }
            this.totalName = totalName;
            this.input = component.getType() == SignalComponentType.INPUT;
            this.external = external;
            this.index = index;
            this.defaultMode = SignalMode.EQUAL;
        }
        
        public GuiSignalComponent(String name, String totalName, int bandwidth, SignalComponentType type, boolean external, int index) {
            this.name = name;
            this.bandwidth = bandwidth;
            this.totalName = totalName;
            this.input = type == SignalComponentType.INPUT;
            this.external = external;
            this.index = index;
            this.defaultMode = SignalMode.EQUAL;
        }
        
        public String display() {
            if (name.equals(totalName))
                return ChatFormatting.BOLD + name + " " + ChatFormatting.RESET + bandwidth + "-bit";
            return ChatFormatting.BOLD + name + " " + totalName + " " + ChatFormatting.RESET + bandwidth + "-bit";
        }
        
        public String info() {
            return name + " " + totalName;
        }
    }
    
    public static interface IConditionConfiguration {
        
        public GuiSignalComponent getOutput();
        
        public SignalInputCondition getCondition();
        
        public void setCondition(SignalInputCondition condition);
        
        public boolean hasModeConfiguration();
        
        public GuiSignalModeConfiguration getModeConfiguration();
        
        public void setModeConfiguration(GuiSignalModeConfiguration config);
        
        public void update();
        
    }
}
