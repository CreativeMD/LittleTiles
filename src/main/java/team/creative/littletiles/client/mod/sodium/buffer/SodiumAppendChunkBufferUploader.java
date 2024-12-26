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
import team.creative.littletiles.client.mod.sodium.data.LittleQuadView;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineSodium;

public class SodiumAppendChunkBufferUploader implements SodiumBufferUploader {
    
    private ByteBuffer[] buffers = new ByteBuffer[ModelQuadFacing.COUNT];
    private NativeBuffer buffer;
    private int[] ranges = new int[ModelQuadFacing.COUNT];
    private List<TextureAtlasSprite> sprites;
    private TranslucentGeometryCollector collector;
    
    public SodiumAppendChunkBufferUploader() {}
    
    public void set(long data, GlVertexFormat format, long offset, ByteBuffer exisitingData, int extraLength, int[] extraLengthFacing, TextureAtlasSprite[] existing) {
        buffer = new NativeBuffer((exisitingData != null ? exisitingData.limit() : 0) + extraLength);
        ByteBuffer buffer = this.buffer.getDirectBuffer();
        
        int currentOffset = 0;
        for (int i = 0; i < buffers.length; i++) {
            int originalStart = (int) ((SectionRenderDataUnsafe.getVertexOffset(data, i) - offset) * format.getStride());
            int originalLength = (int) (SectionRenderDataUnsafe.getElementCount(data, i) / 6 * 4 * format.getStride());
            
            int newStart = originalStart + currentOffset;
            int newLength = originalLength + extraLengthFacing[i];
            
            buffers[i] = buffer.slice(newStart, newLength);
            
            buffers[i].put(0, exisitingData, originalStart, originalLength);
            buffers[i].position(originalLength);
            
            currentOffset += extraLengthFacing[i];
            ranges[i] = newLength / format.getStride();
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
        ChunkVertexType type = LittleRenderPipelineSodium.getType();
        int stride = type.getVertexFormat().getStride();
        ModelQuadFacing facing = ModelQuadFacing.UNASSIGNED;
        
        while (ptr < end) {
            quad.readVertices(ptr, 0, stride, facing);
            collector.appendQuad(quad.getPackedNormal(), quad.getVertices(), facing);
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
