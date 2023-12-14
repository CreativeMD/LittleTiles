package team.creative.littletiles.common.gui.signal.node;

import java.util.Iterator;
import java.util.List;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.util.type.itr.IterableIterator;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;

public class GuiSignalNodeOutput extends GuiSignalNodeComponent {
    
    public GuiSignalConnection from;
    
    public GuiSignalNodeOutput(GuiSignalComponent component) {
        super(component);
        setTitle(Component.translatable("gui.signal.out").append(": " + component.name()));
    }
    
    @Override
    public boolean canConnectTo(GuiSignalNode node) {
        return false;
    }
    
    @Override
    public boolean canConnectFrom(GuiSignalNode node) {
        return from == null;
    }
    
    @Override
    public Iterator<GuiSignalConnection> iterator() {
        return new Iterator<GuiSignalConnection>() {
            
            public boolean has = from != null;
            
            @Override
            public boolean hasNext() {
                return has;
            }
            
            @Override
            public GuiSignalConnection next() {
                has = false;
                return from;
            }
        };
    }
    
    @Override
    public Iterable<GuiSignalConnection> toConnections() {
        return new IterableIterator<GuiSignalConnection>() {
            
            @Override
            public boolean hasNext() {
                return false;
            }
            
            @Override
            public GuiSignalConnection next() {
                return null;
            }
            
        };
    }
    
    @Override
    public GuiSignalConnection getConnectionTo(GuiSignalNode node) {
        return null;
    }
    
    @Override
    public void disconnect(GuiSignalConnection connection) {
        from = null;
    }
    
    @Override
    public void connect(GuiSignalConnection connection) {
        from = connection;
    }
    
    @Override
    public void remove() {
        if (from != null)
            from.disconnect(controller());
    }
    
    @Override
    public int indexOf(GuiSignalConnection connection) {
        return 0;
    }
    
    @Override
    public SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException {
        reset();
        if (from == null)
            throw new GeneratePatternException(this, "empty");
        return from.from().generateCondition(processed);
    }
    
    @Override
    public boolean removable() {
        return false;
    }
    
}
