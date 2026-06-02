package team.creative.littletiles.client.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent.MouseButton.Pre;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleMeasure;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.mc.MeshDataExtender;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer.BoxRenderResult;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.item.component.MeasurementsComponent;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.measure.LittleMeasurement;
import team.creative.littletiles.common.math.measure.LittleMeasurementType;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.packet.item.MeasurementPacket;
import team.creative.littletiles.common.placement.PlacementHelper;

public class LittleToolMeasure extends LittleTool {
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    public final ILittleMeasure measure;
    private List<LittleMeasurement> measurements = new ArrayList<>();
    private MeasurementsComponent component;
    
    private LittleBoxAbsolute last;
    private List<LittleBoxAbsolute> selected = new ArrayList<>();
    
    private int marked;
    private LittleBoxAbsolute markedPosition;
    private BoxRenderResult result;
    
    private int lastMouseKey = -1;
    private double lastMouseClicked;
    
    public LittleToolMeasure(ItemStack stack) {
        super(stack);
        measure = (ILittleMeasure) stack.getItem();
    }
    
    public void reset() {
        measurements.clear();
        selected.clear();
        marked = -1;
        markedPosition = null;
        removeCache();
    }
    
    @Override
    protected void tickInternal(PreviewRenderer renderer) {
        if (stack.get(LittleTilesRegistry.MEASUREMENTS) != component) {
            component = stack.get(LittleTilesRegistry.MEASUREMENTS);
            reset();
            measurements.addAll(component.value());
            buildBoxes(renderer);
        }
    }
    
    @Override
    protected void renderInternal(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (!lines)
            return;
        renderer.setupPreviewRenderer(true);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        
        if (result != null)
            renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), -1);
        
        var blockHit = renderer.blockHit();
        var player = renderer.player();
        var level = renderer.level();
        
        if (blockHit != null)
            last = new ShapePosition(player, PlacementHelper.getPosition(level, blockHit, measure.getPositionGrid(player, stack)), blockHit, false, true).toAbsoluteBox();
        
        List<LittleBoxAbsolute> positions = new ArrayList<>();
        for (LittleMeasurement measurement : measurements)
            measurement.collectPositions(positions);
        positions.addAll(selected);
        positions.add(last);
        int markedIndex = positions.indexOf(markedPosition);
        renderer.renderBoxes(pose, cam, positions, x -> x == markedIndex);
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.applyModelViewMatrix();
        
    }
    
    @Override
    protected void renderGuiInternal(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam) {
        super.renderGuiInternal(renderer, overlay, cam);
        for (LittleMeasurement measurement : measurements)
            measurement.overlay(renderer, overlay, cam);
    }
    
    private void removeCache() {
        if (result != null) {
            result.close();
            result = null;
        }
    }
    
    @Override
    public boolean keyPressed(PreviewRenderer renderer, int keyCode, int scanCode, int action, int modifiers) {
        if (action == InputConstants.RELEASE)
            return super.keyPressed(renderer, keyCode, scanCode, action, modifiers);
        
        var facing = LittleTilesClient.facingFromKeybind(MC.player, keyCode, scanCode);
        if (facing != null && markedPosition != null) {
            var grid = measure.getPositionGrid(renderer.player(), stack);
            LittleVec vec = new LittleVec(facing);
            vec.scale(Screen.hasControlDown() ? grid.count : 1);
            var vecGrid = new LittleVecGrid(vec, grid);
            
            markedPosition.sameGrid(vecGrid, () -> markedPosition.box.add(vecGrid.getVec()));
            if (marked != -1)
                measurements.get(marked).changed();
            buildBoxes(renderer);
            return true;
        }
        
        return super.keyPressed(renderer, keyCode, scanCode, action, modifiers);
    }
    
    private void buildBoxes(PreviewRenderer renderer) {
        removeCache();
        
        if (measurements.isEmpty()) {
            result = null;
            return;
        }
        
        ByteBufferBuilder buffer = renderer.createBuffer();
        var builder = renderer.createBuilder(buffer, true);
        
        BlockPos pos = last != null ? last.pos : BlockPos.ZERO;
        
        PoseStack pose = new PoseStack();
        pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        
        for (LittleMeasurement measurement : measurements)
            measurement.build(renderer, pose, builder);
        
        var mesh = builder.build();
        if (mesh instanceof MeshDataExtender m)
            m.keepAlive(true);
        result = new BoxRenderResult(null, pos, buffer, mesh);
    }
    
    private void updateMeasurements() {
        component = MeasurementsComponent.of(measurements);
        var packet = new MeasurementPacket(component);
        packet.execute(MC.player);
        LittleTiles.NETWORK.sendToServer(packet);
    }
    
    @Override
    public void mouseInput(Pre event) {
        if (event.getAction() != InputConstants.PRESS || MC.player == null || MC.screen != null)
            return;
        
        var renderer = LittleTilesClient.PREVIEW_RENDERER.renderer;
        boolean doubleClick = lastMouseKey == event.getButton() && Blaze3D.getTime() - lastMouseClicked < ScreenEventListener.DOUBLE_CLICK_TIME;
        lastMouseKey = event.getButton();
        lastMouseClicked = Blaze3D.getTime();
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Map<LittleBoxAbsolute, LittleMeasurement> map = new Object2ObjectArrayMap<>();
            List<LittleBoxAbsolute> temp = new ArrayList<>();
            for (LittleMeasurement measurement : measurements) {
                measurement.collectPositions(temp);
                for (LittleBoxAbsolute pos : temp)
                    map.put(pos, measurement);
                temp.clear();
            }
            
            temp.addAll(map.keySet());
            temp.addAll(selected);
            
            if (LittleActionHandlerClient.isUsingSecondMode())
                if (doubleClick) {
                    reset();
                    updateMeasurements();
                } else {
                    int index = renderer.selectBox(temp);
                    if (index >= 0) {
                        var measurement = map.get(temp.get(index));
                        if (measurement != null) {
                            measurements.remove(measurement);
                            updateMeasurements();
                        }
                    }
                    marked = -1;
                    markedPosition = null;
                }
            else {
                int index = renderer.selectBox(temp);
                if (index >= 0) {
                    markedPosition = temp.get(index);
                    marked = measurements.indexOf(map.get(temp.get(index)));
                } else {
                    marked = -1;
                    markedPosition = null;
                }
            }
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            selected.add(last.copy());
            var type = stack.has(LittleTilesRegistry.MEASUREMENT_TYPE) ? stack.get(LittleTilesRegistry.MEASUREMENT_TYPE).type : LittleMeasurementType.REGISTRY.getDefault();
            if (type.points().apply(selected.size())) {
                var m = type.factory().apply(new ArrayList<>(selected));
                m.color = BuildingModeFeatures.MEASURES.colorFromIndex(measurements.size());
                measurements.add(m);
                selected.clear();
                updateMeasurements();
            }
        }
        
        event.setCanceled(true);
        buildBoxes(renderer);
    }
    
    @Override
    public List<BuildingModeFeature> buildingFeatures() {
        List<BuildingModeFeature> features = new ArrayList<>();
        features.add(BuildingModeFeatures.TOP_BAR);
        features.add(BuildingModeFeatures.ZOOM);
        features.add(BuildingModeFeatures.GRID);
        features.add(BuildingModeFeatures.CYCLE_MEASURES);
        return features;
    }
    
    public void unloadLevel() {
        measurements.clear();
        selected.clear();
        last = null;
        markedPosition = null;
        marked = -1;
        removeCache();
    }
    
}
