package team.creative.littletiles.common.gui.signal.node;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;
import team.creative.littletiles.common.structure.signal.logic.SignalInputConditionGate;

public class GuiSignalNodeGate extends GuiSignalNode {
    
    public static final GuiSignalNodeAnchor UPPER_BUTTON = new GuiSignalNodeAnchor<GuiSignalNodeGate>() {
        
        @Override
        public float x(GuiSignalNodeGate node) {
            return node.upper.rect.centerX();
        }
        
        @Override
        public float y(GuiSignalNodeGate node) {
            return node.upper.rect.centerY();
        }
        
    };
    
    public final boolean invert;
    public GuiSignalConnection first;
    public GuiSignalConnection second;
    public List<GuiSignalConnection> to = new ArrayList<>();
    public GuiParent upper;
    
    public GuiSignalNodeGate(boolean invert, SignalPosition position) {
        super(invert ? "ngate" : "gate", position);
        this.invert = invert;
    }
    
    @Override
    protected void beforeAddingButton() {
        super.beforeAddingButton();
        upper = new GuiParent();
        add(upper.setScale(0.5));
        upper.add(new GuiButton("", x -> {
            if (x == 1) {
                if (first != null)
                    first.disconnect(controller());
            } else
                controller().selectOrConnect(GuiSignalNodeGate.this, from -> from ? UPPER_BUTTON : null, false);
        }).setTitle(Component.literal(" ")));
    }
    
    @Override
    protected GuiSignalNodeAnchor buttonAnchor(boolean from) {
        if (from)
            return GuiSignalNodeAnchor.LEFT;
        return null;
    }
    
    @Override
    public GuiSignalNodeAnchor connectionAnchor(boolean from, GuiSignalNode other) {
        if (!from)
            return first == null ? UPPER_BUTTON : GuiSignalNodeAnchor.LEFT;
        return super.connectionAnchor(from, other);
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
        if (first != null && first.from() == node)
            return false;
        if (second != null && second.from() == node)
            return false;
        
        if (anchor == null)
            return first == null || second == null;
        if (anchor == UPPER_BUTTON)
            return first == null;
        return second == null;
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
            if (connection.toAnchor() == GuiSignalNodeAnchor.LEFT)
                second = connection;
            else
                first = connection;
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
        return new SignalInputConditionGate(parsed.get(0), invert, parsed.get(1), position());
    }
    
}
