package team.creative.littletiles.client.tool.mode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.settings.KeyModifier;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.api.common.tool.ILittleMeasure;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer.BoxRenderResult;
import team.creative.littletiles.client.tool.mode.BuildingModeTopBar.BuildingModeInfo;
import team.creative.littletiles.common.math.measure.LittleMeasurement;

public class BuildingModeMeasures extends BuildingModeToggle implements BuildingModeInfo {
    
    private BoxRenderResult result;
    private List<LittleMeasurement> tapes = new ArrayList<>();
    private int inventoryChanged;
    private boolean builtCache = false;
    
    private List<LittleMeasurement> toolMeasurements;
    
    @CreativeConfig
    public List<Color> measureTapeColors = Arrays.asList(new Color(252, 186, 3), new Color(205, 144, 0), new Color(177, 102, 0), new Color(159, 73, 0), new Color(129, 59, 0),
        new Color(98, 40, 0));
    
    public BuildingModeMeasures() {
        super("building.toggle.measurements", InputConstants.KEY_N, KeyModifier.NONE, false);
    }
    
    public int colorFromIndex(int index) {
        if (measureTapeColors.isEmpty())
            return ColorUtils.WHITE;
        return measureTapeColors.get(index % measureTapeColors.size()).toInt();
    }
    
    public void updateMeasureTapes(PreviewRenderer renderer) {
        removeCache();
        inventoryChanged = renderer.player().getInventory().getTimesChanged();
        tapes.clear();
        for (ItemStack stack : renderer.player().getInventory().items)
            if (stack.getItem() instanceof ILittleMeasure m)
                tapes.addAll(m.getMeasurements(stack));
        result = buildTapes(renderer, tapes);
        builtCache = true;
    }
    
    private void removeCache() {
        if (result != null) {
            result.close();
            result = null;
        }
        builtCache = false;
    }
    
    @Override
    public void tick(PreviewRenderer renderer) {
        super.tick(renderer);
        if (renderer.player().getInventory().getTimesChanged() != inventoryChanged || !builtCache)
            updateMeasureTapes(renderer);
    }
    
    @Override
    public boolean render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (!lines || !enabled())
            return false;
        
        renderer.setupPreviewRenderer(true);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        
        if (result != null)
            renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), -1);
        
        toolMeasurements = renderer.manager.tool().measurements();
        if (toolMeasurements != null) {
            var result = buildTapes(renderer, toolMeasurements);
            if (result != null)
                renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), -1);
            result.close();
            result = null;
        }
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.applyModelViewMatrix();
        
        return false;
    }
    
    @Override
    public void renderGui(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        super.renderGui(renderer, overlay, cam);
        if (!enabled())
            return;
        for (LittleMeasurement measurement : tapes)
            measurement.overlay(renderer, overlay, cam);
        if (toolMeasurements != null)
            for (LittleMeasurement measurement : toolMeasurements)
                measurement.overlay(renderer, overlay, cam);
    }
    
    private BoxRenderResult buildTapes(PreviewRenderer renderer, List<LittleMeasurement> measurements) {
        if (measurements.isEmpty())
            return null;
        
        ByteBufferBuilder buffer = renderer.createBuffer();
        var builder = renderer.createBuilder(buffer, true);
        
        BlockPos pos = renderer.player().blockPosition();
        
        PoseStack pose = new PoseStack();
        pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        
        for (LittleMeasurement measurement : measurements)
            measurement.build(renderer, pose, builder);
        
        var mesh = builder.build();
        if (mesh instanceof MeshDataExtender m)
            m.keepAlive(true);
        return new BoxRenderResult(null, pos, buffer, mesh);
    }
    
    @Override
    protected void changed() {
        super.changed();
        if (!enabled())
            removeCache();
    }
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        removeCache();
    }
    
    @Override
    public void unloadLevel() {
        tapes.clear();
        removeCache();
    }
}
