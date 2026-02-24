package team.creative.littletiles.client.tool.mode;

import java.util.List;

import net.neoforged.neoforge.client.event.InputEvent;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;

public abstract class BuildingModeFeature {
    
    public abstract void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures);
    
    public abstract void remove(OverlayGuiLayer gui);
    
    public void keyPressed(InputEvent.Key key) {}
    
}
