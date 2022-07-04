package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

public class RenderedBufferHolder implements BufferHolder {
    
    public final BufferBuilder.RenderedBuffer buffer;
    
    public RenderedBufferHolder(BufferBuilder.RenderedBuffer buffer) {
        this.buffer = buffer;
    }
    
    @Override
    public ByteBuffer byteBuffer() {
        return buffer.vertexBuffer();
    }
    
    @Override
    public int length() {
        return buffer.drawState().vertexBufferSize();
    }
    
    @Override
    public int vertexCount() {
        return buffer.drawState().vertexCount();
    }
    
}