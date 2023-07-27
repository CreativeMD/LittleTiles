package mcp.mobius.waila.api.event;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/** The base event for rendering the Waila tooltip. This provides the opportunity to do last minute changes to the tooltip.
 * <p>
 * All sub-events are fired from {@link mcp.mobius.waila.overlay.OverlayRenderer#renderOverlay(mcp.mobius.waila.overlay.Tooltip)}.
 * All sub-events are fired every render tick.
 * <p>
 * {@link #xPos} - The X location that the tooltip is being rendered at.
 * {@link #yPos} - The Y location that the tooltip is being rendered at.
 * {@link #width} - The width of the tooltip.
 * {@link #height} - The height of the tooltip. */
public class WailaRenderEvent extends Event {
    
    private int xPos;
    private int yPos;
    private int width;
    private int height;
    
    public WailaRenderEvent(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
    }
    
    public int getX() {
        return xPos;
    }
    
    protected void setxPos(int xPos) {
        this.xPos = xPos;
    }
    
    public int getY() {
        return yPos;
    }
    
    protected void setyPos(int yPos) {
        this.yPos = yPos;
    }
    
    public int getWidth() {
        return width;
    }
    
    protected void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    protected void setHeight(int height) {
        this.height = height;
    }
    
    /** This event is fired just before the Waila tooltip is rendered and right after setting up the GL state in
     * {@link mcp.mobius.waila.overlay.OverlayRenderer#renderOverlay(mcp.mobius.waila.overlay.Tooltip)}
     * <p>
     * This event is cancelable.
     * If this event is canceled, the tooltip will not render. */
    @Cancelable
    public static class Pre extends WailaRenderEvent {
        
        private final IWailaCommonAccessor accessor;
        
        public Pre(IWailaCommonAccessor accessor, int xPos, int yPos, int width, int height) {
            super(xPos, yPos, width, height);
            
            this.accessor = accessor;
        }
        
        public IWailaCommonAccessor getAccessor() {
            return accessor;
        }
        
        @Override
        public void setxPos(int xPos) {
            super.setxPos(xPos);
        }
        
        @Override
        public void setyPos(int yPos) {
            super.setyPos(yPos);
        }
        
        @Override
        public void setWidth(int width) {
            super.setWidth(width);
        }
        
        @Override
        public void setHeight(int height) {
            super.setHeight(height);
        }
    }
    
    /** This event is fired just after the tooltip is rendered and right before the GL state is reset in
     * {@link mcp.mobius.waila.overlay.OverlayRenderer#renderOverlay(mcp.mobius.waila.overlay.Tooltip)}
     * This event is only fired if {@link Pre} is not cancelled.
     * <p>
     * This event is not cancelable. */
    public static class Post extends WailaRenderEvent {
        
        public Post(int xPos, int yPos, int width, int height) {
            super(xPos, yPos, width, height);
        }
    }
}
