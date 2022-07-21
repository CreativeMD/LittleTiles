package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;

public interface IRenderDataCache {
    
    public ByteBuffer byteBuffer();
    
    public int length();
    
    public int vertexCount();
    
}
