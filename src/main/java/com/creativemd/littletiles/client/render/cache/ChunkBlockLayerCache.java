package com.creativemd.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.world.TileEntityRenderManager;

import net.minecraft.client.renderer.BufferBuilder;

public class ChunkBlockLayerCache {
    
    private final int layer;
    private int totalSize;
    private List<BlockRenderCache> caches = new ArrayList<>();
    private int expanded = 0;
    
    public ChunkBlockLayerCache(int layer) {
        this.layer = layer;
    }
    
    public int expanded() {
        return expanded;
    }
    
    public void reset() {
        caches = new ArrayList<>();
        totalSize = 0;
        expanded = 0;
    }
    
    public void fillBuilder(BufferBuilder builder) {
        for (BlockRenderCache cache : caches)
            cache.fill(builder);
        totalSize = BufferBuilderUtils.getBufferSizeByte(builder);
    }
    
    public void fillBuffer(ByteBuffer buffer) {
        for (BlockRenderCache cache : caches)
            cache.fill(buffer);
        totalSize = buffer.position();
    }
    
    public void add(TileEntityRenderManager manager, IRenderDataCache data) {
        synchronized (manager) {
            if (data == null)
                return;
            ByteBuffer buffer = data.byteBuffer();
            if (buffer == null)
                return;
            expanded += data.length();
            caches.add(new BlockRenderCache(manager, layer, data, buffer));
        }
    }
    
    public void discard() {
        for (BlockRenderCache cache : caches) {
            synchronized (cache.manager) {
                if (cache.manager.getBufferCache() == null)
                    continue;
                cache.manager.getBufferCache().setEmptyIfEqual(cache.link, layer);
            }
        }
        reset();
        
    }
    
    public int totalSize() {
        return totalSize;
    }
    
    public void download(ByteBuffer buffer) {
        for (BlockRenderCache cache : caches) {
            synchronized (cache.manager) {
                try {
                    BufferLink link = cache.link;
                    if (buffer.capacity() >= link.index + link.length) {
                        ByteBuffer newBuffer = ByteBuffer.allocateDirect(link.length);
                        buffer.position(link.index);
                        int end = link.index + link.length;
                        while (buffer.position() < end)
                            newBuffer.put(buffer.get());
                        link.downloaded(newBuffer);
                    }
                } catch (IllegalArgumentException e) {}
            }
        }
    }
    
    public void uploaded() {
        for (BlockRenderCache cache : caches) {
            synchronized (cache.manager) {
                if (cache.manager.getBufferCache() == null)
                    continue;
                cache.link.uploaded();
            }
        }
        expanded = 0;
    }
    
    public boolean isEmpty() {
        return caches.isEmpty();
    }
    
}
