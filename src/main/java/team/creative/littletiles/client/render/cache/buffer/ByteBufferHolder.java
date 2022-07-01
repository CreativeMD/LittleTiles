package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

public class ByteBufferHolder implements BufferHolder {
    
    public ByteBuffer buffer;
    public int length;
    public int vertexCount;
    
    public ByteBufferHolder(ByteBuffer buffer, int length, int vertexCount) {
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