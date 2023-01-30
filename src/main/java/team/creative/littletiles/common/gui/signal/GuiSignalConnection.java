package team.creative.littletiles.common.gui.signal;

import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public record GuiSignalConnection(GuiSignalNode from, GuiSignalNode to) {
    
    public void disconnect(GuiSignalController controller) {
        from.disconnect(this);
        to.disconnect(this);
        
        controller.raiseEvent(new GuiControlChangedEvent(controller));
    }
    
}
