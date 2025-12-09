package team.creative.littletiles.common.gui.signal.node;

import java.util.ArrayList;
import java.util.List;

import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicComparator;

public class GuiSignalNodeComparator extends GuiSignalNode {
    
    public SignalLogicComparator comparater;
    
    public GuiSignalConnection first;
    public GuiSignalConnection second;
    public List<GuiSignalConnection> to = new ArrayList<>();
    
    public GuiSignalNodeComparator(SignalLogicComparator comparater, SignalPosition position) {
        super(comparater.operator, position);
        this.comparater = comparater;
    }
    
    @Override
    public GuiSignalNodeAnchor connectionAnchor(boolean from, GuiSignalNode other) {
        if (!from)
            return first == null ? GuiSignalNodeAnchor.TOP : GuiSignalNodeAnchor.BOTTOM;
        return super.connectionAnchor(from, other);
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
        if (first != null && first.from() == node)
            return false;
        if (second != null && second.from() == node)
            return false;
        return first == null || second == null;
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
        if (connection.to() == this) {
            if (first == connection)
                first = null;
            else
                second = null;
        } else
            to.remove(connection);
    }
    
    @Override
    public Iterable<GuiSignalConnection> toConnections() {
        return to;
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        if (connection.to() == this) {
            if (first == null)
                first = connection;
            else
                second = connection;
        } else
            to.add(connection);
    }
    
    @Override
    public void remove() {
        if (first != null)
            first.disconnect(controller());
        if (second != null)
            second.disconnect(controller());
        for (GuiSignalConnection connection : new ArrayList<>(to))
            connection.disconnect(controller());
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        if (first == null && second == null)
            throw new GeneratePatternException(this, "empty");
        if (processed.contains(this))
            throw new GeneratePatternException(this, "circular");
        processed.add(this);
        List<SignalInputCondition> parsed = new ArrayList<>();
        if (first != null)
            parsed.add(first.from().generateCondition(new ArrayList<>(processed)));
        if (second != null)
            parsed.add(second.from().generateCondition(new ArrayList<>(processed)));
        
        if (parsed.isEmpty())
            throw new GeneratePatternException(this, "novalidchildren");
        if (parsed.size() == 1)
            return parsed.get(0);
        return comparater.create(parsed.toArray(new SignalInputCondition[parsed.size()]), position());
    }
    
}
