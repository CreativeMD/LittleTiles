package team.creative.littletiles.mixin.client.render;

import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import team.creative.creativecore.client.render.VertexFormatUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.mod.sodium.SodiumManager;
import team.creative.littletiles.client.render.block.LittleBlockClientRegistry;
import team.creative.littletiles.client.render.cache.build.RenderingThread;

@Mixin(value = LevelRenderer.class, priority = 1500)
public class LevelRendererMixin {
    
    @Shadow
    private ClientLevel level;
    
    @Inject(at = @At("TAIL"), method = "allChanged()V")
    public void allChanged(CallbackInfo info) {
        if (Minecraft.getInstance().levelRenderer != (LevelRenderer) (Object) this && level != null)
            return;
        
        synchronized (RenderingThread.class) {
            // Stop blocks from rendering
            RenderingThread.reload();
            
            // Update cached values first
            VertexFormatUtils.update();
            SodiumManager.reload();
            
            // Count up index
            RenderingThread.CURRENT_RENDERING_INDEX++;
            
            // Notify thread and blocks about change
            if (LittleTilesClient.ANIMATION_HANDLER != null)
                LittleTilesClient.ANIMATION_HANDLER.allChanged();
            LittleBlockClientRegistry.clearCache();
            LittleTilesClient.ITEM_RENDER_CACHE.clearCache();
            
            RenderingThread.initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
        }
    }
    
    @Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=blockentities"),
            method = "renderLevel")
    public void renderBlockEntities(DeltaTracker timer, boolean p_109603_, Camera cam, GameRenderer renderer, LightTexture lightTexture, Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix, CallbackInfo info) {
        Frustum frustum = ((LevelRendererAccessor) this).getCapturedFrustum() != null ? ((LevelRendererAccessor) this).getCapturedFrustum() : ((LevelRendererAccessor) this)
                .getCullingFrustum();
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.renderBlockEntitiesAndDestruction(new PoseStack(), frustum, timer.getGameTimeDeltaPartialTick(false));
    }
    
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;zTransparentOld:D", opcode = Opcodes.PUTFIELD), method = "renderChunkLayer")
    public void resortTransparency(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.resortTransparency(layer, x, y, z);
    }
    
    @Inject(at = @At("HEAD"), method = "needsUpdate()V")
    public void needsUpdate(CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.needsUpdate();
    }
    
    @Inject(at = @At("TAIL"), method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V")
    public void setupRender(Camera camera, Frustum frustum, boolean capturedFrustum, boolean spectator, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.setupRender(camera, frustum, capturedFrustum, spectator);
    }
    
    @Inject(at = @At("TAIL"), method = "compileSections(Lnet/minecraft/client/Camera;)V", require = 1)
    public void compileSections(Camera camera, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.compileSections(camera);
    }
    
}
