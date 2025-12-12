package team.creative.littletiles.common.gui.signal.node;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.SignalContext;
import team.creative.littletiles.common.structure.signal.SignalState;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;
import team.creative.littletiles.common.structure.signal.input.SignalInputVariable;
import team.creative.littletiles.common.structure.signal.input.SignalInputVariable.SignalInputVariableEquation;
import team.creative.littletiles.common.structure.signal.input.SignalInputVariable.SignalInputVariableOperator;
import team.creative.littletiles.common.structure.signal.input.SignalInputVariable.SignalInputVariablePattern;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;
import team.creative.littletiles.common.structure.signal.logic.SignalPatternParser;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndex;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndexRange;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndexSingle;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChildCustomIndex;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChildIndex;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalTargetChildIndexRange;

public class GuiSignalNodeInput extends GuiSignalNodeComponent {
    
    public List<GuiSignalConnection> tos = new ArrayList<>();
    public SignalCustomIndex[] indexes;
    public int operator = 0; // 0 none, 1 logic operator, 2 pattern, 3 equation
    public SignalLogicOperator logic;
    public int[] pattern;
    public SignalInputCondition equation;
    
    private GuiSignalInputConfig testConfig;
    private SignalInputCondition testCondition;
    
    public GuiSignalNodeInput(GuiSignalComponent component, @Nullable SignalPosition position) {
        super(component, position);
    }
    
    public GuiSignalNodeInput(SignalInputVariable variable, GuiSignalComponent com, @Nullable SignalPosition position) throws ParseException {
        super(com, position);
        SignalTarget target = variable.target.getNestedTarget();
        if (target instanceof SignalTargetChildCustomIndex c)
            indexes = c.indexes;
        else if (target instanceof SignalTargetChildIndex c)
            indexes = new SignalCustomIndex[] { new SignalCustomIndexSingle(c.index) };
        else if (target instanceof SignalTargetChildIndexRange c)
            indexes = new SignalCustomIndex[] { new SignalCustomIndexRange(c.index, c.index + c.length - 1) };
        if (variable instanceof SignalInputVariableOperator o) {
            operator = 1;
            logic = o.operator;
        } else if (variable instanceof SignalInputVariablePattern p) {
            operator = 2;
            pattern = p.indexes;
        } else if (variable instanceof SignalInputVariableEquation e) {
            operator = 3;
            equation = e.condition;
        } else
            operator = 0;
        updateLabel();
    }
    
    public void updateLabel() {
        String caption = component.name();
        int length = 0;
        
        if (indexes != null) {
            String rangeText = getRange();
            if (rangeText.length() > 6) {
                rangeText = "...";
                length += 3;
            } else
                length += rangeText.length();
            caption += "[" + rangeText + "]";
        }
        String operatorText = "";
        switch (operator) {
            case 1:
                operatorText = (logic == SignalLogicOperator.AND ? "&" : logic.operator) + "";
                break;
            case 2:
                for (int i = 0; i < pattern.length; i++)
                    operatorText += "" + (pattern[i] >= 2 ? "*" : pattern[i]);
                break;
            case 3:
                if (equation != null)
                    operatorText = equation.write();
                break;
        }
        if (operatorText.length() + length > 10)
            operatorText = "...";
        if (!operatorText.isEmpty())
            caption += "{" + operatorText + "}";
        button.setTitle(Component.literal(caption));
        raiseEvent(new GuiControlChangedEvent(controller()));
    }
    
    public String getRange() {
        if (indexes == null)
            return "";
        String result = "";
        for (int i = 0; i < indexes.length; i++) {
            if (i > 0)
                result += ",";
            result += indexes[i].write();
        }
        return result;
    }
    
    @Override
    public boolean mouseDoubleClicked(double x, double y, int button) {
        LittleTilesGuiRegistry.INPUT_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(this);
        return true;
    }
    
