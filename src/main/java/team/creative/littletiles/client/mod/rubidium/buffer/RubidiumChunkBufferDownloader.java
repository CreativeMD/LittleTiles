package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataUnsafe;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;

public class RubidiumChunkBufferDownloader implements ChunkBufferDownloader {
    
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    
    public RubidiumChunkBufferDownloader() {}
    
    public void set(long data, GlVertexFormat format, ByteBuffer buffer) {
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = buffer.slice(SectionRenderDataUnsafe.getVertexOffset(data, i) * format.getStride(), SectionRenderDataUnsafe.getElementCount(data, i) / 6 * 4 * format
                    .getStride());
        }
    }
    
    public void clear() {
        Arrays.fill(buffers, null);
    }
    
    @Override
    public ByteBuffer downloaded() {
        return downloaded(ModelQuadFacing.UNASSIGNED.ordinal());
    }
    
    @Override
    public boolean hasFacingSupport() {
        return true;
    }
    
    @Override
    public ByteBuffer downloaded(int facing) {
        return buffers[facing];
    }
    
}
