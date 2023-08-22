package team.creative.littletiles.mixin.client.render;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.BufferBuilder;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(targets = "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin implements RebuildTaskExtender {
    
    @Unique
    public Set<RenderType> renderTypes;
    
    @Unique
    public ChunkBufferBuilderPack pack;
    
    @Unique
    public ChunkLayerMap<BufferCollection> caches;
    
    @Shadow(aliases = { "this$0" })
    public RenderChunk this$1;
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;enableCaching()V"),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private void compileStart(float f1, float f2, float f3, ChunkBufferBuilderPack pack, CallbackInfoReturnable info) {
        this.pack = pack;
        LittleRenderPipelineType.startCompile((RenderChunkExtender) this$1, this);
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;clearCache()V"),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private void compile(CallbackInfoReturnable info) {
        LittleRenderPipelineType.endCompile((RenderChunkExtender) this$1, this);
        
        this.pack = null;
        this.renderTypes = null;
    }
    
    @Redirect(at = @At(value = "NEW", target = "(I)Lit/unimi/dsi/fastutil/objects/ReferenceArraySet;", remap = false),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private ReferenceArraySet afterSetCreated(int capacity) {
        renderTypes = new ReferenceArraySet(capacity);
        return (ReferenceArraySet) renderTypes;
    }
    
    @Inject(at = @At("HEAD"),
            method = "handleBlockEntity(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
            require = 1)
    private void handleBlockEntity(@Coerce Object object, BlockEntity block, CallbackInfo info) {
        if (block instanceof BETiles tiles)
            LittleRenderPipelineType.compile((RenderChunkExtender) this$1, tiles, this);
    }
    
    @Inject(method = "doTask(Lnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"), cancellable = true, require = 1)
    private void injected(CallbackInfoReturnable<CompletableFuture> cir) {
        cir.setReturnValue(cir.getReturnValue().whenComplete((result, exception) -> {
            if (((Enum) result).ordinal() == 0) { // Successful
                ((RenderChunkExtender) this$1).prepareUpload();
                for (Tuple<RenderType, BufferCollection> tuple : caches.tuples())
                    ((RenderChunkExtender) this$1).uploaded(tuple.key, tuple.value);
            }
            this.caches = null;
        }));
    }
    
    @Unique
    public BufferBuilder builder(RenderType layer) {
        BufferBuilder builder = pack.builder(layer);
        if (renderTypes.add(layer))
            ((RenderChunkExtender) this$1).begin(builder);
        return builder;
    }
    
    @Override
    public BufferCache upload(RenderType layer, BufferCache cache) {
        if (cache.upload((ChunkBufferUploader) builder(layer))) {
            getOrCreateBuffers(layer).queueForUpload(cache);
            return cache;
        }
        return null;
    }
    
    @Unique
    public BufferCollection getOrCreateBuffers(RenderType layer) {
        if (caches == null)
            caches = new ChunkLayerMap<>();
        BufferCollection cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new BufferCollection());
        return cache;
    }
    
}
