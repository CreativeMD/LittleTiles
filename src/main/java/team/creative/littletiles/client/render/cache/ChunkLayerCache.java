package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.platform.MemoryTracker;

import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;

public class ChunkLayerCache implements Iterable<UploadableBufferHolder> {
    
    private int totalSize;
    private List<UploadableBufferHolder> holders = new ArrayList<>();
    
    public ChunkLayerCache() {}
    
    public void add(int totalSize, UploadableBufferHolder buffer) {
        this.totalSize = totalSize;
        holders.add(buffer);
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
            if (buffer.capacity() >= holder.index + holder.length()) {
                ByteBuffer downloaded = MemoryTracker.create(holder.length());
                downloaded.put(0, buffer, holder.index, holder.length());
                downloaded.rewind();
                holder.downloaded(downloaded);
            } else
                holder.invalidate();
            
        buffer.rewind();
    }
    
    @Override
    public Iterator<UploadableBufferHolder> iterator() {
        return holders.iterator();
    }
    
    public void uploaded(boolean doNotErase) {
        for (UploadableBufferHolder holder : holders)
            holder.uploaded(doNotErase);
    }
    
    public boolean isEmpty() {
        return holders.isEmpty();
    }
    
}
