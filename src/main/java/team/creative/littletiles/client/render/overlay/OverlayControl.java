package team.creative.littletiles.client.render.overlay;

import java.util.function.BooleanSupplier;

import team.creative.creativecore.common.gui.GuiControl;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayPositionType;

public class OverlayControl {
    
    public final GuiControl control;
    public OverlayPositionType position;
    
    public OverlayGui parent;
    
    protected BooleanSupplier shouldRender = null;
    
    public OverlayControl(GuiControl control, OverlayPositionType position) {
        this.control = control;
        this.position = position;
    }
    
    public OverlayControl setShouldRender(BooleanSupplier supplier) {
        this.shouldRender = supplier;
        return this;
    }
    
    public boolean shouldRender() {
        if (shouldRender == null)
            return true;
        return shouldRender.getAsBoolean();
    }
    
    public void onTick() {
        
    }
    
    public void resize() {
        
    }
    
}
