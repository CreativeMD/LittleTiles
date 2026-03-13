package team.creative.littletiles.client.tool.mode;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.grid.LittleGrid;

public abstract class BuildingModeFeature {
    
    public LittleGrid positionGrid() {
        var renderer = LittleTilesClient.PREVIEW_RENDERER.renderer;
        var player = renderer.player();
        var stack = renderer.manager.tool().stack;
        return ((ILittleTool) stack.getItem()).getPositionGrid(player, stack);
    }
    
    public abstract void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures);
    
    public abstract void remove(OverlayGuiLayer gui);
    
    public void tick(PreviewRenderer renderer) {}
    
    /** if returns true the tool will not render anything. Other features are not affected by this */
    public boolean render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        return false;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        return false;
    }
    
    public Component tooltip() {
        return null;
    }
    
    public LittleAction prepareAction(LittleAction action) {
        return action;
    }
    
    public void unloadLevel() {}
}
