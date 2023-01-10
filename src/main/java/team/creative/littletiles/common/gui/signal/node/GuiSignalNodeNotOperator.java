package team.creative.littletiles.common.gui.signal.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNot;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalInputConditionNotBitwise;

public class GuiSignalNodeNotOperator extends GuiSignalNode {
    
    public final boolean bitwise;
    private GuiSignalConnection from;
    private List<GuiSignalConnection> to = new ArrayList<>();
    
    public GuiSignalNodeNotOperator(boolean bitwise) {
        super(bitwise ? "b-not" : "not");
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
    public void removeConnection(GuiSignalConnection connection) {
        if (connection.to() == this)
            from = null;
        else
            to.remove(connection);
    }
    
    @Override
    public Iterator<GuiSignalConnection> iterator() {
        return new Iterator<GuiSignalConnection>() {
            
            public int index = 0;
            public Iterator<GuiSignalConnection> iterator = to.iterator();
            
            @Override
            public boolean hasNext() {
                if (index == 0)
                    return from != null || iterator.hasNext();
                else if (index == 1)
                    return iterator.hasNext();
                return false;
            }
            
            @Override
            public GuiSignalConnection next() {
                if (index == 0) {
                    index++;
                    if (from != null)
                        return from;
                    else {
                        index++;
                        return iterator.next();
                    }
                } else if (index == 1)
                    return iterator.next();
                
                throw new UnsupportedOperationException();
            }
        };
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
            from.remove();
        for (GuiSignalConnection connection : new ArrayList<>(to))
            connection.remove();
    }
    
    @Override
    public int indexOf(GuiSignalConnection connection) {
        if (connection.to() == this)
            return 0;
        return to.indexOf(connection);
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        if (from == null)
            throw new GeneratePatternException(this, "empty");
        if (processed.contains(this))
            throw new GeneratePatternException(this, "circular");
        processed.add(this);
        return bitwise ? new SignalInputConditionNotBitwise(from.from().generateCondition(processed)) : new SignalInputConditionNot(from.from().generateCondition(processed));
    }
    
}
