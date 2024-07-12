package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataUnsafe;

import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;

public class RubidiumChunkBufferDownloader implements ChunkBufferDownloader {
    
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    
    public RubidiumChunkBufferDownloader() {}
    
    public void set(long data, GlVertexFormat format, int offset, ByteBuffer buffer) {
        for (int i = 0; i < buffers.length; i++) {
            int count = SectionRenderDataUnsafe.getElementCount(data, i);
            buffers[i] = buffer.slice((SectionRenderDataUnsafe.getVertexOffset(data, i) - offset) * format.getStride(), count / 6 * 4 * format.getStride());
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
