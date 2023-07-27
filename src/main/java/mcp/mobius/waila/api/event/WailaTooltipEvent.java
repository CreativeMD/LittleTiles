package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

/** This event is fired just before the Waila tooltip sizes are calculated. This is the last chance to make edits to
 * the information being displayed.
 * <p>
 * This event is not cancelable.
 * <p>
 * {@link #currentTip} - The current tooltip to be drawn. */
public class WailaTooltipEvent extends Event {
    
    private final List<String> currentTip;
    private final IWailaCommonAccessor accessor;
    
    public WailaTooltipEvent(List<String> currentTip, IWailaCommonAccessor accessor) {
        this.currentTip = currentTip;
        this.accessor = accessor;
    }
    
    public List<String> getCurrentTip() {
        return currentTip;
    }
    
    public IWailaCommonAccessor getAccessor() {
        return accessor;
    }
}
