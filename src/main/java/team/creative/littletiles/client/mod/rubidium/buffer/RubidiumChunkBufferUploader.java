package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.gl.util.VertexRange;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataUnsafe;
import org.embeddedt.embeddium.impl.util.NativeBuffer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public class RubidiumChunkBufferUploader implements ChunkBufferUploader {
    
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    private NativeBuffer buffer;
    private VertexRange[] ranges = new VertexRange[ModelQuadFacing.COUNT];
    private List<TextureAtlasSprite> sprites;
    
    public RubidiumChunkBufferUploader() {}
    
    public void set(long data, GlVertexFormat format, int offset, ByteBuffer exisitingData, int extraLength, int[] extraLengthFacing, TextureAtlasSprite[] existing) {
        buffer = new NativeBuffer((exisitingData != null ? exisitingData.limit() : 0) + extraLength);
        ByteBuffer buffer = this.buffer.getDirectBuffer();
        
        int currentOffset = 0;
        for (int i = 0; i < buffers.length; i++) {
            int originalStart = (SectionRenderDataUnsafe.getVertexOffset(data, i) - offset) * format.getStride();
            int originalLength = SectionRenderDataUnsafe.getElementCount(data, i) / 6 * 4 * format.getStride();
            
            int newStart = originalStart + currentOffset;
            int newLength = originalLength + extraLengthFacing[i];
            
            buffers[i] = buffer.slice(newStart, newLength);
            
            buffers[i].put(0, exisitingData, originalStart, originalLength);
            buffers[i].position(originalLength);
            
            currentOffset += extraLengthFacing[i];
            ranges[i] = new VertexRange(newStart / format.getStride(), newLength / format.getStride());
        }
        
        if (existing != null) {
            sprites = new ArrayList<>();
            for (int i = 0; i < existing.length; i++)
                sprites.add(existing[i]);
        }
    }
    
    public void clear() {
        Arrays.fill(buffers, null);
        buffer.free();
        buffer = null;
    }
    
    @Override
    public void addSprite(TextureAtlasSprite texture) {
        if (sprites == null)
            sprites = new ArrayList<>();
        if (!sprites.contains(texture))
            sprites.add(texture);
    }
    
    @Override
    public int uploadIndex() {
        return buffers[ModelQuadFacing.UNASSIGNED.ordinal()].position();
    }
    
    @Override
    public void upload(ByteBuffer buffer) {
        buffers[ModelQuadFacing.UNASSIGNED.ordinal()].put(buffer);
        buffer.rewind();
    }
    
    @Override
    public int uploadIndex(int facing) {
        return buffers[facing].position();
    }
    
    @Override
    public void upload(int facing, ByteBuffer buffer) {
        buffers[facing].put(buffer);
        buffer.rewind();
    }
    
    @Override
    public boolean hasFacingSupport() {
        return true;
    }
    
    public TextureAtlasSprite[] sprites() {
        if (sprites == null)
            return null;
        return sprites.toArray(new TextureAtlasSprite[sprites.size()]);
    }
    
    public VertexRange[] ranges() {
        return ranges;
    }
    
    public NativeBuffer buffer() {
        return buffer;
    }
    
    @Override
    public boolean isSorted() {
        return false;
    }
    
}
