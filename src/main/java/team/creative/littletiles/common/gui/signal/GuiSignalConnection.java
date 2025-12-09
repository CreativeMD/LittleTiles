package team.creative.littletiles.common.gui.signal;

import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public record GuiSignalConnection(GuiSignalNode from, GuiSignalNodeAnchor fromAnchor, GuiSignalNode to, GuiSignalNodeAnchor toAnchor) {
    
    public static GuiSignalConnection connect(GuiSignalNode from, GuiSignalNode to) {
        var connection = new GuiSignalConnection(from, from.connectionAnchor(true, to), to, to.connectionAnchor(false, from));
        from.connect(connection);
        to.connect(connection);
        return connection;
    }
    
    public GuiSignalConnection(GuiSignalNode from, GuiSignalNode to) {
        this(from, GuiSignalNodeAnchor.RIGHT, to, GuiSignalNodeAnchor.LEFT);
    }
    
    public float fromX() {
        return fromAnchor.x(from);
    }
    
    public float fromY() {
        return fromAnchor.y(from);
    }
    
    public float toX() {
        return toAnchor.x(to);
    }
    
    public float toY() {
        return toAnchor.y(to);
    }
    
    public void disconnect(GuiSignalController controller) {
        from.disconnect(this);
        to.disconnect(this);
        
        controller.raiseEvent(new GuiControlChangedEvent(controller));
    }
    
}
