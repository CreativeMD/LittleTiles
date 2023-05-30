package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

public class UploadableBufferHolder implements BufferHolder {
    
    public int index;
    private int length;
    private int vertexCount;
    private ByteBuffer byteBuffer;
    private boolean invalid;
    private final int[] indexes;
    private int indexCount;
    
    public UploadableBufferHolder(ByteBuffer buffer, int index, int length, int count, int[] indexes) {
        this.byteBuffer = buffer;
        this.index = index;
        this.length = length;
        this.vertexCount = count;
        this.indexes = indexes;
        this.indexCount = indexes != null ? indexes.length / 2 : 0;
    }
    
    public void uploaded(boolean doNotErase) {
        if (!doNotErase)
            byteBuffer = null;
    }
    
    public void downloaded(ByteBuffer buffer) {
        byteBuffer = buffer;
        index = -1;
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
    
    public boolean isInvalid() {
        return invalid;
    }
    
    public void invalidate() {
        invalid = true;
    }
    
}
