package team.creative.littletiles.client.render.level;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import team.creative.creativecore.client.render.model.BufferBuilderUtils;
import team.creative.littletiles.client.render.LittleRenderUtils;
import team.creative.littletiles.client.render.cache.ChunkBlockLayerCache;
import team.creative.littletiles.client.render.cache.LayeredRenderBufferCache;
import team.creative.littletiles.client.render.level.RenderUploader.NotSupportedException;
import team.creative.littletiles.common.block.entity.BETiles;

public class LittleRenderChunk {
    
    protected int lastRenderIndex = LittleChunkDispatcher.currentRenderState;
    
    public final BlockPos pos;
    protected final VertexBuffer[] vertexBuffers = new VertexBuffer[LittleRenderUtils.BLOCK_LAYERS.length];
    protected BufferBuilder[] builders = new BufferBuilder[LittleRenderUtils.BLOCK_LAYERS.length];
    
    protected ChunkBlockLayerCache[] cachedBuffers = new ChunkBlockLayerCache[LittleRenderUtils.BLOCK_LAYERS.length];
    protected boolean[] bufferChanged = new boolean[LittleRenderUtils.BLOCK_LAYERS.length];
    
    private LinkedHashMap<BlockPos, BETiles> blockEntities = new LinkedHashMap<>();
    /** if one of the blocks has been modified, which requires the chunk cache to be uploaded again */
    private boolean modified = false;
    private boolean complete = false;
    
    public LittleRenderChunk(BlockPos pos) {
        this.pos = pos;
        for (int i = 0; i < cachedBuffers.length; i++)
            cachedBuffers[i] = new ChunkBlockLayerCache(i);
    }
    
    public int transparencySortedIndex = 0;
    
    public void addRenderData(BETiles te) {
        synchronized (blockEntities) {
            BETiles existing = blockEntities.get(te.getBlockPos());
            
            if (existing != null) {
                if (existing != te) {
                    if (te.isEmpty())
                        blockEntities.remove(te.getBlockPos());
                    else
                        blockEntities.put(te.getBlockPos(), te);
                } else if (te.isEmpty())
                    blockEntities.remove(te.getBlockPos());
                
                modified = true;
            } else {
                if (te.isEmpty())
                    return;
                
                blockEntities.put(te.getBlockPos(), te);
                
                if (complete)
                    modified = true;
                
                if (!modified)
                    addRenderDataInternal(te);
            }
            
            if (modified)
                complete = false;
        }
    }
    
    private void addRenderDataInternal(BETiles te) {
        LayeredRenderBufferCache cache = te.render.getBufferCache();
        for (int i = 0; i < LittleRenderUtils.BLOCK_LAYERS.length; i++)
            cachedBuffers[i].add(te.render, cache.get(i));
    }
    
    public void resortTransparency(int index, float x, float y, float z) {
        if (index == transparencySortedIndex)
            return;
        
        this.transparencySortedIndex = index;
        
        int translucentIndex = LittleRenderUtils.TRANSLUCENT;
        
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
                    BufferBuilder tempBuffer = new BufferBuilder(DefaultVertexFormat.BLOCK.getVertexSize() + cachedBuffers[i].expanded());
                    tempBuffer.begin(7, DefaultVertexFormat.BLOCK);
                    tempBuffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
                    builders[i] = tempBuffer;
                } else
                    BufferBuilderUtils.ensureTotalSize(builders[i], builders[i].getVertexFormat().getVertexSize() + cachedBuffers[i].expanded());
                cachedBuffers[i].fillBuilder(builders[i]);
                bufferChanged[i] = true;
            }
        }
        
    }
    
    public void uploadBuffer() {
        synchronized (blockEntities) {
            if (modified) {
                backToRAM();
                for (int i = 0; i < vertexBuffers.length; i++) {
                    cachedBuffers[i].reset();
                    builders[i] = null;
                }
                modified = false;
                for (BETiles te : blockEntities.values())
                    addRenderDataInternal(te);
            }
            
            processQueue();
            
            for (int i = 0; i < bufferChanged.length; i++) {
                if (bufferChanged[i]) {
                    if (vertexBuffers[i] == null)
                        vertexBuffers[i] = new VertexBuffer(DefaultVertexFormat.BLOCK);
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
        synchronized (blockEntities) {
            complete = true;
        }
    }
    
    public void backToRAM() {
        synchronized (blockEntities) {
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
        synchronized (blockEntities) {
            for (int i = 0; i < vertexBuffers.length; i++) {
                if (vertexBuffers[i] != null)
                    vertexBuffers[i].deleteGlBuffers();
                builders[i] = null;
                cachedBuffers[i].reset();
            }
            blockEntities.clear();
        }
    }
}
