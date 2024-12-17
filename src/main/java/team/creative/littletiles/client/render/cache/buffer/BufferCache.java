package team.creative.littletiles.client.render.cache.buffer;

import java.util.Iterator;

import net.minecraft.world.phys.Vec3;

public interface BufferCache {
    
    public static BufferCache combine(BufferCache first, BufferCache second) {
        if (first == null && second == null)
            return null;
        if (first == null)
            return second;
        if (second == null)
            return first;
        
        return first.combine(second);
    }
    
    public static BufferCache combineOrCopy(BufferCache first, Iterable<BufferCache> caches) {
        var itr = caches.iterator();
        
        if (first == null && itr.hasNext())
            return null;
        if (!itr.hasNext())
            return first.copy();
        if (first == null)
            first = itr.next();
        if (!itr.hasNext())
            return first.copy();
        
        return first.combine(itr);
    }
    
    public BufferCache extract(int index);
    
    public BufferCache copy();
    
    public BufferCache combine(BufferCache holder);
    
    public BufferCache combine(Iterator<BufferCache> holder);
    
    public void applyOffset(Vec3 vec);
    
    public boolean isInvalid();
    
    public boolean isAvailable();
    
    public int groupCount();
    
    public boolean upload(ChunkBufferUploader uploader);
    
    public boolean download(ChunkBufferDownloader downloader);
    
    public void eraseBuffer();
    
    public void invalidate();
    
    public int lengthToUpload();
    
    public int lengthToUpload(int facing);
    
}
