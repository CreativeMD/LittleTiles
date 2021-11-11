package com.creativemd.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayeredRenderBufferCache {
    
    private IRenderDataCache[] queue = new IRenderDataCache[BlockRenderLayer.values().length];
    private BufferLink[] uploaded = new BufferLink[BlockRenderLayer.values().length];
    
    private boolean beforeUpdate = false;
    private IRenderDataCache[] additional = null;
    
    public LayeredRenderBufferCache() {}
    
    public IRenderDataCache get(int layer) {
        if (queue[layer] == null)
            return uploaded[layer];
        if (additional != null && layer != BlockRenderLayer.TRANSLUCENT.ordinal())
            return combine(layer, queue[layer], additional[layer]);
        return queue[layer];
    }
    
    public synchronized void setEmptyIfEqual(BufferLink link, int layer) {
        if (uploaded[layer] == link)
            uploaded[layer] = null;
    }
    
    public synchronized void setUploaded(BufferLink link, int layer) {
        queue[layer] = null;
        uploaded[layer] = link;
    }
    
    public synchronized void set(int layer, BufferBuilder buffer) {
        if (buffer == null && additional == null)
            uploaded[layer] = null;
        queue[layer] = buffer != null ? new BufferBuilderWrapper(buffer) : null;
        if (layer == BlockRenderLayer.TRANSLUCENT.ordinal() && !beforeUpdate && additional != null && additional[layer] != null)
            queue[layer] = combine(layer, queue[layer], additional[layer]);
    }
    
    public synchronized void setEmpty() {
        for (int i = 0; i < queue.length; i++) {
            queue[i] = null;
            uploaded[i] = null;
        }
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
            additional = new IRenderDataCache[queue.length];
        
        for (int i = 0; i < additional.length; i++) {
            IRenderDataCache aCache = cache.get(i);
            additional[i] = already ? combine(i, additional[i], aCache) : aCache;
            
            if (i == BlockRenderLayer.TRANSLUCENT.ordinal())
                queue[i] = combine(i, get(i), aCache);
            else
                uploaded[i] = combine(i, get(i), aCache);
        }
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
        return new BufferBuilder(format.getNextOffset() * size);
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
            return builder.getByteBuffer();
        }
        
        @Override
        public int length() {
            return BufferBuilderUtils.getBufferSizeByte(builder);
        }
        
        @Override
        public int vertexCount() {
            return builder.getVertexCount();
        }
        
    }
}
