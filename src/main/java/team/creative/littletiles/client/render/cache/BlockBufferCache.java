package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.RenderedBufferHolder;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;

public class BlockBufferCache {
    
    private HashMap<RenderType, BufferHolder> queue = new HashMap<>();
    private HashMap<RenderType, UploadableBufferHolder> uploaded = new HashMap<>();
    
    private HashMap<RenderType, BufferHolder> additional = null;
    
    public BlockBufferCache() {}
    
    private UploadableBufferHolder getUploaded(RenderType layer) {
        UploadableBufferHolder holder = uploaded.get(layer);
        if (holder != null && holder.isInvalid()) {
            uploaded.remove(layer);
            return null;
        }
        return holder;
    }
    
    public BufferHolder get(RenderType layer) {
        BufferHolder queued = queue.get(layer);
        if (queued == null)
            return getUploaded(layer);
        if (additional != null)
            return combine(queued, additional.get(layer));
        return queued;
        
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
        
        queue.remove(layer);
        uploaded.put(layer, cache.add(builder, holder));
    }
    
    public void set(RenderType layer, BufferBuilder.RenderedBuffer buffer) {
        synchronized (this) {
            if (buffer == null && additional == null)
                uploaded.remove(layer);
            
            if (buffer == null)
                queue.remove(layer);
            else
                queue.put(layer, new RenderedBufferHolder(buffer));
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
        additional = null;
    }
    
    public synchronized void additional(BlockBufferCache cache) {
        boolean already = additional != null;
        if (!already)
            additional = new HashMap<>();
        
        for (Entry<RenderType, BufferHolder> entry : additional.entrySet()) {
            BufferHolder aCache = cache.get(entry.getKey());
            entry.setValue(already ? combine(entry.getValue(), aCache) : aCache);
            
            if (entry.getKey() == RenderType.translucent())
                queue.put(entry.getKey(), combine(get(entry.getKey()), aCache));
            else
                uploaded.put(entry.getKey(), combine(get(entry.getKey()), aCache));
        }
    }
    
    private UploadableBufferHolder combine(BufferHolder first, BufferHolder second) {
        int vertexCount = 0;
        int length = 0;
        ByteBuffer firstBuffer = null;
        if (first != null) {
            firstBuffer = first.byteBuffer();
            if (firstBuffer != null) {
                vertexCount += first.vertexCount();
                length += first.length();
            }
        }
        
        ByteBuffer secondBuffer = null;
        if (second != null) {
            secondBuffer = second.byteBuffer();
            if (secondBuffer != null) {
                vertexCount += second.vertexCount();
                length += second.length();
            }
        }
        
        if (vertexCount == 0)
            return null;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        
        if (firstBuffer != null) {
            firstBuffer.position(0);
            firstBuffer.limit(first.length());
            byteBuffer.put(firstBuffer);
        }
        
        if (secondBuffer != null) {
            secondBuffer.position(0);
            secondBuffer.limit(second.length());
            byteBuffer.put(secondBuffer);
        }
        return new UploadableBufferHolder(byteBuffer, 0, length, vertexCount);
    }
    
    public static BufferBuilder createVertexBuffer(VertexFormat format, List<? extends RenderBox> cubes) {
        int size = 1;
        for (RenderBox cube : cubes)
            size += cube.countQuads();
        return new BufferBuilder(format.getVertexSize() * size / 6);
    }
    
    public boolean hasInvalidBuffers() {
        for (Entry<RenderType, UploadableBufferHolder> entry : uploaded.entrySet())
            if ((entry.getValue().isInvalid() || !entry.getValue().isAvailable()) && !queue.containsKey(entry.getKey()))
                return true;
        return false;
    }
}
