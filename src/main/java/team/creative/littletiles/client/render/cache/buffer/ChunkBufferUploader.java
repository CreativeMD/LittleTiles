package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ChunkBufferUploader {
    
    public int uploadIndex();
    
    public void upload(ByteBuffer buffer);
    
    public boolean hasFacingSupport();
    
    public int uploadIndex(int facing);
    
    public void upload(int facing, ByteBuffer buffer);
    
    public void addSprite(TextureAtlasSprite texture);
    
    public boolean isSorted();
    
}
