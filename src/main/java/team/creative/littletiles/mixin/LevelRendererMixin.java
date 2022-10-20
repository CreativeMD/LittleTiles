package team.creative.littletiles.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    
    @Inject(at = @At("HEAD"), method = "allChanged()V")
    public void allChanged(CallbackInfo info) {
        LittleChunkDispatcher.onReloadRenderers((LevelRenderer) (Object) this);
    }
    
    @Inject(at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=blockentities"),
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
    public void renderBlockEntities(PoseStack pose, float frameTime, long time, boolean outOfMemory, Camera cam, GameRenderer renderer, LightTexture lightTexture, Matrix4f matrix, CallbackInfo info) {
        Frustum frustum = ((LevelRendererAccessor) this).getCapturedFrustum() != null ? ((LevelRendererAccessor) this).getCapturedFrustum() : ((LevelRendererAccessor) this)
                .getCullingFrustum();
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.renderBlockEntities(pose, frustum, frameTime);
    }
    
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;zTransparentOld:D", opcode = Opcodes.PUTFIELD),
            method = "renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V")
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.resortTransparency(layer, x, y, z);
    }
    
}
