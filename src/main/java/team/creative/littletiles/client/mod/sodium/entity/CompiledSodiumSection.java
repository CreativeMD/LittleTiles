package team.creative.littletiles.client.mod.sodium.entity;

import java.util.Set;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.sodium.buffer.RenderedBufferSodium;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class CompiledSodiumSection {
    
    public final LittleSodiumSection section;
    public ChunkLayerMap<RenderedBufferSodium> buffers = new ChunkLayerMap<>();
    public ChunkLayerMap<BufferCollection> caches = new ChunkLayerMap<>();
    
    public CompiledSodiumSection(LittleSodiumSection section) {
        this.section = section;
        LittleRenderPipelineType.startCompile(section);
    }
    
    public BufferCollection getBuffers(RenderType layer) {
        if (caches == null)
            return null;
        return caches.get(layer);
    }
    
    public BufferCollection getOrCreateBuffers(RenderType layer) {
        if (caches == null)
            caches = new ChunkLayerMap<>();
        BufferCollection cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new BufferCollection());
        return cache;
    }
    
    public boolean finish() {
        for (Tuple<RenderType, BufferCollection> layer : caches.tuples())
            buffers.put(layer.key, new RenderedBufferSodium(layer.value));
        
        LittleRenderPipelineType.endCompile(section);
        
        return !buffers.isEmpty();
    }
    
    public void upload(Set<RenderType> hasBlocks) {
        section.upload(this, hasBlocks);
    }
    
}
