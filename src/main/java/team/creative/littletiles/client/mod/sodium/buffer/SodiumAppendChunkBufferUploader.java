package team.creative.littletiles.client.mod.sodium.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.data.SectionRenderDataUnsafe;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.util.NativeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.mod.sodium.SodiumInteractor;
import team.creative.littletiles.client.mod.sodium.data.LittleQuadView;

public class SodiumAppendChunkBufferUploader implements SodiumBufferUploader {
    
    public static final long ALL_FACINGS = 1694364648734976L;
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    private NativeBuffer buffer;
    private int[] ranges = new int[ModelQuadFacing.COUNT << 1];
    private List<TextureAtlasSprite> sprites;
    private TranslucentGeometryCollector collector;
    
    public SodiumAppendChunkBufferUploader() {}
    
    public void set(long data, GlVertexFormat format, ByteBuffer exisitingData, long[] existingCount, int extraLength, int[] extraLengthFacing, TextureAtlasSprite[] existing) {
        
        buffer = new NativeBuffer((exisitingData != null ? exisitingData.limit() : 0) + extraLength);
        ByteBuffer buffer = this.buffer.getDirectBuffer();
        
        long facingList = exisitingData == null ? ALL_FACINGS : SectionRenderDataUnsafe.getFacingList(data);
        
        int stride = format.getStride();
        int currentOffset = 0;
        long pointerOffset = 0;
        for (int i = 0; i < buffers.length; i++) {
            long dataCount = existingCount[i];
            int facing = exisitingData == null ? i : (int) ((facingList >>> (i * 8)) & 0xFF);
            
            int originalStart = (int) pointerOffset;
            int originalLength = (int) (dataCount * stride);
            
            int newStart = originalStart + currentOffset;
            int newLength = originalLength + extraLengthFacing[facing];
            
            buffers[facing] = buffer.slice(newStart, newLength);
            
            if (exisitingData != null)
                buffers[facing].put(0, exisitingData, originalStart, originalLength);
            buffers[facing].position(originalLength);
            
            currentOffset += extraLengthFacing[facing];
            ranges[facing << 1] = newLength / stride;
            ranges[(facing << 1) + 1] = facing;
            
            pointerOffset += originalLength;
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
    public void addTexture(TextureAtlasSprite texture) {
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
    
    public int[] ranges() {
        return ranges;
    }
    
    public NativeBuffer buffer() {
        return buffer;
    }
    
    public void appendVanillaTranslucentData(ByteBuffer buffer) {
        // Add translucent data by going through the buffers, collecting the quads and adding to the translucent collector
        long ptr = MemoryUtil.memAddress(buffer);
        long end = ptr + buffer.remaining();
        LittleQuadView quad = new LittleQuadView();
        ChunkVertexType type = SodiumInteractor.getVertexType();
        int stride = type.getVertexFormat().getStride();
        ModelQuadFacing facing = ModelQuadFacing.UNASSIGNED;
        
        while (ptr < end) {
            quad.readVertices(ptr, 0, stride, facing);
            collector.appendQuad(quad.getVertices(), quad.quadFacing(), quad.getPackedNormal());
            ptr += stride * 4;
        }
    }
    
    @Override
    public boolean isSorted() {
        return collector != null;
    }
    
    @Override
    public TranslucentGeometryCollector getTranslucentCollector() {
        return collector;
    }
    
    @Override
    public void setTranslucentCollector(TranslucentGeometryCollector collector) {
        this.collector = collector;
    }
    
}
