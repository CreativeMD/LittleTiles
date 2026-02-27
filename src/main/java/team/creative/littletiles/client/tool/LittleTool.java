package team.creative.littletiles.client.tool;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.mode.BuildingModeFeature;
import team.creative.littletiles.client.tool.mode.BuildingModeFeatures;

public abstract class LittleTool {
    
    public static final PoseStack EMPTY = new PoseStack();
    
    public ItemStack stack;
    private boolean buildingMode;
    private List<BuildingModeFeature> features;
    
    public LittleTool(ItemStack stack) {
        this.stack = stack;
    }
    
    public abstract void tick(Level level, Player player, @Nullable BlockHitResult blockHit);
    
    public abstract void render(Level level, Player player, PoseStack pose, Vec3 cam, boolean lines);
    
    protected void disableBuildingMode() {
        for (BuildingModeFeature feature : features)
            feature.remove(LittleTilesClient.OVERLAY_RENDERER.gui());
        buildingMode = false;
    }
    
    public void keyPressed(InputEvent.Key key) {
        if (buildingMode)
            for (BuildingModeFeature feature : features)
                feature.keyPressed(key);
    }
    
    public boolean toolKeyPressed(Level level, Player player, KeyMapping key) {
        if (key == LittleTilesClient.KEY_BUILDING_MODE) {
            if (buildingMode)
                disableBuildingMode();
            else if (hasBuildingMode()) {
                features = buildingFeatures();
                if (buildingMode = features != null) {
                    var gui = LittleTilesClient.OVERLAY_RENDERER.gui();
                    for (BuildingModeFeature feature : features)
                        feature.create(gui, this, features);
                    gui.reflow();
                    
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean hasBuildingMode() {
        return true;
    }
    
    public List<BuildingModeFeature> buildingFeatures() {
        List<BuildingModeFeature> features = new ArrayList<>();
        features.add(BuildingModeFeatures.TOP_BAR);
        features.add(BuildingModeFeatures.ZOOM);
        features.add(BuildingModeFeatures.GRID);
        return features;
    }
    
    public void remove() {
        if (buildingMode)
            disableBuildingMode();
    }
    
    protected void setupPreviewRenderer(boolean lines) {
        if (lines) {
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth((float) LittleTiles.CONFIG.rendering.previewLineThickness);
            
            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.4F);
            RenderSystem.enableDepthTest();
            return;
        }
        if (LittleTiles.CONFIG.rendering.darkerPreviewBoxShading) {
            GL14.glBlendColor(0.25F, 0.25F, 0.25F, 0.25F);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.CONSTANT_COLOR, GlStateManager.DestFactor.ONE_MINUS_DST_COLOR, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        } else
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        
        double alpha = (float) (Math.sin(System.nanoTime() / 200000000D) * 0.2 + 0.5);
        RenderSystem.setShaderColor(1, 1, 1, (float) alpha);
        
        RenderSystem.setShaderTexture(0, PreviewRenderer.WHITE_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.enableCull();
    }
    
    protected ByteBufferBuilder createBuffer() {
        return new ByteBufferBuilder(86432);
    }
    
    protected BufferBuilder createBuilder(ByteBufferBuilder buffer, boolean lines) {
        if (lines)
            return new BufferBuilder(buffer, VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }
    
    protected BufferBuilder createTesselatorBuilder(boolean lines) {
        if (lines)
            return Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }
    
    protected void buildBox(PoseStack pose, RenderBox box, BufferBuilder builder, int colorAlpha, boolean lines) {
        if (lines)
            box.renderLines(pose, builder, colorAlpha, box.getCenter(), 0.002);
        else
            box.renderPreview(pose, builder, colorAlpha);
    }
    
    public boolean onRightClick(Level level, Player player, BlockHitResult result) {
        return true;
    }
    
    public boolean onLeftClick(Level level, Player player, BlockHitResult result) {
        return false;
    }
    
    public boolean onMouseWheelClickBlock(Level level, Player player, BlockHitResult result) {
        return false;
    }
    
}
