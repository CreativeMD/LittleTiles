package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;

public class ByteBufferHolder implements BufferHolder {
    
    public final ByteBuffer buffer;
    public final int length;
    public final int vertexCount;
    
    public ByteBufferHolder(ByteBuffer buffer, int length, int vertexCount) {
        this.buffer = buffer;
        this.length = length;
        this.vertexCount = vertexCount;
    }
    
    public ByteBufferHolder(RenderedBuffer buffer) {
        this.length = buffer.drawState().vertexBufferSize();
        this.buffer = MemoryTracker.create(length);
        this.buffer.put(buffer.vertexBuffer());
        this.buffer.rewind();
        this.vertexCount = buffer.drawState().vertexCount();
        buffer.release();
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