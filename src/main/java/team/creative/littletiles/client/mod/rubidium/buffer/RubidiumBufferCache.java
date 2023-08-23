package team.creative.littletiles.client.mod.rubidium.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public class RubidiumBufferCache implements BufferCache {
    
    private final BufferHolder[] buffers;
    private List<TextureAtlasSprite> textures;
    private int groupCount;
    private boolean invalid;
    
    public RubidiumBufferCache(BufferHolder[] buffers, List<TextureAtlasSprite> textures, int groupCount) {
        this.buffers = buffers;
        this.textures = textures;
        this.groupCount = groupCount;
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
        for (int i = 0; i < buffers.length; i++)
            if (this.buffers[i] != null)
                buffers[i] = this.buffers[i].extract(index);
        groupCount--;
        return new RubidiumBufferCache(buffers, textures, 1);
    }
    
    @Override
    public BufferCache combine(BufferCache cache) {
        if (cache instanceof RubidiumBufferCache r) {
            List<TextureAtlasSprite> sprites = new ArrayList<>();
            for (TextureAtlasSprite texture : r.getUsedTextures())
                if (!sprites.contains(texture))
                    sprites.add(texture);
            BufferHolder[] buffers = new BufferHolder[ModelQuadFacing.COUNT];
            for (int i = 0; i < buffers.length; i++)
                buffers[i] = BufferHolder.combine(this.buffers[i], r.buffer(ModelQuadFacing.VALUES[i]));
            return new RubidiumBufferCache(buffers, sprites, groupCount + r.groupCount());
        }
        
        if (!(cache instanceof BufferHolder))
            return null;
        
        BufferHolder[] buffers = Arrays.copyOf(this.buffers, ModelQuadFacing.COUNT);
        
        int un = ModelQuadFacing.UNASSIGNED.ordinal();
        buffers[un] = BufferHolder.combine(this.buffers[un], (BufferHolder) cache);
        
        return new RubidiumBufferCache(buffers, textures, groupCount + cache.groupCount());
    }
    
    @Override
    public void applyOffset(Vec3 vec) {
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null)
                buffers[i].applyOffset(vec);
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
    public int lengthToUpload() {
        int length = 0;
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && buffers[i].isAvailable())
                length += buffers[i].lengthToUpload();
        return length;
    }
    
    @Override
    public int lengthToUpload(int facing) {
        int length = 0;
        for (int i = 0; i < buffers.length; i++)
            if (buffers[i] != null && buffers[i].isAvailable())
                length += buffers[i].lengthToUpload(facing);
        return length;
    }
    
    @Override
    public int groupCount() {
        return groupCount;
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
            uploader.addSprite(texture);
        
        if (uploader.hasFacingSupport()) {
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
    
}
