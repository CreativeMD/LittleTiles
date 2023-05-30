package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;

public class ByteBufferHolder implements BufferHolder {
    
    private final ByteBuffer buffer;
    private int length;
    private int vertexCount;
    private final int[] indexes;
    private int indexCount;
    
    public ByteBufferHolder(ByteBuffer buffer, int length, int vertexCount, int[] indexes) {
        this.buffer = buffer;
        this.length = length;
        this.vertexCount = vertexCount;
        this.indexes = indexes;
        this.indexCount = indexes != null ? indexes.length / 2 : 0;
    }
    
    public ByteBufferHolder(RenderedBuffer buffer, int[] indexes) {
        this.length = buffer.drawState().vertexBufferSize();
        this.buffer = MemoryTracker.create(length);
        this.buffer.put(buffer.vertexBuffer());
        this.buffer.rewind();
        this.vertexCount = buffer.drawState().vertexCount();
        buffer.release();
        this.indexes = indexes;
        this.indexCount = indexes != null ? indexes.length / 2 : 0;
    }
    
    @Override
    public int[] indexes() {
        return indexes;
    }
    
    @Override
    public int indexCount() {
        return indexCount;
    }
    
    @Override
    public void removeEntry(int length, int vertexCount) {
        this.length -= length;
        this.vertexCount -= vertexCount;
        this.indexCount--;
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