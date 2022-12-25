package team.creative.littletiles.common.gui.controls;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
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
import team.creative.littletiles.mixin.LightTextureAccessor;

public class GuiAnimationViewer extends GuiControl implements IAnimationControl {
    
    public AnimationPreview preview;
    
    public SmoothValue rotX = new SmoothValue(200);
    public SmoothValue rotY = new SmoothValue(200);
    public SmoothValue rotZ = new SmoothValue(200);
    public SmoothValue distance = new SmoothValue(200);
    
    public boolean grabbed = false;
    public double grabX;
    public double grabY;
    
    public GuiAnimationViewer(String name) {
        super(name);
    }
    
    @Override
    public ControlFormatting getControlFormatting() {
        return ControlFormatting.NESTED_NO_PADDING;
    }
    
    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (grabbed) {
            rotY.set(rotY.aimed() + x - grabX);
            rotX.set(rotX.aimed() + y - grabY);
            grabX = x;
            grabY = y;
        }
    }
    
    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == 0) {
            grabbed = true;
            grabX = x;
            grabY = y;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        if (button == 0)
            grabbed = false;
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
    
    protected void renderChunkLayer(RenderType layer, PoseStack pose, Matrix4f matrix) {
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
    
    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        if (preview == null)
            return;
        
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        int[][] pixels = makeLightBright();
        
        rotX.tick();
        rotY.tick();
        rotZ.tick();
        distance.tick();
        
        pose.pushPose();
        
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        RenderSystem.viewport((int) (rect.minX * window.getGuiScale()), (int) (rect.minY * window.getGuiScale()) + 1, (int) (rect.getWidth() * window.getGuiScale()), (int) (rect
                .getHeight() * window.getGuiScale()));
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        PoseStack projection = new PoseStack();
        projection.mulPoseMatrix(mc.gameRenderer.getProjectionMatrix(70));
        RenderSystem.setProjectionMatrix(projection.last().pose());
        
        pose.setIdentity();
        Matrix4f matrix = projection.last().pose();
        
        pose.translate(0, 0, -distance.current());
        RenderSystem.enableDepthTest();
        
        Vec3d rotationCenter = preview.animation.getCenter().rotationCenter;
        
        pose.translate(-preview.box.getXsize() * 0.5, -preview.box.getYsize() * 0.5, -preview.box.getZsize() * 0.5);
        
        pose.translate(rotationCenter.x, rotationCenter.y, rotationCenter.z);
        
        pose.mulPose(Axis.XP.rotationDegrees((float) rotX.current()));
        pose.mulPose(Axis.YP.rotationDegrees((float) rotY.current()));
        pose.mulPose(Axis.ZP.rotationDegrees((float) rotZ.current()));
        
        pose.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z);
        
        RenderSystem.setInverseViewRotationMatrix(new Matrix3f(pose.last().normal()).invert());
        preview.animation.getRenderManager().setupRender(preview.animation, new Vec3d(), null, false, false);
        LittleLevelEntityRenderer.INSTANCE.compileChunks(preview.animation);
        
        renderChunkLayer(RenderType.solid(), pose, matrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        renderChunkLayer(RenderType.cutoutMipped(), pose, matrix);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderChunkLayer(RenderType.cutout(), pose, matrix);
        
        renderChunkLayer(RenderType.translucent(), pose, matrix);
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
    public void onLoaded(AnimationPreview preview) {
        this.preview = preview;
        this.distance.setStart(preview.grid.toVanillaGrid(preview.entireBox.getLongestSide()) / 2D + 2);
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
    protected int preferredWidth() {
        return 10;
    }
    
    @Override
    protected int preferredHeight(int width) {
        return 10;
    }
}