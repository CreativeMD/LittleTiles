package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;

import team.creative.creativecore.mixin.BufferBuilderAccessor;

public class BufferBuilderHolder implements BufferHolder {
    
    public final BufferBuilder builder;
    
    public BufferBuilderHolder(BufferBuilder builder) {
        this.builder = builder;
    }
    
    @Override
    public ByteBuffer byteBuffer() {
        return ((BufferBuilderAccessor) builder).getBuffer();
    }
    
    @Override
    public int length() {
        return ((BufferBuilderAccessor) builder).getNextElementByte();
    }
    
    @Override
    public int vertexCount() {
        return ((BufferBuilderAccessor) builder).getVertices();
    }
    
}