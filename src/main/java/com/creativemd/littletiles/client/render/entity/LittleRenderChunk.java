package com.creativemd.littletiles.client.render.entity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.cache.ChunkBlockLayerCache;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUploader.NotSupportedException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

public class LittleRenderChunk {
    
    protected int lastRenderIndex = LittleChunkDispatcher.currentRenderState;
    
    public final BlockPos pos;
    protected final VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
    protected BufferBuilder[] builders = new BufferBuilder[BlockRenderLayer.values().length];
    
    protected ChunkBlockLayerCache[] cachedBuffers = new ChunkBlockLayerCache[BlockRenderLayer.values().length];
    protected boolean[] bufferChanged = new boolean[BlockRenderLayer.values().length];
    
    private LinkedHashMap<BlockPos, TileEntityLittleTiles> tileEntities = new LinkedHashMap<>();
    /** if one of the blocks has been modified, which requires the chunk cache to be uploaded again */
    private boolean modified = false;
    private boolean complete = false;
    
    public LittleRenderChunk(BlockPos pos) {
        this.pos = pos;
        for (int i = 0; i < cachedBuffers.length; i++)
            cachedBuffers[i] = new ChunkBlockLayerCache(i);
    }
    
    public int transparencySortedIndex = 0;
    
    public void addRenderData(TileEntityLittleTiles te) {
        synchronized (tileEntities) {
            TileEntityLittleTiles existing = tileEntities.get(te.getPos());
            
            if (existing != null) {
                if (existing != te) {
                    if (te.isRenderingEmpty())
                        tileEntities.remove(te.getPos());
                    else
                        tileEntities.put(te.getPos(), te);
                } else if (te.isRenderingEmpty())
                    tileEntities.remove(te.getPos());
                
                modified = true;
            } else {
                if (te.isRenderingEmpty())
                    return;
                
                tileEntities.put(te.getPos(), te);
                
                if (complete)
                    modified = true;
                
                if (!modified)
                    addRenderDataInternal(te);
            }
            
            if (modified)
                complete = false;
        }
    }
    
    private void addRenderDataInternal(TileEntityLittleTiles te) {
        LayeredRenderBufferCache cache = te.render.getBufferCache();
        for (int i = 0; i < BlockRenderLayer.values().length; i++)
            cachedBuffers[i].add(te.render, cache.get(i));
    }
    
    public void resortTransparency(int index, float x, float y, float z) {
        if (index == transparencySortedIndex)
            return;
        
        this.transparencySortedIndex = index;
        
        int translucentIndex = BlockRenderLayer.TRANSLUCENT.ordinal();
        
        BufferBuilder builder = builders[translucentIndex];
        if (builder != null) {
            builder.sortVertexData(x, y, z);
            if (vertexBuffers[translucentIndex] != null)
                vertexBuffers[translucentIndex].deleteGlBuffers();
            vertexBuffers[translucentIndex] = new VertexBuffer(DefaultVertexFormats.BLOCK);
            vertexBuffers[translucentIndex].bufferData(builder.getByteBuffer());
        }
    }
    
    protected void processQueue() {
        for (int i = 0; i < cachedBuffers.length; i++) {
            if (cachedBuffers[i].expanded() > (builders[i] != null ? BufferBuilderUtils.getBufferSizeByte(builders[i]) : 0)) {
                if (builders[i] == null) {
                    BufferBuilder tempBuffer = new BufferBuilder(DefaultVertexFormats.BLOCK.getNextOffset() + cachedBuffers[i].expanded());
                    tempBuffer.begin(7, DefaultVertexFormats.BLOCK);
                    tempBuffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
                    builders[i] = tempBuffer;
                } else
                    BufferBuilderUtils.ensureTotalSize(builders[i], builders[i].getVertexFormat().getNextOffset() + cachedBuffers[i].expanded());
                cachedBuffers[i].fillBuilder(builders[i]);
                bufferChanged[i] = true;
            }
        }
        
    }
    
    public void uploadBuffer() {
        synchronized (tileEntities) {
            if (modified) {
                backToRAM();
                for (int i = 0; i < vertexBuffers.length; i++) {
                    cachedBuffers[i].reset();
                    builders[i] = null;
                }
                modified = false;
                for (TileEntityLittleTiles te : tileEntities.values())
                    addRenderDataInternal(te);
            }
            
            processQueue();
            
            for (int i = 0; i < bufferChanged.length; i++) {
                if (bufferChanged[i]) {
                    if (vertexBuffers[i] == null)
                        vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
                    int vertexCount = builders[i].getVertexCount();
                    builders[i].getByteBuffer().rewind();
                    builders[i].getByteBuffer().limit(BufferBuilderUtils.getBufferSizeByte(builders[i]));
                    vertexBuffers[i].bufferData(builders[i].getByteBuffer());
                    bufferChanged[i] = false;
                }
            }
            
            if (complete) {
                for (int j = 0; j < builders.length; j++)
                    if (j != BlockRenderLayer.TRANSLUCENT.ordinal() && builders[j] != null) {
                        builders[j] = null;
                        cachedBuffers[j].uploaded();
                    }
                complete = false;
            }
            
            if (lastRenderIndex != LittleChunkDispatcher.currentRenderState) {
                Collection<TileEntityLittleTiles> temp = new ArrayList<>(tileEntities.values());
                tileEntities.clear();
                for (TileEntityLittleTiles te : temp)
                    te.updateQuadCache(this);
            }
        }
    }
    
    public VertexBuffer getLayerBuffer(BlockRenderLayer layer) {
        return vertexBuffers[layer.ordinal()];
    }
    
    public void markCompleted() {
        synchronized (tileEntities) {
            complete = true;
        }
    }
    
    public void backToRAM() {
        synchronized (tileEntities) {
            for (int j = 0; j < vertexBuffers.length; j++)
                if (j != BlockRenderLayer.TRANSLUCENT.ordinal() && !cachedBuffers[j].isEmpty()) {
                    if (vertexBuffers[j] == null)
                        continue;
                    vertexBuffers[j].bindBuffer();
                    try {
                        ByteBuffer uploadedData = RenderUploader.glMapBufferRange(cachedBuffers[j].totalSize());
                        if (uploadedData != null)
                            cachedBuffers[j].download(uploadedData);
                        else
                            cachedBuffers[j].discard();
                    } catch (NotSupportedException e) {
                        e.printStackTrace();
                    }
                    vertexBuffers[j].unbindBuffer();
                }
        }
    }
    
    public void unload() {
        synchronized (tileEntities) {
            for (int i = 0; i < vertexBuffers.length; i++) {
                if (vertexBuffers[i] != null)
                    vertexBuffers[i].deleteGlBuffers();
                builders[i] = null;
                cachedBuffers[i].reset();
            }
            tileEntities.clear();
        }
    }
}
