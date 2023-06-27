package team.creative.littletiles.mixin.rubidium;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.BufferBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.gl.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.rubidium.buffer.RubidiumBufferHolder;
import team.creative.littletiles.client.rubidium.buffer.RubidiumUploadableBufferHolder;
import team.creative.littletiles.client.rubidium.data.ChunkRenderDataExtender;
import team.creative.littletiles.common.block.entity.BETiles;

@Mixin(ChunkRenderRebuildTask.class)
public class ChunkRenderRebuildTaskMixin implements RebuildTaskExtender {
    
    @Shadow(remap = false)
    @Final
    private RenderSection render;
    
    @Unique
    public ChunkLayerMap<ChunkLayerCache> caches;
    
    @Unique
    public ChunkBuildContext buildContext;
    
    @Inject(at = @At("HEAD"), remap = false, require = 1,
            method = "performBuild(Lme/jellysquid/mods/sodium/client/gl/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationSource;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildResult;")
    public void performBuildStart(ChunkBuildContext buildContext, CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> info) {
        LittleRenderPipelineType.startCompile((RenderChunkExtender) render, this);
        this.buildContext = buildContext;
    }
    
    @Redirect(
            method = "performBuild(Lme/jellysquid/mods/sodium/client/gl/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationSource;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildResult;",
            remap = false, require = 1, at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;"))
    public BlockEntityRenderer<BlockEntity> getRendererRedirect(BlockEntityRenderDispatcher dispatcher, BlockEntity entity) {
        if (entity instanceof BETiles be)
            LittleRenderPipelineType.compile((RenderChunkExtender) render, be, this);
        return dispatcher.getRenderer(entity);
    }
    
    @Inject(at = @At("TAIL"), remap = false, require = 1,
            method = "performBuild(Lme/jellysquid/mods/sodium/client/gl/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationSource;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildResult;")
    public void performBuildEnd(ChunkBuildContext buildContext, CancellationSource cancellationSource, CallbackInfoReturnable<ChunkBuildResult> info) {
        LittleRenderPipelineType.endCompile((RenderChunkExtender) render, this);
    }
    
    @Redirect(
            method = "performBuild(Lme/jellysquid/mods/sodium/client/gl/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationSource;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildResult;",
            remap = false, at = @At(value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;build()Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData;"))
    public ChunkRenderData chunkDataBuilder(ChunkRenderData.Builder builder) {
        ChunkRenderData data = builder.build();
        ((ChunkRenderDataExtender) data).setCaches(caches);
        return data;
    }
    
    @Override
    public BufferBuilder builder(RenderType layer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ChunkLayerMap<ChunkLayerCache> getLayeredCache() {
        return caches;
    }
    
    @Override
    public ChunkLayerCache getOrCreate(RenderType layer) {
        if (caches == null)
            caches = new ChunkLayerMap<>();
        ChunkLayerCache cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new ChunkLayerCache());
        return cache;
    }
    
    @Override
    public void clear() {
        caches = null;
    }
    
    @Override
    public UploadableBufferHolder upload(RenderType layer, BlockBufferCache cache) {
        BufferHolder temp = cache.get(layer);
        if (!(temp instanceof RubidiumBufferHolder) || (temp instanceof RubidiumUploadableBufferHolder t && (!t.isAvailable() || t.isInvalid())))
            return null;
        RubidiumBufferHolder buffer = (RubidiumBufferHolder) temp;
        ChunkModelBuilder builder = buildContext.buffers.get(layer);
        
        ChunkVertexBufferBuilderAccessor vertex = (ChunkVertexBufferBuilderAccessor) builder.getVertexBuffer();
        
        // Add to vertex buffer
        int vertexStart = vertex.getCount();
        int vertexCount = buffer.vertexCount();
        if (vertexStart + vertexCount >= vertex.getCapacity())
            vertex.callGrow(vertex.getStride() * vertexCount);
        ByteBuffer data = vertex.getBuffer();
        int index = vertex.getCount() * vertex.getStride();
        data.position(index);
        data.put(buffer.byteBuffer());
        buffer.byteBuffer().rewind();
        data.rewind();
        vertex.setCount(vertex.getCount() + vertexCount);
        
        // Add to index buffers
        int[] facingIndex = new int[ModelQuadFacing.COUNT];
        for (int i = 0; i < ModelQuadFacing.COUNT; i++) {
            ModelQuadFacing facing = ModelQuadFacing.VALUES[i];
            IntArrayList indexData = buffer.facingIndexList(facing);
            if (indexData == null || indexData.isEmpty()) {
                facingIndex[i] = -1;
                continue;
            }
            
            IntArrayList chunkIndexData = ((IndexBufferBuilderAccessor) builder.getIndexBuffer(facing)).getIndices();
            facingIndex[i] = chunkIndexData.size();
            for (int j = 0; j < indexData.size(); j++)
                chunkIndexData.add(indexData.getInt(j) + vertexStart);
        }
        
        // Add textures
        for (TextureAtlasSprite texture : buffer.getUsedTextures())
            builder.addSprite(texture);
        
        RubidiumUploadableBufferHolder holder = new RubidiumUploadableBufferHolder(buffer.byteBuffer(), index, facingIndex, buffer.length(), buffer.vertexCount(), buffer
                .indexes(), buffer.facingIndexLists(), buffer.getUsedTextures());
        getOrCreate(layer).add(vertex.getCount() * vertex.getStride(), holder);
        return holder;
    }
    
}
