package team.creative.littletiles.client.render.cache;

import java.util.Map.Entry;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;

public class BlockBufferCache {
    
    public static BufferCache combine(BufferCache first, BufferCache second) {
        if (first == null && second == null)
            return null;
        if (first == null)
            return second;
        if (second == null)
            return first;
        
        return first.combine(second);
        
    }
    
    private ChunkLayerMap<BufferCache> queue = new ChunkLayerMap<>();
    private ChunkLayerMap<BufferCache> uploaded = new ChunkLayerMap<>();
    
    private transient ChunkLayerMap<BufferCache> additional = null;
    
    public BlockBufferCache() {}
    
    private BufferCache getUploaded(RenderType layer) {
        BufferCache holder = uploaded.get(layer);
        if (holder != null && holder.isInvalid()) {
            uploaded.remove(layer);
            return null;
        }
        return holder;
    }
    
    private BufferCache getOriginal(RenderType layer) {
        BufferCache queued = queue.get(layer);
        if (queued == null)
            return getUploaded(layer);
        return queued;
    }
    
    public BufferCache get(RenderType layer) {
        BufferCache original = getOriginal(layer);
        if (additional != null)
            return combine(original, additional.get(layer));
        return original;
        
    }
    
    public BufferCache extract(RenderType layer, int index) {
        BufferCache holder = getOriginal(layer);
        if (holder == null)
            return null;
        boolean holderUploaded = uploaded.get(layer) != null;
        BufferCache extracted = holder.extract(index);
        if (holder.groupCount() == 0)
            if (holderUploaded)
                uploaded.remove(layer);
            else
                queue.remove(layer);
        return extracted;
    }
    
    public boolean has(RenderType layer) {
        return queue.containsKey(layer) || getUploaded(layer) != null || (additional != null && additional.containsKey(layer));
    }
    
    public void setUploaded(RenderType layer, BufferCache uploadable) {
        queue.remove(layer);
        if (uploadable == null)
            uploaded.remove(layer);
        else
            uploaded.put(layer, uploadable);
    }
    
    public synchronized void setEmpty() {
        queue.clear();
        uploaded.clear();
        additional = null;
    }
    
    public boolean hasAdditional() {
        return additional != null;
    }
    
    public synchronized void setBuffers(ChunkLayerMap<BufferCache> buffers) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            BufferCache buffer = buffers.get(layer);
            if (buffer == null && additional == null)
                uploaded.remove(layer);
            
            if (buffer == null)
                queue.remove(layer);
            else
                queue.put(layer, buffer);
        }
        
        if (additional != null)
            additional = null;
    }
    
    public synchronized void additional(RenderType layer, BufferCache holder) {
        boolean already = additional != null;
        if (!already)
            additional = new ChunkLayerMap<>();
        additional.put(layer, already ? combine(additional.get(layer), holder) : holder);
    }
    
    public synchronized void additional(LayeredBufferCache cache) {
        boolean already = additional != null;
        if (!already)
            additional = new ChunkLayerMap<>();
        
        for (RenderType layer : RenderType.chunkBufferLayers())
            additional.put(layer, already ? combine(additional.get(layer), cache.get(layer)) : cache.get(layer));
    }
    
    public boolean hasInvalidBuffers() {
        for (Entry<RenderType, BufferCache> entry : uploaded.tuples())
            if ((entry.getValue() != null && (entry.getValue().isInvalid() || !entry.getValue().isAvailable())) && !queue.containsKey(entry.getKey()))
                return true;
        return false;
    }
}
