package team.creative.littletiles.client.mod.sodium.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.itr.FunctionNonNullIterator;
import team.creative.littletiles.client.mod.sodium.SodiumInteractor;
import team.creative.littletiles.client.mod.sodium.data.LittleQuadView;
import team.creative.littletiles.client.mod.sodium.pipeline.LittleRenderPipelineSodium;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public class SodiumBufferCache implements BufferCache {
    
    private final BufferHolder[] buffers;
    private List<TextureAtlasSprite> textures;
    private boolean invalid;
    
    public SodiumBufferCache(BufferHolder[] buffers, List<TextureAtlasSprite> textures) {
        this.buffers = buffers;
        this.textures = textures;
    }
    
    public BufferHolder buffer(ModelQuadFacing facing) {
        return buffers[facing.ordinal()];
    }
    
    public List<TextureAtlasSprite> getUsedTextures() {
        return textures;
    }
    
    @Override
    public BufferCache extract(int index) {
        BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
        boolean found = false;
        for (int i = 0; i < buffers.length; i++)
            if (this.buffers[i] != null) {
                buffers[i] = this.buffers[i].extract(index);
                if (this.buffers[i].isEmpty())
                    this.buffers[i] = null;
                if (buffers[i] != null)
                    found = true;
            }
        if (found)
            return new SodiumBufferCache(buffers, textures);
        return null;
    }
    
    @Override
    public BufferCache extract(int[] toExtract) {
        BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
        boolean found = false;
        for (int i = 0; i < buffers.length; i++)
            if (this.buffers[i] != null) {
                buffers[i] = this.buffers[i].extract(toExtract);
                if (this.buffers[i].isEmpty())
                    this.buffers[i] = null;
                if (buffers[i] != null)
                    found = true;
            }
        if (found)
            return new SodiumBufferCache(buffers, textures);
        return null;
    }
    
    @Override
    public BufferCache copy() {
        BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
        for (int i = 0; i < buffers.length; i++)
            if (this.buffers[i] != null)
                buffers[i] = this.buffers[i].copy();
        return new SodiumBufferCache(buffers, new ArrayList<>(textures));
    }
    
    @Override
    public BufferCache combine(Iterator<BufferCache> itr) {
        List<SodiumBufferCache> list = new ArrayList<>();
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        
        while (itr.hasNext()) {
            SodiumBufferCache b = (SodiumBufferCache) itr.next();
            for (TextureAtlasSprite texture : b.getUsedTextures())
                if (!sprites.contains(texture))
                    sprites.add(texture);
            list.add(b);
        }
        
        BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
        for (int i = 0; i < buffers.length; i++) {
            final int index = i;
            buffers[i] = (BufferHolder) BufferCache.combineOrCopy(this.buffers[i], new FunctionNonNullIterator<BufferCache>(list, x -> x.buffers[index]));
        }
        return new SodiumBufferCache(buffers, sprites);
    }
    
    private void applySodiumOffset(ByteBuffer buffer, Vec3 vec, int sectionIndex) {
        if (buffer == null)
            return;
        
        long ptr = MemoryUtil.memAddress(buffer);
        int i = 0;
        while (i < buffer.limit()) {
            int hi = MemoryUtil.memGetInt(ptr + 0);
            int lo = MemoryUtil.memGetInt(ptr + 4);
            float x = SodiumInteractor.unpackPositionX(hi, lo) + (float) vec.x;
            float y = SodiumInteractor.unpackPositionY(hi, lo) + (float) vec.y;
            float z = SodiumInteractor.unpackPositionZ(hi, lo) + (float) vec.z;
            
            int quantX = SodiumInteractor.quantizePosition(x);
            int quantY = SodiumInteractor.quantizePosition(y);
            int quantZ = SodiumInteractor.quantizePosition(z);
            
            MemoryUtil.memPutInt(ptr + 0L, SodiumInteractor.packPositionHi(quantX, quantY, quantZ));
            MemoryUtil.memPutInt(ptr + 4L, SodiumInteractor.packPositionLo(quantX, quantY, quantZ));
            
            MemoryUtil.memPutInt(ptr + 16L, (MemoryUtil.memGetInt(ptr + 16) & 0xFFFFFF) | ((sectionIndex & 0xFF) << 24));
            
            ptr += CompactChunkVertex.STRIDE;
            i += CompactChunkVertex.STRIDE;
        }
    }
    
    @Override
    public void applyOffset(Vec3 vec, int sectionIndex) {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                applySodiumOffset(buffers[i].byteBuffer(), vec, sectionIndex);
    }
    
    @Override
    public boolean isInvalid() {
        return invalid;
    }
    
    @Override
    public void invalidate() {
        invalid = true;
    }
    
    @Override
    public boolean isAvailable() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].isAvailable())
                return false;
        return true;
    }
    
    @Override
    public boolean isEmpty() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].isEmpty())
                return false;
        return true;
    }
    
    @Override
    public int lengthToUpload() {
        int length = 0;
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && buffers[i].isAvailable())
                length += buffers[i].lengthToUpload();
        return length;
    }
    
    @Override
    public int lengthToUpload(int facing) {
        if (buffers[facing] != null && buffers[facing].isAvailable())
            return buffers[facing].lengthToUpload();
        return 0;
    }
    
    @Override
    public void eraseBuffer() {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].eraseBuffer();
    }
    
    @Override
    public boolean upload(ChunkBufferUploader uploader) {
        for (TextureAtlasSprite texture : textures)
            uploader.addTexture(texture);
        
        if (uploader.hasFacingSupport()) {
            if (uploader instanceof SodiumBufferUploader u && u.isSorted()) {
                ChunkVertexType type = LittleRenderPipelineSodium.getType();
                int stride = type.getVertexFormat().getStride();
                LittleQuadView quad = new LittleQuadView();
                
                for (int i = 0; i < buffers.length; i++) {
                    if (buffers[i] == null)
                        continue;
                    
                    if (!buffers[i].upload(i, uploader))
                        return false; // Something went wrong
                        
                    // Add translucent data by going through the buffers, collecting the quads and adding to the translucent collector
                    ModelQuadFacing facing = ModelQuadFacing.values()[i];
                    ByteBuffer buffer = buffers[i].byteBuffer();
                    long ptr = MemoryUtil.memAddress(buffer);
                    long end = ptr + buffer.remaining();
                    var collector = u.getTranslucentCollector();
                    
                    while (ptr < end) {
                        quad.readVertices(ptr, 0, stride, facing);
                        collector.appendQuad(quad.getPackedNormal(), quad.getVertices(), facing);
                        ptr += stride * 4;
                    }
                }
                return true;
            }
            for (int i = 0; i < buffers.length; i++)
                if (buffers[i] != null && !buffers[i].upload(i, uploader))
                    return false;
            return true;
        }
        
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].upload(uploader))
                return false;
        return true;
    }
    
    @Override
    public boolean download(ChunkBufferDownloader downloader) {
        if (downloader.hasFacingSupport()) {
            for (int i = 0; i < buffers.length; i++)
                if (buffers[i] != null && !buffers[i].download(downloader.downloaded(i)))
                    return false;
            return true;
        }
        
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && !buffers[i].download(downloader.downloaded()))
                return false;
        return true;
    }
    
    public void moveInBuffer(int offset) {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].moveUploadIndex(offset);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(buffers);
    }
}
