package team.creative.littletiles.common.gui.controls;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.entity.LittleLevelEntityRenderer;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.mixin.LightTextureAccessor;

public class GuiAnimationViewer extends GuiControl {
    
    public SmoothValue offX = new SmoothValue(200);
    public SmoothValue offY = new SmoothValue(200);
    public SmoothValue offZ = new SmoothValue(200);
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue distance = new SmoothValue(200);
    
    public ViewerDragMode grabMode = ViewerDragMode.NONE;
    public double grabX;
    public double grabY;
    public Supplier<LinkedHashMap<GuiTreeItemStructure, AnimationPreview>> supplier;
    public Supplier<GuiTreeItemStructure> selected;
    private boolean initialized = false;
    
    public GuiAnimationViewer(String name, Supplier<LinkedHashMap<GuiTreeItemStructure, AnimationPreview>> supplier, Supplier<GuiTreeItemStructure> selected) {
        super(name);
        this.supplier = supplier;
        this.selected = selected;
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (grabMode == ViewerDragMode.NONE)
            return;
        
        double grabOffset = 0.01;
        
        switch (grabMode) {
            case LEFT -> {
                rotY.set(rotY.aimed() + x - grabX);
                rotX.set(rotX.aimed() + y - grabY);
            }
            case RIGHT -> {
                Vector3f offset = new Vector3f((float) ((x - grabX) * grabOffset), 0, (float) ((y - grabY) * grabOffset));
                offset.rotate(Axis.XP.rotationDegrees((float) rotX.current()));
                offset.rotate(Axis.YP.rotationDegrees((float) rotY.current()));
                offset.rotate(Axis.ZP.rotationDegrees((float) rotZ.current()));
                offX.set(offX.aimed() + offset.x);
                offY.set(offY.aimed() + offset.y);
                offZ.set(offZ.aimed() + offset.z);
            }
            case MIDDLE -> {
                Vector3f offset = new Vector3f((float) ((x - grabX) * grabOffset), (float) ((y - grabY) * -grabOffset), 0);
                offset.rotate(Axis.XP.rotationDegrees((float) rotX.current()));
                offset.rotate(Axis.YP.rotationDegrees((float) rotY.current()));
                offset.rotate(Axis.ZP.rotationDegrees((float) rotZ.current()));
                offX.set(offX.aimed() + offset.x);
                offY.set(offY.aimed() + offset.y);
                offZ.set(offZ.aimed() + offset.z);
            }
        }
        grabX = x;
        grabY = y;
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (grabMode == ViewerDragMode.NONE) {
            grabMode = ViewerDragMode.of(button);
            grabX = x;
            grabY = y;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        if (button == grabMode.ordinal() - 1)
            grabMode = ViewerDragMode.NONE;
    }
    
    @Override
    public boolean mouseScrolled(Rect rect, double x, double y, double delta) {
        distance.set(Math.max(distance.aimed() + delta * -(Screen.hasControlDown() ? 5 : 1), 0));
        return true;
    }
    
    public static int[][] makeLightBright() {
        int[][] pixels = new int[16][16];
        LightTextureAccessor texture = (LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++) {
                pixels[x][y] = texture.getLightPixels().getPixelRGBA(x, y);
                texture.getLightPixels().setPixelRGBA(x, y, ColorUtils.WHITE);
            }
        
        texture.getLightTexture().upload();
        return pixels;
    }
    
    public static void resetLight(int[][] pixels) {
        LightTextureAccessor texture = (LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                texture.getLightPixels().setPixelRGBA(x, y, pixels[x][y]);
        texture.getLightTexture().upload();
    }
    
    protected void renderChunkLayer(AnimationPreview preview, RenderType layer, PoseStack pose, Matrix4f matrix) {
        layer.setupRenderState();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        
        for (int i = 0; i < 12; ++i)
            shaderinstance.setSampler("Sampler" + i, RenderSystem.getShaderTexture(i));
        
        if (shaderinstance.MODEL_VIEW_MATRIX != null)
            shaderinstance.MODEL_VIEW_MATRIX.set(pose.last().pose());
        
        if (shaderinstance.PROJECTION_MATRIX != null)
            shaderinstance.PROJECTION_MATRIX.set(matrix);
        
        if (shaderinstance.COLOR_MODULATOR != null)
            shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        
        if (shaderinstance.FOG_START != null)
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        
        if (shaderinstance.FOG_END != null)
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        
        if (shaderinstance.FOG_COLOR != null)
            shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        
        if (shaderinstance.FOG_SHAPE != null)
            shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        
        if (shaderinstance.TEXTURE_MATRIX != null)
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        
        if (shaderinstance.GAME_TIME != null)
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        
        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        
        LittleLevelEntityRenderer.INSTANCE.renderChunkLayer(preview.animation, layer, pose, 0, 0, 0, matrix);
        shaderinstance.clear();
        VertexBuffer.unbind();
        layer.clearRenderState();
    }
    
    public PoseStack getProjectionMatrix(Minecraft mc, double fov, float width, float height) {
        PoseStack posestack = new PoseStack();
        posestack.setIdentity();
        posestack.mulPoseMatrix(new Matrix4f().setPerspective((float) (fov * Math.PI / 180F), width / height, 0.05F, mc.gameRenderer.getDepthFar()));
        return posestack;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        LinkedHashMap<GuiTreeItemStructure, AnimationPreview> previews = supplier.get();
        GuiTreeItemStructure selected = this.selected.get();
        if (selected == null)
            return;
        AnimationPreview main = previews.get(selected);
        if (main == null)
            return;
        
        if (!initialized) {
            this.distance.setStart(main.grid.toVanillaGrid(main.entireBox.getLongestSide()) / 2D + 2);
            initialized = true;
        }
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        int[][] pixels = makeLightBright();
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        distance.tick();
        offX.tick();
        offY.tick();
        offZ.tick();
        
        pose.pushPose();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        double scale = window.getGuiScale();
        int height = (int) (rect.getHeight() * scale);
        RenderSystem.viewport((int) (rect.minX * scale), (int) (window.getHeight() - rect.minY * scale - height), (int) (rect.getWidth() * scale), height);
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        PoseStack projection = getProjectionMatrix(mc, 70, (float) rect.getWidth(), (float) rect.getHeight());
        RenderSystem.setProjectionMatrix(projection.last().pose());
        
        pose.setIdentity();
        Matrix4f matrix = projection.last().pose();
        
        pose.translate(0, 0, -distance.current());
        RenderSystem.enableDepthTest();
        
        Vec3d rotationCenter = main.animation.getCenter().rotationCenter;
        
        projection.translate(offX.current(), offY.current(), offZ.current());
        
        projection.translate(-main.box.getXsize() * 0.5, -main.box.getYsize() * 0.5, -main.box.getZsize() * 0.5);
        
        projection.translate(rotationCenter.x, rotationCenter.y, rotationCenter.z - distance.current());
        
        projection.mulPose(Axis.XP.rotationDegrees((float) rotX.current()));
        projection.mulPose(Axis.YP.rotationDegrees((float) rotY.current()));
        projection.mulPose(Axis.ZP.rotationDegrees((float) rotZ.current()));
        
        projection.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z + distance.current());
        
        RenderSystem.setInverseViewRotationMatrix(new Matrix3f(pose.last().normal()).invert());
        
        for (AnimationPreview preview : previews.values()) {
            preview.animation.getRenderManager().setupRender(preview.animation, new Vec3d(), null, false, false);
            LittleLevelEntityRenderer.INSTANCE.compileChunks(preview.animation);
            
            renderChunkLayer(preview, RenderType.solid(), pose, matrix);
            mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
            renderChunkLayer(preview, RenderType.cutoutMipped(), pose, matrix);
            mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
            renderChunkLayer(preview, RenderType.cutout(), pose, matrix);
            
            renderChunkLayer(preview, RenderType.translucent(), pose, matrix);
        }
        
        pose.popPose();
        
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        
        RenderSystem.setProjectionMatrix(new Matrix4f()
                .setOrtho(0.0F, (float) (window.getWidth() / window.getGuiScale()), (float) (window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, ForgeHooksClient
                        .getGuiFarPlane()));
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        resetLight(pixels);
    }
    
    @Override
    public void closed() {}
    
    @Override
    public void init() {}
    
    @Override
    public void tick() {}
    
    @Override
    public void flowX(int width, int preferred) {}
    
    @Override
    public void flowY(int width, int height, int preferred) {}
    
    @Override
    protected int preferredWidth(int availableWidth) {
        return 10;
    }
    
    @Override
    protected int preferredHeight(int width, int availableHeight) {
        return 10;
    }
    
    protected static enum ViewerDragMode {
        
        NONE,
        LEFT,
        RIGHT,
        MIDDLE;
        
        public static ViewerDragMode of(int button) {
            return switch (button) {
                case 0 -> LEFT;
                case 1 -> RIGHT;
                case 2 -> MIDDLE;
                default -> NONE;
            };
        }
        
    }
}