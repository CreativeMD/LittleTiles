package com.creativemd.littletiles.client.render.cache;

import java.nio.ByteBuffer;

public class BufferLink implements IRenderDataCache {
    
    public int index;
    public final int length;
    public final int vertexCount;
    private ByteBuffer byteBuffer;
    private boolean uploaded = false;
    
    public BufferLink(ByteBuffer buffer, int length, int count) {
        this.byteBuffer = buffer;
        this.length = length;
        this.vertexCount = count;
    }
    
    public void merged(int index) {
        this.index = index;
    }
    
    public void uploaded() {
        uploaded = true;
        byteBuffer = null;
    }
    
    public void downloaded(ByteBuffer buffer) {
        uploaded = false;
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
