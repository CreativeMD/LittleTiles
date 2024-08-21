package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.render.block.BERenderManager;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.IBlockBufferCache;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;

@OnlyIn(Dist.CLIENT)
public class RenderUploader {
    
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
    
    public static class RenderDataLevel {
        
        public final Level level;
        private final HashMap<BlockPos, RenderDataToAdd> caches = new HashMap<>();
        private int waitTill;
        
        public RenderDataLevel(Level level) {
            this.level = level;
        }
        
        private RenderDataToAdd getOrCreate(Level targetLevel, RenderChunkExtender chunk, BlockPos pos) {
            RenderDataToAdd data = caches.get(pos);
            if (data == null)
                caches.put(pos, data = new RenderDataToAdd(targetLevel, chunk));
            return data;
        }
        
        public void queue(Level targetLevel, LittleAnimationEntity entity) {
            HashSet<RenderChunkExtender> chunks = new HashSet<>();
            for (BETiles be : entity.getSubLevel()) {
                RenderChunkExtender chunk = be.render.getRenderChunk();
                if (chunks.add(chunk))
                    chunk.backToRAM();
                
                RenderDataToAdd block = getOrCreate(targetLevel, BERenderManager.getRenderChunk(targetLevel, be.getBlockPos()), be.getBlockPos());
                block.queueNew(be);
            }
            waitTill = LittleTilesClient.ANIMATION_HANDLER.longTickIndex + LittleAnimationHandlerClient.MAX_INTERVALS_WAITING;
            
            HashMapList<RenderChunkExtender, RenderDataToAdd> chunksList = new HashMapList<>();
            for (RenderDataToAdd block : caches.values())
                chunksList.add(block.targetChunk, block);
            
            if (LittleTiles.CONFIG.rendering.uploadToVBODirectly) {
                for (Entry<RenderChunkExtender, ArrayList<RenderDataToAdd>> entry : chunksList.entrySet())
                    if (!entry.getKey().appendRenderData(entry.getValue()))
                        entry.getKey().markReadyForUpdate(false);
            } else
                for (RenderChunkExtender chunk : chunksList.keySet())
                    chunk.markReadyForUpdate(false);
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
        
        private final ChunkLayerMap<BufferCache> holders = new ChunkLayerMap<>();
        private final Level targetLevel;
        private final RenderChunkExtender targetChunk;
        private BETiles cached = null;
        private boolean toSearch = true;
        
        public RenderDataToAdd(Level targetLevel, RenderChunkExtender chunk) {
            this.targetLevel = targetLevel;
            targetChunk = chunk;
        }
        
        @Override
        public BufferCache get(RenderType layer) {
            return holders.get(layer);
        }
        
        public void queueNew(BETiles be) {
            IBlockBufferCache cache = be.render.buffers();
            Vec3 vec = targetChunk.offsetCorrection(be.render.getRenderChunk());
            
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                BufferCache holder = cache.get(layer);
                if (holder == null)
                    continue;
                if (vec != null)
                    holder.applyOffset(vec);
                holder.markAsAdditional();
                holders.put(layer, BlockBufferCache.combine(holders.get(layer), holder));
            }
            
            if (toSearch) {
                cached = BlockTile.loadBE(targetLevel, be.getBlockPos());
                if (cached != null)
                    cached.render.additionalBuffersEarly(x -> x.additional(this));
                toSearch = false;
            }
        }
        
        public void receiveUpdate(BETiles be) {
            if (cached != be)
                be.render.additionalBuffers(x -> x.additional(this));
        }
        
    }
}