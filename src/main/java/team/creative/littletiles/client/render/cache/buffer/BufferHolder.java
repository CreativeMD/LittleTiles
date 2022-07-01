package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

public interface BufferHolder {
    
    public ByteBuffer byteBuffer();
    
    public int length();
    
    public int vertexCount();
    
}
