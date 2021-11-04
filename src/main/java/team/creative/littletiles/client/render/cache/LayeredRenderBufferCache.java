package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.BufferBuilderUtils;
import team.creative.littletiles.client.render.LittleRenderUtils;

public class LayeredRenderBufferCache {
    
    private IRenderDataCache[] queue = new IRenderDataCache[LittleRenderUtils.BLOCK_LAYERS.length];
    private BufferLink[] uploaded = new BufferLink[LittleRenderUtils.BLOCK_LAYERS.length];
    
    public LayeredRenderBufferCache() {}
    
    public IRenderDataCache get(RenderType layer) {
        return get(LittleRenderUtils.id(layer));
    }
    
    public IRenderDataCache get(int layer) {
        if (queue[layer] == null)
            return uploaded[layer];
        return queue[layer];
    }
    
    public synchronized void setEmptyIfEqual(BufferLink link, RenderType layer) {
        int id = LittleRenderUtils.id(layer);
        if (uploaded[id] == link)
            uploaded[id] = null;
    }
    
    public synchronized void setUploaded(BufferLink link, RenderType layer) {
        int id = LittleRenderUtils.id(layer);
        queue[id] = null;
        uploaded[id] = link;
    }
    
    public synchronized void set(RenderType layer, BufferBuilder buffer) {
        int id = LittleRenderUtils.id(layer);
        if (buffer == null)
            uploaded[id] = null;
        queue[id] = buffer != null ? new BufferBuilderWrapper(buffer) : null;
    }
    
    public synchronized void setEmpty() {
        for (int i = 0; i < queue.length; i++) {
            queue[i] = null;
            uploaded[i] = null;
        }
    }
    
    public synchronized void combine(LayeredRenderBufferCache cache) {
        for (int i = 0; i < queue.length; i++)
            if (i == LittleRenderUtils.TRANSLUCENT)
                queue[i] = combine(i, get(i), cache.get(i));
            else
                uploaded[i] = combine(i, get(i), cache.get(i));
    }
    
    private BufferLink combine(int layer, IRenderDataCache first, IRenderDataCache second) {
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