    @Override
    public GuiSignalConnection getConnectionTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return connectTo;
        return null;
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return false;
        return true;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        return false;
    }
    
    @Override
    public void disconnect(GuiSignalConnection connection) {
        tos.remove(connection);
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        tos.add(connection);
    }
    
    @Override
    public Iterable<GuiSignalConnection> toConnections() {
        return tos;
    }
    
    @Override
    public void remove() {
        for (GuiSignalConnection connection : new ArrayList<>(tos))
            connection.disconnect(controller());
    }
    
    protected SignalInputCondition parseCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        try {
            SignalTarget target = SignalTarget.parseTarget(new SignalPatternParser(component.name() + (indexes != null ? "[" + getRange() + "]" : "")), false, false);
            switch (operator) {
                case 1:
                    return new SignalInputVariableOperator(target, logic, position());
                case 2:
                    return new SignalInputVariablePattern(target, pattern, position());
                case 3:
                    if (equation != null)
                        return new SignalInputVariableEquation(target, equation, position());
                default:
                    return new SignalInputVariable(target, position());
            }
            
        } catch (ParseException e) {
            throw new GeneratePatternException(this, "Invalid target");
        }
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed, @Nullable SignalContext testContext) throws GeneratePatternException {
        reset();
        var condition = parseCondition(processed);
        
        if (testContext != null) {
            if (controller().testingDisplayNumber())
                testConfig = new GuiSignalInputNumber(component.bandwidth());
            else
                testConfig = new GuiSignalInputBitToggle(component.bandwidth());
            testConfig.set(controller().getInputState(component.totalName(), component.bandwidth()));
            insertControlBefore(button, testConfig);
            testCondition = condition;
            testInputChanged();
        }
        
        return condition;
    }
    
    @Override
    public void resetTest() {
        if (testConfig != null)
            remove(testConfig);
        testCondition = null;
    }
    
    @Override
    public void testInputChanged() {
        if (testCondition == null || testConfig == null)
            return;
        
        testConfig.set(controller().getInputState(component.name(), component.bandwidth()));
    }
    
    public static abstract class GuiSignalInputConfig extends GuiParent {
        
        public abstract SignalState get();
        
        public abstract void set(SignalState state);
        
    }
    
    public class GuiSignalInputBitToggle extends GuiSignalInputConfig {
        
        public final int bandwidth;
        protected final GuiStateButton<Integer>[] buttons;
        private boolean preventUpdate;
        
        public GuiSignalInputBitToggle(int bandwidth) {
            this.bandwidth = bandwidth;
            spacing = 0;
            setScale(0.5);
            
            buttons = new GuiStateButton[bandwidth];
            for (int i = 0; i < buttons.length; i++)
                add(buttons[i] = new GuiStateButton<Integer>("" + i, new TextMapBuilder<Integer>().addComponent(0, Component.literal("" + 0)).addComponent(1, Component.literal(
                    "" + 1))));
            
            registerEventChanged(x -> {
                if (preventUpdate)
                    return;
                controller().setInputState(component.name(), get());
            });
        }
        
        @Override
        public void set(SignalState state) {
            preventUpdate = true;
            for (int i = 0; i < buttons.length; i++)
                buttons[i].select(state.is(i) ? 1 : 0);
            preventUpdate = false;
        }
        
        @Override
        public SignalState get() {
            var state = SignalState.of(0);
            for (int i = 0; i < buttons.length; i++)
                if (buttons[i].selected() == 1)
                    state = state.set(i, true);
            return state;
        }
        
    }
    
    public class GuiSignalInputNumber extends GuiSignalInputConfig {
        
        public final int bandwidth;
        protected final GuiTextfield textfield;
        private boolean preventUpdate;
        
        public GuiSignalInputNumber(int bandwidth) {
            this.bandwidth = bandwidth;
            add(textfield = new GuiTextfield("").setDim(50).setNumbersIncludingNegativeOnly());
            setScale(0.5);
            registerEventChanged(x -> {
                if (preventUpdate)
                    return;
                controller().setInputState(component.name(), get());
            });
        }
        
        @Override
        public void set(SignalState state) {
            preventUpdate = true;
            if (!textfield.isFocused())
                textfield.setText(state.number() + "", false);
            preventUpdate = false;
        }
        
        @Override
        public SignalState get() {
            return SignalState.of(textfield.parseInteger(), bandwidth);
        }
        
        @Override
        public void looseFocus() {
            super.looseFocus();
            if (get().number() != textfield.parseInteger())
                set(get());
        }
    }
}
