package team.creative.littletiles.client.render.overlay;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.container.SubGui;

public class OverlayGui extends SubGui {
    
    protected List<OverlayControl> overlayControls = new ArrayList<>();
    
    public OverlayGui() {
        borderWidth = 0;
        marginWidth = 0;
        setStyle(Style.emptyStyle);
    }
    
    @Override
    public void createControls() {
        
    }
    
    @Override
    public boolean isTopLayer() {
        return true;
    }
    
    @Override
    public int getLayerID() {
        return 0;
    }
    
    @Override
    public void onTick() {
        super.onTick();
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
