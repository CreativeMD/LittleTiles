package team.creative.littletiles.common.gui.signal.node;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNot;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNotBitwise;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public class GuiSignalNodeNotOperator extends GuiSignalNode {
    
    public final boolean bitwise;
    private GuiSignalConnection from;
    private List<GuiSignalConnection> to = new ArrayList<>();
    
    public GuiSignalNodeNotOperator(boolean bitwise, @Nullable SignalPosition position) {
        super(bitwise ? "b-not" : "not", position);
        this.bitwise = bitwise;
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node) {
        for (GuiSignalConnection connectTo : to)
            if (connectTo.to() == node)
                return false;
        return true;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node) {
        return from == null;
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
            from = null;
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
            from = connection;
        else
            to.add(connection);
    }
    
    @Override
    public void remove() {
        if (from != null)
            from.disconnect(controller());
        for (GuiSignalConnection connection : new ArrayList<>(to))
            connection.disconnect(controller());
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        if (from == null)
            throw new GeneratePatternException(this, "empty");
        if (processed.contains(this))
            throw new GeneratePatternException(this, "circular");
        processed.add(this);
        return bitwise ? new SignalInputConditionNotBitwise(from.from().generateCondition(processed), position()) : new SignalInputConditionNot(from.from().generateCondition(
            processed), position());
    }
    
}
