package team.creative.littletiles.client.render.level;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL15;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

@OnlyIn(Dist.CLIENT)
public class RenderUploader {
    
    private static final Minecraft mc = Minecraft.getInstance();
    private static final HashMap<Level, RenderDataLevel> CACHES = new HashMap<>();
    
    private static RenderDataLevel getOrCreate(Level level) {
        RenderDataLevel data = CACHES.get(level);
        if (data == null)
            CACHES.put(level, data = new RenderDataLevel(level));
        return data;
    }
    
    public static void queue(Level targetLevel, LittleAnimationEntity entity) {
        synchronized (CACHES) {
            getOrCreate(entity.level()).queue(targetLevel, entity);
        }
    }
    
    public static void notifyReceiveClientUpdate(BETiles be) {
        if (CACHES.isEmpty())
            return;
        synchronized (CACHES) {
            RenderDataLevel data = CACHES.get(be.getLevel());
            if (data != null && data.notifyReceiveClientUpdate(be))
                CACHES.remove(be.getLevel());
        }
    }
    
    public static void unload() {
        CACHES.clear();
    }
    
    public static void longTick(int index) {
        for (Iterator<RenderDataLevel> iterator = CACHES.values().iterator(); iterator.hasNext();) {
            RenderDataLevel level = iterator.next();
            if (level.longTick(index))
                iterator.remove();
        }
    }
    
    public static void uploadRenderData(RenderChunkExtender chunk, Iterable<? extends LayeredBufferCache> blocks) {
        if (OptifineHelper.isRenderRegions() || !LittleTiles.CONFIG.rendering.uploadToVBODirectly)
            return;
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            ChunkLayerCache cache = new ChunkLayerCache();
            
            int size = 0;
            for (LayeredBufferCache data : blocks)
                size += data.length(layer);
            
            if (size == 0)
                continue;
            
            try {
                VertexBuffer uploadBuffer = chunk.getVertexBuffer(layer);
                
                if (uploadBuffer == null)
                    return;
                
                VertexFormat format = uploadBuffer.getFormat();
                if (format == null)
                    format = DefaultVertexFormat.BLOCK;
                ChunkRenderDispatcher dispatcher = mc.levelRenderer.getChunkRenderDispatcher();
                
                ByteBuffer vanillaBuffer = null;
                if (!chunk.isEmpty(layer)) {
                    GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((VertexBufferExtender) uploadBuffer).getVertexBufferId());
                    vanillaBuffer = glMapBufferRange(((VertexBufferExtender) uploadBuffer).getLastUploadedLength());
                    VertexBuffer.unbind();
                }
                
                BufferBuilder builder = new BufferBuilder(((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + size + DefaultVertexFormat.BLOCK.getVertexSize()) / 6); // dividing by 6 is risking and could potentially cause issues
                chunk.begin(builder);
                if (vanillaBuffer != null) {
                    if (layer == RenderType.translucent()) {
                        SortState state = chunk.getTransparencyState();
                        if (state != null)
                            builder.restoreSortState(state);
                    }
                    
                    builder.putBulkData(vanillaBuffer);
                }
                
                for (LayeredBufferCache data : blocks)
                    cache.add(builder, data.get(layer));
                
                if (layer == RenderType.translucent())
                    chunk.setQuadSorting(builder, dispatcher.getCameraPosition());
                
                uploadBuffer.bind();
                uploadBuffer.upload(builder.end());
                VertexBuffer.unbind();
                chunk.setHasBlock(layer);
            } catch (IllegalArgumentException | NotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static ByteBuffer glMapBufferRange(long length) throws NotSupportedException {
        try {
            ByteBuffer result = MemoryTracker.create((int) length);
            GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, result);
            return result;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (e instanceof IllegalStateException)
                throw new NotSupportedException(e);
            else
                e.printStackTrace();
        }
        return null;
    }
    
    public static class NotSupportedException extends Exception {
        
        public NotSupportedException(Exception e) {
            super(e);
        }
        
    }
    
    public static class RenderDataLevel {
        
        public final Level level;
        private final HashMap<BlockPos, RenderDataToAdd> caches = new HashMap<>();
        private int waitTill;
        
        public RenderDataLevel(Level level) {
            this.level = level;
        }
        
        private RenderDataToAdd getOrCreate(RenderChunkExtender chunk, BlockPos pos) {
            RenderDataToAdd data = caches.get(pos);
            if (data == null)
                caches.put(pos, data = new RenderDataToAdd(chunk));
            return data;
        }
        
        public void queue(Level targetLevel, LittleAnimationEntity entity) {
            HashSet<RenderChunkExtender> chunks = new HashSet<>();
            for (BETiles be : entity.getSubLevel()) {
                RenderChunkExtender chunk = be.render.getRenderChunk();
                if (chunks.add(chunk)) {
                    for (RenderType layer : RenderType.chunkBufferLayers()) {
                        VertexBufferExtender buffer = (VertexBufferExtender) chunk.getVertexBuffer(layer);
                        ChunkLayerUploadManager manager = buffer.getManager();
                        if (manager != null)
                            manager.backToRAM();
                    }
                }
                
                RenderDataToAdd block = getOrCreate(BERenderManager.getRenderChunk(targetLevel, be.getBlockPos()), be.getBlockPos());
                block.queueNew(be);
            }
            waitTill = LittleTilesClient.ANIMATION_HANDLER.longTickIndex + LittleAnimationHandlerClient.MAX_INTERVALS_WAITING;
            
            HashMapList<RenderChunkExtender, RenderDataToAdd> chunksList = new HashMapList<>();
            for (RenderDataToAdd block : caches.values())
                chunksList.add(block.targetChunk, block);
            
            for (Entry<RenderChunkExtender, ArrayList<RenderDataToAdd>> entry : chunksList.entrySet())
                uploadRenderData(entry.getKey(), entry.getValue());
        }
        
        public boolean notifyReceiveClientUpdate(BETiles be) {
            RenderDataToAdd data = caches.remove(be.getBlockPos());
            if (data != null)
                data.receiveUpdate(be);
            return caches.isEmpty();
        }
        
        public boolean longTick(int index) {
            return index >= waitTill;
        }
        
    }
    
    private static class RenderDataToAdd implements LayeredBufferCache {
        
        private final ChunkLayerMap<BufferHolder> holders = new ChunkLayerMap<>();
        private final RenderChunkExtender targetChunk;
        
        public RenderDataToAdd(RenderChunkExtender chunk) {
            targetChunk = chunk;
        }
        
        @Override
        public int length(RenderType type) {
            BufferHolder holder = holders.get(type);
            if (holder != null)
                return holder.length();
            return 0;
        }
        
        @Override
        public BufferHolder get(RenderType layer) {
            return holders.get(layer);
        }
        
        public void queueNew(BETiles be) {
            BlockBufferCache cache = be.render.getBufferCache();
            Vec3 vec = targetChunk.offsetCorrection(be.render.getRenderChunk());
            
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                BufferHolder holder = cache.get(layer);
                if (holder == null)
                    continue;
                if (vec != null)
                    holder.applyOffset(vec);
                holders.put(layer, BlockBufferCache.combine(holders.get(layer), holder));
            }
        }
        
        public void receiveUpdate(BETiles be) {
            be.render.getBufferCache().additional(this);
        }
        
    }
}