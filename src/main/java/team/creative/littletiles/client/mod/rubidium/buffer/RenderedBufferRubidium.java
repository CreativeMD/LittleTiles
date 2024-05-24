package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.platform.MemoryTracker;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public class RenderedBufferRubidium implements AutoCloseable, ChunkBufferUploader {
    
    private ByteBuffer buffer;
    private Set<TextureAtlasSprite> animatedSprites = new ObjectOpenHashSet<>();
    
    public RenderedBufferRubidium(BufferCollection collection) {
        buffer = MemoryTracker.create(collection.length());
        for (BufferCache cache : collection.buffers())
            cache.upload(this);
        buffer.rewind();
    }
    
    @Override
    public boolean hasFacingSupport() {
        return false;
    }
    
    @Override
    public void upload(ByteBuffer buffer) {
        this.buffer.put(buffer);
        buffer.rewind();
    }
    
    @Override
    public int uploadIndex() {
        return this.buffer.position();
    }
    
    @Override
    public void upload(int facing, ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int uploadIndex(int facing) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addSprite(TextureAtlasSprite texture) {
        animatedSprites.add(texture);
    }
    
    public int vertices(GlVertexFormat format) {
        return buffer.limit() / format.getStride();
    }
    
    public ByteBuffer byteBuffer() {
        return buffer;
    }
    
    public Collection<TextureAtlasSprite> sprites() {
        return animatedSprites;
    }
    
    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(buffer);
    }
    
    @Override
    public boolean isSorted() {
        return false;
    }
}
