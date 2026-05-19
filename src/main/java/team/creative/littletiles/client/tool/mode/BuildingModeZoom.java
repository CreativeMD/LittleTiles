package team.creative.littletiles.client.tool.mode;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.tool.LittleTool;

public class BuildingModeZoom extends BuildingModeFeature {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private static final double MIN_ZOOM = 1;
    private static final double MAX_ZOOM = 16;
    private static boolean RENDERING_HAND = false;
    private boolean active;
    
    private SmoothValue zoom = new SmoothValue(200, 1);
    private double scrollPosition;
    private int level = 0;
    
    @CreativeConfig
    private double scrollSpeed = 0.25;
    
    @CreativeConfig
    public List<Double> levels = Arrays.asList(1D, 2D, 4D, 8D, 16D);
    
    public BuildingModeZoom() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {
        active = true;
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        active = false;
    }
    
    private boolean active() {
        return active && MC.player != null && MC.screen == null;
    }
    
    private double zoomLevel() {
        double zoom;
        if (level >= levels.size())
            if (levels.isEmpty())
                zoom = 1;
            else
                zoom = levels.getLast();
        else
            zoom = levels.get(level);
        return Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
    }
    
    private double zoom() {
        return zoom.current();
    }
    
    @SubscribeEvent
    public void onRenderTick(RenderFrameEvent.Pre event) {
        RENDERING_HAND = true;
    }
    
    @SubscribeEvent
    public void fov(ComputeFov event) {
        if (!active())
            return;
        
        if (RENDERING_HAND) {
            zoom.tick();
            event.setFOV(Math.toDegrees(Math.atan(Math.tan(Math.toRadians(event.getFOV())) / zoom())));
        }
        RENDERING_HAND = !RENDERING_HAND;
    }
    
    @SubscribeEvent
    public void turn(CalculatePlayerTurnEvent event) {
        if (!active())
            return;
        event.setMouseSensitivity(event.getMouseSensitivity() / zoom());
    }
    
    @SubscribeEvent
    public void scroll(InputEvent.MouseScrollingEvent event) {
        if (!active() || BuildingModeFeatures.GRID.isKeyDown())
            return;
        
        scrollPosition = Math.clamp(scrollPosition + event.getScrollDeltaY() * scrollSpeed, 0, levels.size());
        int levelBefore = level;
        level = (int) scrollPosition;
        
        if (levelBefore != level) {
            this.zoom.set(zoomLevel());
            MC.player.displayClientMessage(Component.translatable("building.zoom.message", zoomLevel()), true);
        }
        event.setCanceled(true);
    }
    
}
