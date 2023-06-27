package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

import team.creative.creativecore.mixin.BufferBuilderAccessor;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;

public class UploadableBufferHolder implements BufferHolder {
    
    public static UploadableBufferHolder addToBuild(BufferBuilder builder, BufferHolder data, ChunkLayerCache cache) {
        int index = ((BufferBuilderAccessor) builder).getNextElementByte();
        ByteBuffer buffer = data.byteBuffer();
        if (buffer == null)
            return null;
        builder.putBulkData(buffer);
        buffer.rewind();
        UploadableBufferHolder holder = new UploadableBufferHolder(buffer, index, data.length(), data.vertexCount(), data.indexes());
        cache.add(((BufferBuilderAccessor) builder).getNextElementByte(), holder);
        return holder;
    }
    
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
            erase();
    }
    
    protected void erase() {
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
        erase();
    }
    
}
