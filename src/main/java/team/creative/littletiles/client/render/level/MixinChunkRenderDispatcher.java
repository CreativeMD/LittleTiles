package team.creative.littletiles.client.render.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.phys.Vec3;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask")
public abstract class MixinChunkRenderDispatcher {
    
    @Inject(at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"),
            method = "doTask(Lnet/minecraft/client/renderer/ChunkBufferBuilderPack;)", locals = LocalCapture.CAPTURE_FAILHARD)
    private void rebuild(ChunkBufferBuilderPack pack, CallbackInfo info, Vec3 vec, float x, float y, float z, CompiledChunk compiled) {
        LittleChunkDispatcher.beforeUploadRebuild(pack, compiled, (RenderChunk) (Object) this);
    }
    
}