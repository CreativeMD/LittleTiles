package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.BufferBuilderUtils;

public class LayeredRenderBufferCache {
    
    private HashMap<RenderType, IRenderDataCache> queue = new HashMap<>();
    private HashMap<RenderType, BufferLink> uploaded = new HashMap<>();
    
    private boolean beforeUpdate = false;
    private HashMap<RenderType, IRenderDataCache> additional = null;
    
    public LayeredRenderBufferCache() {}
    
    public IRenderDataCache get(RenderType layer) {
        IRenderDataCache queued = queue.get(layer);
        if (queued == null)
            return uploaded.get(layer);
        if (additional != null && layer != RenderType.translucent())
            return combine(queued, additional.get(layer));
        return queued;
        
    }
    
    public synchronized void setEmptyIfEqual(BufferLink link, RenderType layer) {
        if (uploaded.get(layer) == link)
            uploaded.remove(layer);
    }
    
    public synchronized void setUploaded(BufferLink link, RenderType layer) {
        queue.remove(layer);
        queue.put(layer, link);
    }
    
    public synchronized void set(RenderType layer, BufferBuilder buffer) {
        if (buffer == null && additional == null)
            uploaded.remove(layer);
        queue.put(layer, buffer != null ? new BufferBuilderWrapper(buffer) : null);
        
        if (layer == RenderType.translucent() && !beforeUpdate && additional != null) {
            IRenderDataCache data = additional.get(layer);
            if (data != null)
                queue.put(layer, combine(queue.get(layer), data));
        }
    }
    
    public synchronized void setEmpty() {
        queue.clear();
        uploaded.clear();
    }
    
    public boolean hasAdditional() {
        return additional != null;
    }
    
    public void beforeUpdate() {
        beforeUpdate = true;
    }
    
    public void afterUpdate() {
        beforeUpdate = false;
        additional = null;
    }
    
    public synchronized void additional(LayeredRenderBufferCache cache) {
        boolean already = additional != null;
        if (!already)
            additional = new HashMap<>();
        
        for (Entry<RenderType, IRenderDataCache> entry : additional.entrySet()) {
            IRenderDataCache aCache = cache.get(entry.getKey());
            entry.setValue(already ? combine(entry.getValue(), aCache) : aCache);
            
            if (entry.getKey() == RenderType.translucent())
                queue.put(entry.getKey(), combine(get(entry.getKey()), aCache));
            else
                uploaded.put(entry.getKey(), combine(get(entry.getKey()), aCache));
        }
    }
    
    private BufferLink combine(IRenderDataCache first, IRenderDataCache second) {
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
        return new BufferLink(byteBuffer, length, vertexCount);
    }
    
    public static BufferBuilder createVertexBuffer(VertexFormat format, List<? extends RenderBox> cubes) {
        int size = 1;
        for (RenderBox cube : cubes)
            size += cube.countQuads();
        return new BufferBuilder(format.getVertexSize() * size);
    }
    
    public static class ByteBufferWrapper implements IRenderDataCache {
        
        public ByteBuffer buffer;
        public int length;
        public int vertexCount;
        
        public ByteBufferWrapper(ByteBuffer buffer, int length, int vertexCount) {
            this.buffer = buffer;
        }
        
        @Override
        public ByteBuffer byteBuffer() {
            return buffer;
        }
        
        @Override
        public int length() {
            return length;
        }
        
        @Override
        public int vertexCount() {
            return vertexCount;
        }
        
    }
    
    public static class BufferBuilderWrapper implements IRenderDataCache {
        
        public final BufferBuilder builder;
        
        public BufferBuilderWrapper(BufferBuilder builder) {
            this.builder = builder;
        }
        
        @Override
        public ByteBuffer byteBuffer() {
            return BufferBuilderUtils.getBuffer(builder);
        }
        
        @Override
        public int length() {
            return BufferBuilderUtils.getBufferSizeByte(builder);
        }
        
        @Override
        public int vertexCount() {
            return BufferBuilderUtils.getVertexCount(builder);
        }
        
    }
}
