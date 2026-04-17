package team.creative.littletiles.client.tool;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.math.measure.LittleMeasurement;

public abstract class LittleTool {
    
    public ItemStack stack;
    private boolean buildingMode;
    private List<BuildingModeFeature> features;
    
    public LittleTool(ItemStack stack) {
        this.stack = stack;
    }
    
    public final void tick(PreviewRenderer renderer) {
        if (buildingMode)
            for (BuildingModeFeature feature : features)
                feature.tick(renderer);
        tickInternal(renderer);
    }
    
    public final void render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        boolean rendered = false;
        if (buildingMode)
            for (BuildingModeFeature feature : features)
                rendered |= feature.render(renderer, pose, cam, lines);
        if (!rendered)
            renderInternal(renderer, pose, cam, lines);
    }
    
    protected abstract void tickInternal(PreviewRenderer renderer);
    
    protected abstract void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines);
    
    public void renderGui(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {}
    
    protected void disableBuildingMode() {
        for (BuildingModeFeature feature : features)
            feature.remove(LittleTilesClient.OVERLAY_RENDERER.gui());
        buildingMode = false;
    }
    
    public boolean keyPressed(PreviewRenderer renderer, int keyCode, int scanCode, int action, int modifiers) {
        if (buildingMode)
            for (BuildingModeFeature feature : features)
                if (feature.keyPressed(keyCode, scanCode, action, modifiers))
                    return true;
        return false;
    }
    
    public void startBuildingMode() {
        features = buildingFeatures();
        if (buildingMode = features != null) {
            var gui = LittleTilesClient.OVERLAY_RENDERER.gui();
            for (BuildingModeFeature feature : features)
                feature.create(gui, this, features);
            gui.reflow();
            
        }
    }
    
    public boolean toolKeyPressed(PreviewRenderer renderer, KeyMapping key) {
        if (key == LittleTilesClient.KEY_BUILDING_MODE) {
            if (buildingMode)
                disableBuildingMode();
            else if (hasBuildingMode())
                startBuildingMode();
            return true;
        }
        return false;
    }
    
    public boolean hasBuildingMode() {
        return true;
    }
    
    public boolean buildingMode() {
        return buildingMode;
    }
    
    public List<BuildingModeFeature> buildingFeatures() {
        List<BuildingModeFeature> features = new ArrayList<>();
        features.add(BuildingModeFeatures.TOP_BAR);
        features.add(BuildingModeFeatures.ZOOM);
        features.add(BuildingModeFeatures.GRID);
        features.add(BuildingModeFeatures.RULES);
        features.add(BuildingModeFeatures.INCLUDE);
        features.add(BuildingModeFeatures.EXCLUDE);
        features.add(BuildingModeFeatures.MIRRORS);
        return features;
    }
    
    public void remove() {
        if (buildingMode)
            disableBuildingMode();
    }
    
    public void mouseInput(InputEvent.MouseButton.Pre event) {}
    
    public boolean onRightClick(PreviewRenderer renderer, BlockHitResult result) {
        return true;
    }
    
    public boolean onLeftClick(PreviewRenderer renderer, BlockHitResult result) {
        return false;
    }
    
    public boolean onMouseWheelClickBlock(PreviewRenderer renderer, BlockHitResult result) {
        return false;
    }
    
    public LittleAction prepareAction(LittleAction action) {
        if (buildingMode)
            for (BuildingModeFeature feature : features)
                action = feature.prepareAction(action);
        return action;
    }
    
    public Component tooltip() {
        if (buildingMode)
            for (BuildingModeFeature feature : features) {
                var tooltip = feature.tooltip();
                if (tooltip != null)
                    return tooltip;
            }
        return null;
    }
    
    public Iterable<LittleMeasurement> measurements() {
        return null;
    }
    
}
