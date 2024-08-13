package team.creative.littletiles.common.gui.controls.animation;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
        preview.setupRendering(pose);
        
        preview.animation.getRenderManager().setupRender(FAKE_CAMERA, null, false, false);
        preview.animation.getRenderManager().compileSections(FAKE_CAMERA);
        
        renderChunkLayer(preview, RenderType.solid(), pose, projection);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, mc.options.mipmapLevels().get() > 0); // Neo: fix flickering leaves when mods mess up the blurMipmap settings
        renderChunkLayer(preview, RenderType.cutoutMipped(), pose, projection);
        mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        renderChunkLayer(preview, RenderType.cutout(), pose, projection);
        
        renderChunkLayer(preview, RenderType.translucent(), pose, projection);
    }
    
    public default void renderChunkLayer(AnimationPreview preview, RenderType layer, PoseStack pose, Matrix4f matrix) {
        layer.setupRenderState();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.setDefaultUniforms(Mode.QUADS, pose.last().pose(), matrix, Minecraft.getInstance().getWindow());
        shaderinstance.apply();
        
        preview.animation.getRenderManager().renderChunkLayer(layer, pose, 0, 0, 0, matrix, shaderinstance.CHUNK_OFFSET);
        shaderinstance.clear();
        VertexBuffer.unbind();
        layer.clearRenderState();
    }
    
}