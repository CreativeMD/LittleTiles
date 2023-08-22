package team.creative.littletiles.client.render.cache.buffer;

import net.minecraft.world.phys.Vec3;

public interface BufferCache {
    
    public BufferCache extract(int index);
    
    public BufferCache combine(BufferCache holder);
    
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
