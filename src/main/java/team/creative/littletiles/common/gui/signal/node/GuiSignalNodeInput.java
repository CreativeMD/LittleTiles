package team.creative.littletiles.common.gui.signal.node;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
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
    
    public GuiSignalNodeInput(GuiSignalComponent component) {
        super(component);
    }
    
    public GuiSignalNodeInput(SignalInputVariable variable, GuiSignalComponent com) throws ParseException {
        super(com);
        SignalTarget target = variable.target.getNestedTarget();
        if (target instanceof SignalTargetChildCustomIndex)
            indexes = ((SignalTargetChildCustomIndex) target).indexes;
        else if (target instanceof SignalTargetChildIndex)
            indexes = new SignalCustomIndex[] { new SignalCustomIndexSingle(((SignalTargetChildIndex) target).index) };
        else if (target instanceof SignalTargetChildIndexRange)
            indexes = new SignalCustomIndex[] { new SignalCustomIndexRange(((SignalTargetChildIndexRange) target).index, ((SignalTargetChildIndexRange) target).index + ((SignalTargetChildIndexRange) target).length - 1) };
        if (variable instanceof SignalInputVariableOperator) {
            operator = 1;
            logic = ((SignalInputVariableOperator) variable).operator;
        } else if (variable instanceof SignalInputVariablePattern) {
            operator = 2;
            pattern = ((SignalInputVariablePattern) variable).indexes;
        } else if (variable instanceof SignalInputVariableEquation) {
            operator = 3;
            equation = ((SignalInputVariableEquation) variable).condition;
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
        setTitle(Component.literal(caption));
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
    public boolean mouseDoubleClicked(Rect rect, double x, double y, int button) {
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
    public boolean canConnectTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : tos)
            if (connectTo.to() == node)
                return false;
        return true;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node) {
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
    public Iterator<GuiSignalConnection> iterator() {
        return tos.iterator();
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
    
    @Override
    public int indexOf(GuiSignalConnection connection) {
        return tos.indexOf(connection);
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        try {
            SignalTarget target = SignalTarget.parseTarget(new SignalPatternParser(component.name() + (indexes != null ? "[" + getRange() + "]" : "")), false, false);
            switch (operator) {
                case 1:
                    return new SignalInputVariableOperator(target, logic);
                case 2:
                    return new SignalInputVariablePattern(target, pattern);
                case 3:
                    if (equation != null)
                        return new SignalInputVariableEquation(target, equation);
                default:
                    return new SignalInputVariable(target);
            }
            
        } catch (ParseException e) {
            throw new GeneratePatternException(this, "Invalid target");
        }
        
    }
    
}
