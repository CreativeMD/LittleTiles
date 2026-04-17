package team.creative.littletiles.client.tool.mode;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer.BoxRenderResult;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;

public class BuildingModeMeasures extends BuildingModeToggle implements BuildingModeInfo {
    
    private BoxRenderResult result;
    
    public BuildingModeMeasures() {
        super("building.toggle.proportional", InputConstants.KEY_G, KeyModifier.NONE, false);
    }
    
    public void updateMeasureTapes() {
        
    }
    
    @Override
    public boolean render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (lines || !enabled())
            return false;
        var measures = renderer.manager.tool().measurements();
        if (measures == null)
            return false;
        
        /*for (LittleMeasurement measure : measures) {
            measure.render(renderer);
        }*/
        
        return false;
    }
}
