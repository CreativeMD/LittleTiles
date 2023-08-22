package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.gl.util.VertexRange;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataUnsafe;
import me.jellysquid.mods.sodium.client.util.NativeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public class RubidiumChunkBufferUploader implements ChunkBufferUploader {
    
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    private NativeBuffer buffer;
    private VertexRange[] ranges = new VertexRange[ModelQuadFacing.COUNT];
    private List<TextureAtlasSprite> sprites;
    
    public RubidiumChunkBufferUploader() {}
    
    public void set(long data, GlVertexFormat format, ByteBuffer exisitingData, int extraLength, int[] extraLengthFacing, TextureAtlasSprite[] existing) {
        buffer = new NativeBuffer((exisitingData != null ? exisitingData.limit() : 0) + extraLength);
        ByteBuffer buffer = this.buffer.getDirectBuffer();
        
        int currentOffset = 0;
        for (int i = 0; i < buffers.length; i++) {
            int start = currentOffset + SectionRenderDataUnsafe.getVertexOffset(data, i) * format.getStride();
            int length = SectionRenderDataUnsafe.getElementCount(data, i) / 6 * 4 * format.getStride() + extraLengthFacing[i];
            buffers[i] = buffer.slice(start, length);
            currentOffset += extraLengthFacing[i];
            ranges[i] = new VertexRange(start / format.getStride(), length / format.getStride());
        }
        
        if (existing != null) {
            sprites = new ArrayList<>();
            for (int i = 0; i < existing.length; i++)
                sprites.add(existing[i]);
        }
    }
    
    public void clear() {
        Arrays.fill(buffers, null);
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
    
}
