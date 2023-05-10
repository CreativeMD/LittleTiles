package team.creative.littletiles.mixin.client.render;

import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.level.LittleClientEventHandler;

@Mixin(value = LevelRenderer.class, priority = 1500)
public class LevelRendererMixin {
    
    @Inject(at = @At("HEAD"), method = "allChanged()V")
    public void allChanged(CallbackInfo info) {
        LittleChunkDispatcher.onReloadRenderers((LevelRenderer) (Object) this);
    }
    
    @Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=blockentities"),
            method = "renderLevel")
    public void renderBlockEntities(PoseStack pose, float frameTime, long time, boolean outOfMemory, Camera cam, GameRenderer renderer, LightTexture lightTexture, Matrix4f matrix, CallbackInfo info) {
        Frustum frustum = ((LevelRendererAccessor) this).getCapturedFrustum() != null ? ((LevelRendererAccessor) this).getCapturedFrustum() : ((LevelRendererAccessor) this)
                .getCullingFrustum();
        if (LittleTilesClient.ANIMATION_HANDLER != null) {
            LittleTilesClient.ANIMATION_HANDLER.renderBlockEntitiesAndDestruction(pose, frustum, frameTime);
            
        }
    }
    
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;zTransparentOld:D", opcode = Opcodes.PUTFIELD), method = "renderChunkLayer")
    public void resortTransparency(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, CallbackInfo info) {
        LittleClientEventHandler.transparencySortingIndex++;
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.resortTransparency(layer, x, y, z);
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;clear()V"), method = "renderChunkLayer")
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.renderChunkLayer(layer, pose, x, y, z, projectionMatrix);
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
    
    @Inject(at = @At("TAIL"), method = "compileChunks(Lnet/minecraft/client/Camera;)V")
    public void compileChunks(Camera camera, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.compileChunks(camera);
    }
    
}
