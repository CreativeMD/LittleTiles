package team.creative.littletiles.common.gui.signal.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team.creative.creativecore.common.util.type.itr.ConsecutiveIterator;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;

public class GuiSignalNodeOperator extends GuiSignalNode {
    
    public final SignalLogicOperator operator;
    private List<GuiSignalConnection> from = new ArrayList<>();
    private List<GuiSignalConnection> to = new ArrayList<>();
    
    public GuiSignalNodeOperator(SignalLogicOperator operator) {
        super(operator.display);
        this.operator = operator;
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
    public Iterator<GuiSignalConnection> iterator() {
        return new ConsecutiveIterator<>(from.iterator(), to.iterator());
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
    public int indexOf(GuiSignalConnection connection) {
        if (connection.to() == this)
            return from.indexOf(connection);
        else
            return to.indexOf(connection);
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        if (from.isEmpty())
            throw new GeneratePatternException(this, "empty");
        if (processed.contains(this))
            throw new GeneratePatternException(this, "circular");
        processed.add(this);
        if (from.size() == 1)
            return from.get(0).from().generateCondition(processed);
        List<SignalInputCondition> parsed = new ArrayList<>();
        for (int i = 0; i < from.size(); i++)
            try {
                parsed.add(from.get(i).from().generateCondition(new ArrayList<>(processed)));
            } catch (GeneratePatternException e) {}
        
        if (parsed.isEmpty())
            throw new GeneratePatternException(this, "novalidchildren");
        if (parsed.size() == 1)
            return parsed.get(0);
        return operator.create(parsed.toArray(new SignalInputCondition[parsed.size()]));
    }
    
}
