package team.creative.littletiles.mixin;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

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
import com.mojang.blaze3d.vertex.VertexBuffer;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(targets = "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask")
public abstract class RebuildTaskMixin implements RebuildTaskExtender {
    
    @Unique
    public Set<RenderType> renderTypes;
    
    @Unique
    public ChunkBufferBuilderPack pack;
    
    @Unique
    public HashMap<RenderType, ChunkLayerCache> caches;
    
    @Shadow
    public RenderChunk this$1;
    
    @Inject(at = @At("HEAD"),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private void compileStart(float f1, float f2, float f3, ChunkBufferBuilderPack pack, CallbackInfoReturnable info) {
        this.pack = pack;
        LittleChunkDispatcher.startCompile(this$1);
    }
    
    @Inject(at = @At("TAIL"),
            method = "compile(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;",
            require = 1)
    private void compile(CallbackInfoReturnable info) {
        ((RenderChunkExtender) this$1).dynamicLightUpdate(false);
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = this$1.getBuffer(layer);
            ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
            synchronized (manager) {
                manager.queued--;
            }
        }
        
        if (caches != null)
            for (Entry<RenderType, ChunkLayerCache> entry : caches.entrySet()) {
                VertexBuffer vertexBuffer = this$1.getBuffer(entry.getKey());
                ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
                manager.set(entry.getValue());
            }
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
            LittleChunkDispatcher.add(this$1, tiles, this);
    }
    
    @Override
    public BufferBuilder builder(RenderType layer) {
        BufferBuilder builder = pack.builder(layer);
        if (renderTypes.add(layer))
            ((RenderChunkExtender) this$1).invokeBeginLayer(builder);
        return builder;
    }
    
    @Override
    public ChunkLayerCache getOrCreate(RenderType layer) {
        if (caches == null)
            caches = new HashMap<>();
        ChunkLayerCache cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new ChunkLayerCache());
        return cache;
    }
    
}
