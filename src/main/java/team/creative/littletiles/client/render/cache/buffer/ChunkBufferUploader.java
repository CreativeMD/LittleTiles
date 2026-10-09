package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ChunkBufferUploader {
    
    public int uploadIndex();
    
    public void upload(ByteBuffer buffer);

    default boolean tryUpload(ByteBuffer buffer, int vertexCount) {
        upload(buffer);
        return true;
    }
    
    public boolean hasFacingSupport();
    
    public int uploadIndex(int facing);
    
    public void upload(int facing, ByteBuffer buffer);
    
    public void addTexture(TextureAtlasSprite texture);
    
}
