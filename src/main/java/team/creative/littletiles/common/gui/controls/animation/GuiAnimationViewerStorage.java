package team.creative.littletiles.common.gui.controls.animation;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.mixin.client.render.LightTextureAccessor;

public interface GuiAnimationViewerStorage {
    
    public static final Camera FAKE_CAMERA = new Camera();
    
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
    
    public boolean isReady();
    
    public double longestSide();
    
    public AABB overall();
    
    public Vec3d center();
    
    public boolean highlightSelected();
    
    public void highlightSelected(boolean value);
    
    @OnlyIn(Dist.CLIENT)
    public Iterable<AnimationPreview> previewsToRender();
    
    @OnlyIn(Dist.CLIENT)
    public default void renderAll(PoseStack pose, Matrix4f projection, Minecraft mc) {
        int[][] pixels = makeLightBright();
        
        for (AnimationPreview preview : previewsToRender())
            renderPreview(pose, projection, preview, mc);
        
        resetLight(pixels);
    }
    
    @OnlyIn(Dist.CLIENT)
    public default void renderPreview(PoseStack pose, Matrix4f projection, AnimationPreview preview, Minecraft mc) {
        preview.animation.getRenderManager().setupRender(FAKE_CAMERA, null, false, false);
        preview.animation.getRenderManager().compileChunks(FAKE_CAMERA);
        
        renderChunkLayer(preview, RenderType.solid(), pose, projection);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0); // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
        renderChunkLayer(preview, RenderType.cutoutMipped(), pose, projection);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderChunkLayer(preview, RenderType.cutout(), pose, projection);
        
        renderChunkLayer(preview, RenderType.translucent(), pose, projection);
    }
    
    public default void renderChunkLayer(AnimationPreview preview, RenderType layer, PoseStack pose, Matrix4f matrix) {
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
        
        preview.animation.getRenderManager().renderChunkLayer(layer, pose, 0, 0, 0, matrix, shaderinstance.CHUNK_OFFSET);
        shaderinstance.clear();
        VertexBuffer.unbind();
        layer.clearRenderState();
    }
    
}