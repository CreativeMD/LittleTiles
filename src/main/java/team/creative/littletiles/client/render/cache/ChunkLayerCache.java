package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;

import team.creative.creativecore.mixin.BufferBuilderAccessor;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;

public class ChunkLayerCache {
    
    private int totalSize;
    private List<UploadableBufferHolder> holders = new ArrayList<>();
    
    public ChunkLayerCache() {}
    
    public UploadableBufferHolder add(BufferBuilder builder, BufferHolder data) {
        int index = ((BufferBuilderAccessor) builder).getNextElementByte();
        ByteBuffer buffer = data.byteBuffer();
        if (buffer == null)
            return null;
        builder.putBulkData(buffer);
        UploadableBufferHolder holder = new UploadableBufferHolder(buffer, index, data.length(), data.vertexCount());
        holders.add(holder);
        totalSize = ((BufferBuilderAccessor) builder).getNextElementByte();
        return holder;
    }
    
    public void discard() {
        for (UploadableBufferHolder holder : holders)
            holder.invalidate();
    }
    
    public int totalSize() {
        return totalSize;
    }
    
    public void download(ByteBuffer buffer) {
        for (UploadableBufferHolder holder : holders)
            if (buffer.capacity() >= holder.index + holder.length)
                holder.downloaded(buffer.slice(holder.index, holder.length));
            else
                holder.invalidate();
    }
    
    public void uploaded() {
        for (UploadableBufferHolder holder : holders)
            holder.uploaded();
    }
    
    public boolean isEmpty() {
        return holders.isEmpty();
    }
    
}
