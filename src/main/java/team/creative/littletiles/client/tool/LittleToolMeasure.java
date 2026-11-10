package team.creative.littletiles.client.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
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
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.item.component.MeasurementsComponent;
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxLevel;
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
    
    private LittleBoxLevel last;
    private List<LittleBoxLevel> selected = new ArrayList<>();
    
    private int marked;
    private LittleBoxAbsolute markedPosition;
    private RenderMeasurementResults result;
    
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
            if (component != null)
                measurements.addAll(component.value(renderer.level()));
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
            result.render(renderer, lines, cam);
        
        var blockHit = renderer.blockHit();
        var player = renderer.player();
        var level = renderer.blockHitLevel();
        
        var context = renderer.blockHitContext();
        
        if (blockHit != null)
            last = new LittleBoxLevel(context, new ShapePosition(player, level, PlacementHelper.getPosition(level, blockHit, measure.getPositionGrid(player,
                stack)), blockHit, false, true).box().copy());
        
        List<LittleBoxLevel> positions = new ArrayList<>();
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
        result = RenderMeasurementResults.of(renderer, measurements);
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
            Map<LittleBoxLevel, LittleMeasurement> map = new Object2ObjectArrayMap<>();
            List<LittleBoxLevel> temp = new ArrayList<>();
            for (LittleMeasurement measurement : measurements) {
                measurement.collectPositions(temp);
                for (LittleBoxLevel pos : temp)
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
    
    public static record RenderMeasurementResults(ByteBufferBuilder buffer, List<RenderMeasurementResult> results) {
        
        public static RenderMeasurementResults of(PreviewRenderer renderer, List<LittleMeasurement> measurements) {
            if (measurements.isEmpty())
                return null;
            
            var cam = MC.gameRenderer.getMainCamera().getPosition();
            
            var buffer = renderer.createBuffer();
            
            List<RenderMeasurementResult> results = new ArrayList<>();
            PoseStack pose = new PoseStack();
            for (LittleMeasurement measurement : measurements) {
                pose.pushPose();
                var builder = renderer.createBuilder(buffer, true);
                var result = measurement.build(renderer, pose, builder, cam);
                if (result != null)
                    results.add(result);
                else
                    builder.build();
                pose.popPose();
            }
            return new RenderMeasurementResults(buffer, results);
        }
        
        public void render(PreviewRenderer renderer, boolean lines, Vec3 cam) {
            for (RenderMeasurementResult render : results)
                renderer.renderSeethroughLines(render.context, cam, lines, render.pos, render.data, -1);
        }
        
        public void close() {
            for (int i = 0; i < results.size(); i++)
                results.get(i).close();
            buffer.close();
        }
        
    }
    
    public static class RenderMeasurementResult {
        
        public final ILittleLevelContext context;
        public final BlockPos pos;
        public final MeshData data;
        
        public RenderMeasurementResult(ILittleLevelContext context, BlockPos pos, BufferBuilder builder) {
            this.context = context;
            this.pos = pos;
            var mesh = builder.build();
            if (mesh instanceof MeshDataExtender m)
                m.keepAlive(true);
            this.data = mesh;
        }
        
        public void close() {
            if (data instanceof MeshDataExtender m) {
                m.keepAlive(false);
                data.close();
            }
        }
    }
}
