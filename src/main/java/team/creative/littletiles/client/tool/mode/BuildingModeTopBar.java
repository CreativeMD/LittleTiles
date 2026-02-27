package team.creative.littletiles.client.tool.mode;

import java.util.List;

import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.parent.GuiPanel;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayPosition;
import team.creative.littletiles.client.tool.LittleTool;

public class BuildingModeTopBar extends BuildingModeFeature {
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {
        GuiPanel panel = new GuiPanel("top_bar");
        panel.flow = GuiFlow.FIT_X;
        gui.addOverlayControl(panel, OverlayPosition.TOP_STRETCH);
        for (BuildingModeFeature feature : allFeatures)
            if (feature instanceof BuildingModeInfo info)
                info.createInfo(panel);
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        gui.remove("top_bar");
    }
    
    public static interface BuildingModeInfo {
        
        public void createInfo(GuiParent parent);
        
    }
    
}
