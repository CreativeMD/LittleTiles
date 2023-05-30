package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.ByteBufferHolder;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;

public class BlockBufferCache {
    
    public static BufferHolder combine(BufferHolder first, BufferHolder second) {
        if (first == null && second == null)
            return null;
        if (first == null)
            return second;
        if (second == null)
            return first;
        
        int vertexCount = 0;
        int length = 0;
        ByteBuffer firstBuffer = first.byteBuffer();
        if (firstBuffer != null) {
            vertexCount += first.vertexCount();
            length += first.length();
        }
        
        ByteBuffer secondBuffer = second.byteBuffer();
        if (secondBuffer != null) {
            vertexCount += second.vertexCount();
            length += second.length();
        }
        
        if (vertexCount == 0)
            return null;
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        
        if (firstBuffer != null) {
            firstBuffer.position(0);
            firstBuffer.limit(first.length());
            byteBuffer.put(firstBuffer);
            firstBuffer.rewind();
        }
        
        if (secondBuffer != null) {
            secondBuffer.position(0);
            secondBuffer.limit(second.length());
            byteBuffer.put(secondBuffer);
            secondBuffer.rewind();
        }
        byteBuffer.rewind();
        return new ByteBufferHolder(byteBuffer, length, vertexCount, null);
    }
    
    private ChunkLayerMap<BufferHolder> queue = new ChunkLayerMap<>();
    private ChunkLayerMap<UploadableBufferHolder> uploaded = new ChunkLayerMap<>();
    
    private transient ChunkLayerMap<BufferHolder> additional = null;
    
    public BlockBufferCache() {}
    
    private UploadableBufferHolder getUploaded(RenderType layer) {
        UploadableBufferHolder holder = uploaded.get(layer);
        if (holder != null && holder.isInvalid()) {
            uploaded.remove(layer);
            return null;
        }
        return holder;
    }
    
    private BufferHolder getOriginal(RenderType layer) {
        BufferHolder queued = queue.get(layer);
        if (queued == null)
            return getUploaded(layer);
        return queued;
    }
    
    public BufferHolder get(RenderType layer) {
        BufferHolder original = getOriginal(layer);
        if (additional != null)
            return combine(original, additional.get(layer));
        return original;
        
    }
    
    public BufferHolder extract(RenderType layer, int index) {
        BufferHolder holder = getOriginal(layer);
        if (holder == null)
            return null;
        boolean holderUploaded = uploaded.get(layer) != null;
        BufferHolder extracted = holder.extract(index);
        if (holder.indexCount() == 0)
            if (holderUploaded)
                uploaded.remove(layer);
            else
                queue.remove(layer);
        return extracted;
    }
    
    public boolean has(RenderType layer) {
        return queue.containsKey(layer) || getUploaded(layer) != null || (additional != null && additional.containsKey(layer));
    }
    
    public int size(RenderType layer) {
        BufferHolder queued = queue.get(layer);
        if (queued == null) {
            queued = getUploaded(layer);
            if (queued != null)
                return queued.length();
        }
        
        int size = queued.length();
        if (additional != null) {
            queued = additional.get(layer);
            if (queued != null)
                size += queued.length();
        }
        return size;
    }
    
    public void add(RenderType layer, BufferBuilder builder, ChunkLayerCache cache) {
        BufferHolder holder = get(layer);
        if (holder == null)
            return;
        
        queue.remove(layer);
        uploaded.put(layer, cache.add(builder, holder));
    }
    
    public void set(RenderType layer, int[] indexes, BufferBuilder.RenderedBuffer buffer) {
        synchronized (this) {
            if (buffer == null && additional == null)
                uploaded.remove(layer);
            
            if (buffer == null)
                queue.remove(layer);
            else
                queue.put(layer, new ByteBufferHolder(buffer, indexes));
        }
    }
    
    public synchronized void setEmpty() {
        queue.clear();
        uploaded.clear();
    }
    
    public boolean hasAdditional() {
        return additional != null;
    }
    
    public void afterRendered() {
        if (additional != null)
            additional = null;
    }
    
    public synchronized void additional(RenderType layer, BufferHolder holder) {
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
        for (Entry<RenderType, UploadableBufferHolder> entry : uploaded.tuples())
            if ((entry.getValue() != null && (entry.getValue().isInvalid() || !entry.getValue().isAvailable())) && !queue.containsKey(entry.getKey()))
                return true;
        return false;
    }
}
