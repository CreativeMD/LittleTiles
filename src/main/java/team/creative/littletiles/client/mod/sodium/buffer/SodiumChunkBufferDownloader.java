package team.creative.littletiles.client.mod.sodium.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.data.SectionRenderDataUnsafe;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;

public class SodiumChunkBufferDownloader implements ChunkBufferDownloader {
    
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    
    public SodiumChunkBufferDownloader() {}
    
    public void set(long data, GlVertexFormat format, ByteBuffer buffer) {
        
        long facingList = SectionRenderDataUnsafe.getFacingList(data);
        long currentOffset = 0;
        for (int i = 0; i < buffers.length; i++) {
            long count = SectionRenderDataUnsafe.getVertexCount(data, i);
            
            int facing = (int) ((facingList >>> (i * 8)) & 0xFF);
            int length = (int) (count * format.getStride());
            
            if (count > 0)
                buffers[facing] = buffer.slice((int) currentOffset, length);
            else
                buffers[facing] = null;
            currentOffset += length;
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
