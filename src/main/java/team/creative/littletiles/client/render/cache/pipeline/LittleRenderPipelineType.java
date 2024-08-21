package team.creative.littletiles.client.render.cache.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.IBlockBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

public abstract class LittleRenderPipelineType<T extends LittleRenderPipeline> {
    
    private static final List<LittleRenderPipelineType> TYPES = new ArrayList<>();
    public static final LittleRenderPipelineTypeForge FORGE = new LittleRenderPipelineTypeForge();
    
    public static int typeCount() {
        return TYPES.size();
    }
    
    public static LittleRenderPipelineType get(int id) {
        return TYPES.get(id);
    }
    
    public static void startCompile(RenderChunkExtender chunk) {
        chunk.startBuilding();
    }
    
    public static BufferCache upload(ChunkBufferUploader uploader, BufferCollection buffers, BufferCache cache) {
        if (cache.upload(uploader)) {
            buffers.queueForUpload(cache);
            return cache;
        }
        return null;
    }
    
    public static void compile(long pos, BETiles be, Function<RenderType, ChunkBufferUploader> builderSupplier, Function<RenderType, BufferCollection> bufferSupplier) {
        be.updateQuadCache(pos);
        
        IBlockBufferCache cache = be.render.buffers();
        synchronized (be.render) {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                if (!cache.has(layer))
                    continue;
                
                cache.setUploaded(layer, upload(builderSupplier.apply(layer), bufferSupplier.apply(layer), cache.get(layer)));
            }
        }
    }
    
    public static BufferCache markUploaded(BufferCollection buffers, BufferCache cache) {
        buffers.queueForUpload(cache);
        return cache;
    }
    
    public static void compileUploaded(long pos, BETiles be, Function<RenderType, BufferCollection> bufferSupplier) {
        be.updateQuadCache(pos);
        
        IBlockBufferCache cache = be.render.buffers();
        synchronized (be.render) {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                if (!cache.has(layer))
                    continue;
                
                cache.setUploaded(layer, markUploaded(bufferSupplier.apply(layer), cache.get(layer)));
            }
        }
    }
    
    public static void endCompile(RenderChunkExtender chunk) {
        chunk.endBuilding();
    }
    
    public final Supplier<T> factory;
    public final int id;
    
    protected LittleRenderPipelineType(Supplier<T> factory) {
        this.factory = factory;
        id = TYPES.size();
        TYPES.add(this);
    }
    
    public static class LittleRenderPipelineTypeForge extends LittleRenderPipelineType<LittleRenderPipelineForge> {
        
        private LittleRenderPipelineTypeForge() {
            super(LittleRenderPipelineForge::new);
        }
        
    }
    
}
