package team.creative.littletiles.client.render.cache.buffer;

import java.util.ArrayList;
import java.util.List;

public class BufferCollection {
    
    private List<BufferCache> buffers = new ArrayList<>();
    private int length = 0;
    
    public BufferCollection() {}
    
    public void queueForUpload(BufferCache cache) {
        buffers.add(cache);
        length += cache.lengthToUpload();
    }
    
    public int length() {
        return length;
    }
    
    public void discard() {
        for (BufferCache holder : buffers)
            holder.invalidate();
    }
    
    public void download(ChunkBufferDownloader downloader) {
        for (BufferCache holder : buffers)
            holder.download(downloader);
    }
    
    public void eraseBuffers() {
        for (BufferCache holder : buffers)
            holder.eraseBuffer();
    }
    
    public boolean isEmpty() {
        return buffers.isEmpty();
    }
    
    @Override
    public String toString() {
        return buffers.toString();
    }
    
    public Iterable<BufferCache> buffers() {
        return buffers;
    }
    
}
