package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;

public class BufferLink implements IRenderDataCache {
    
    public int index;
    public final int length;
    public final int vertexCount;
    private ByteBuffer byteBuffer;
    
    public BufferLink(ByteBuffer buffer, int length, int count) {
        this.byteBuffer = buffer;
        this.length = length;
        this.vertexCount = count;
    }
    
    public void merged(int index) {
        this.index = index;
    }
    
    public void uploaded() {
        byteBuffer = null;
    }
    
    public void downloaded(ByteBuffer buffer) {
        byteBuffer = buffer;
    }
    
    @Override
    public ByteBuffer byteBuffer() {
        return byteBuffer;
    }
    
    @Override
    public int length() {
        return length;
    }
    
    @Override
    public int vertexCount() {
        return vertexCount;
    }
    
}
