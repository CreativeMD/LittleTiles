package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

import team.creative.creativecore.client.render.model.BufferBuilderUtils;

public class BufferBuilderHolder implements BufferHolder {
    
    public final BufferBuilder builder;
    
    public BufferBuilderHolder(BufferBuilder builder) {
        this.builder = builder;
    }
    
    @Override
    public ByteBuffer byteBuffer() {
        return BufferBuilderUtils.getBuffer(builder);
    }
    
    @Override
    public int length() {
        return BufferBuilderUtils.getBufferSizeByte(builder);
    }
    
    @Override
    public int vertexCount() {
        return BufferBuilderUtils.getVertexCount(builder);
    }
    
}