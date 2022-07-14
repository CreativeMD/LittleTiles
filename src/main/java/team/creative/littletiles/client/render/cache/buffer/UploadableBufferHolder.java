package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

public class UploadableBufferHolder implements BufferHolder {
    
    public int index;
    public final int length;
    public final int vertexCount;
    private ByteBuffer byteBuffer;
    private boolean invalid;
    
    public UploadableBufferHolder(ByteBuffer buffer, int index, int length, int count) {
        this.byteBuffer = buffer;
        this.index = index;
        this.length = length;
        this.vertexCount = count;
    }
    
    public void uploaded(boolean doNotErase) {
        if (!doNotErase)
            byteBuffer = null;
    }
    
    public void downloaded(ByteBuffer buffer) {
        byteBuffer = buffer;
    }
    
    public boolean isAvailable() {
        return byteBuffer != null;
    }
    
    @Override
    public ByteBuffer byteBuffer() {
        return byteBuffer;
    }
    
    @Override
    public int length() {
        return length;
    }
    
    @Override
    public int vertexCount() {
        return vertexCount;
    }
    
    public boolean isInvalid() {
        return invalid;
    }
    
    public void invalidate() {
        invalid = true;
    }
    
}
