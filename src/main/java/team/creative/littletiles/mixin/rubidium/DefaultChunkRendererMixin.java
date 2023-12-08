package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import team.creative.littletiles.client.LittleTilesClient;

@Mixin(DefaultChunkRenderer.class)
public class DefaultChunkRendererMixin {
    
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer;end(Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V"))
    public void render(ChunkRenderMatrices matrices, CommandList commandList, ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera, CallbackInfo info) {
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.renderChunkLayer(((TerrainRenderPassAccessor) renderPass).getLayer(), matrices.modelView(), camera.x, camera.y, camera.z, matrices
                    .projection());
    }
    
}
