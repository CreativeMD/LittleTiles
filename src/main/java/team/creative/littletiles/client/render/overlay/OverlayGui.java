package team.creative.littletiles.client.render.overlay;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiLayer;

public class OverlayGui extends GuiLayer {
    
    protected List<OverlayControl> overlayControls = new ArrayList<>();
    
    public OverlayGui() {
        super("overlay");
    }
    
    @Override
    public void create() {
        
    }
    
    @Override
    public void tick() {
        super.tick();
        for (int i = 0; i < overlayControls.size(); i++)
            overlayControls.get(i).onTick();
    }
    
    public void resize() {
        for (int i = 0; i < overlayControls.size(); i++) {
            OverlayControl control = overlayControls.get(i);
            control.resize();
            positionControl(control);
        }
    }
    
    public void add(OverlayControl control) {
        control.parent = this;
        overlayControls.add(control);
        positionControl(control);
        controls.add(control.control);
        updateControl(control.control, controls.size() - 1);
    }
    
    public boolean remove(OverlayControl control) {
        controls.remove(control.control);
        return overlayControls.remove(control);
    }
    
    public void positionControl(OverlayControl control) {
        control.position.positionControl(control.control);
    }
    
    @Override
    public boolean shouldRenderControl(GuiControl control, int index) {
        if (super.shouldRenderControl(control, index) && index < overlayControls.size())
            return overlayControls.get(index).shouldRender();
        return false;
    }
    
}
