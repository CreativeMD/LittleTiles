package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;

import team.creative.creativecore.client.render.model.BufferBuilderUtils;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;

public class ChunkLayerCache {
    
    private int totalSize;
    private List<UploadableBufferHolder> holders = new ArrayList<>();
    
    public ChunkLayerCache() {}
    
    public UploadableBufferHolder add(BufferBuilder builder, BufferHolder data) {
        int index = BufferBuilderUtils.getBufferSizeByte(builder);
        BufferBuilderUtils.addBuffer(builder, data.byteBuffer(), data.length(), data.vertexCount());
        UploadableBufferHolder holder = new UploadableBufferHolder(data.byteBuffer(), index, data.vertexCount());
        holders.add(holder);
        totalSize = BufferBuilderUtils.getBufferSizeByte(builder);
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
            if (buffer.capacity() >= holder.index + holder.length) {
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(holder.length);
                buffer.position(holder.index);
                int end = holder.index + holder.length;
                while (buffer.position() < end)
                    newBuffer.put(buffer.get());
                holder.downloaded(newBuffer);
            } else
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
