package team.creative.littletiles.common.gui.signal.node;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.SignalContext;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;

public class GuiSignalNodeOperator extends GuiSignalNode {
    
    public final SignalLogicOperator operator;
    private List<GuiSignalConnection> from = new ArrayList<>();
    private List<GuiSignalConnection> to = new ArrayList<>();
    
    private GuiLabel testLabel;
    private SignalInputCondition testCondition;
    private SignalContext testContext;
    
    public GuiSignalNodeOperator(SignalLogicOperator operator, @Nullable SignalPosition position) {
        super(operator.display, position);
        this.operator = operator;
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        for (GuiSignalConnection connectTo : to)
            if (connectTo.to() == node)
                return false;
        return true;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor) {
        for (GuiSignalConnection connectFrom : from)
            if (connectFrom.from() == node)
                return false;
        return true;
    }
    
    @Override
    public GuiSignalConnection getConnectionTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : to)
            if (connectTo.to() == node)
                return connectTo;
        return null;
    }
    
    @Override
    public void disconnect(GuiSignalConnection connection) {
        if (connection.to() == this)
            from.remove(connection);
        else
            to.remove(connection);
    }
    
    @Override
    public Iterable<GuiSignalConnection> toConnections() {
        return to;
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        if (connection.to() == this)
            from.add(connection);
        else
            to.add(connection);
    }
    
    @Override
    public void remove() {
        for (GuiSignalConnection connection : new ArrayList<>(from))
            connection.disconnect(controller());
        for (GuiSignalConnection connection : new ArrayList<>(to))
            connection.disconnect(controller());
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed, @Nullable SignalContext testContext) throws GeneratePatternException {
        reset();
        if (from.isEmpty())
            throw new GeneratePatternException(this, "empty");
        if (processed.contains(this))
            throw new GeneratePatternException(this, "circular");
        processed.add(this);
        if (from.size() == 1)
            return from.get(0).from().generateCondition(processed, testContext);
        List<SignalInputCondition> parsed = new ArrayList<>();
        for (int i = 0; i < from.size(); i++)
            try {
                parsed.add(from.get(i).from().generateCondition(new ArrayList<>(processed), testContext));
            } catch (GeneratePatternException e) {}
        
        if (parsed.isEmpty())
            throw new GeneratePatternException(this, "novalidchildren");
        
        SignalInputCondition condition;
        if (parsed.size() == 1)
            condition = parsed.get(0);
        else
            condition = operator.create(parsed.toArray(new SignalInputCondition[parsed.size()]), position());
        
        if (testContext != null) {
            if (testLabel == null)
                testLabel = new GuiLabel("");
            insertControlBefore(button, testLabel);
            testCondition = condition;
            this.testContext = testContext;
            testInputChanged();
        }
        
        return condition;
        
    }
    
    @Override
    public void resetTest() {
        if (testLabel != null)
            remove(testLabel);
        testCondition = null;
    }
    
    @Override
    public void testInputChanged() {
        if (testCondition == null || testLabel == null)
            return;
        
        testLabel.setTitle(controller().testSignalOutput(testCondition.test(testContext, false), -1));
    }
    
}
