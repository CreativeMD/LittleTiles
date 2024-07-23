package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
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
        public final RenderingLevelHandler origin;
        private final HashMap<BlockPos, RenderDataToAdd> caches = new HashMap<>();
        private int waitTill;
        
        public RenderDataLevel(Level level) {
            this.level = level;
            this.origin = RenderingLevelHandler.of(level);
        }
        
        private RenderDataToAdd getOrCreateBlock(RenderChunkUploader section, BlockPos pos) {
            RenderDataToAdd data = caches.get(pos);
            if (data == null) {
                caches.put(pos, data = new RenderDataToAdd());
                section.queue(data);
            }
            return data;
        }
        
        private RenderChunkUploader getOrCreateSection(RenderingLevelHandler target, Long2ObjectMap<RenderChunkUploader> sections, BlockPos pos) {
            long section = SectionPos.asLong(pos);
            var s = sections.get(section);
            if (s == null)
                sections.put(section, s = new RenderChunkUploader(target.getRenderChunk(level, section), SectionPos.of(section)));
            return s;
        }
        
        public void queue(Level targetLevel, LittleAnimationEntity entity) {
            RenderingLevelHandler target = RenderingLevelHandler.of(targetLevel);
            Long2ObjectMap<RenderChunkUploader> sections = new Long2ObjectOpenHashMap<>();
            for (Entry<BlockPos, RenderDataToAdd> entry : caches.entrySet())
                getOrCreateSection(target, sections, entry.getKey()).queue(entry.getValue());
            for (BETiles be : entity.getSubLevel()) {
                var section = getOrCreateSection(target, sections, be.getBlockPos());
                getOrCreateBlock(section, be.getBlockPos()).queueNew(target, be, section.pos);
            }
            waitTill = LittleTilesClient.ANIMATION_HANDLER.longTickIndex + LittleAnimationHandlerClient.MAX_INTERVALS_WAITING;
            
            if (LittleTiles.CONFIG.rendering.uploadToVBODirectly) {
                for (RenderChunkUploader section : sections.values())
                    section.appendRenderData();
            } else
                for (RenderChunkUploader section : sections.values())
                    section.markReadyForUpdate();
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
        
        private class RenderDataToAdd implements LayeredBufferCache {
            
            private final ChunkLayerMap<BufferCache> holders = new ChunkLayerMap<>();
            
            @Override
            public BufferCache get(RenderType layer) {
                return holders.get(layer);
            }
            
            public void queueNew(RenderingLevelHandler target, BETiles be, SectionPos pos) {
                BlockBufferCache cache = be.render.getBufferCache();
                Vec3 vec = RenderingLevelHandler.offsetCorrection(target, origin, pos);
                
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    BufferCache holder = cache.get(layer);
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
    
    private static class RenderChunkUploader {
        
        public final RenderChunkExtender section;
        public final SectionPos pos;
        private final List<RenderDataLevel.RenderDataToAdd> entries = new ArrayList<>();
        
        public RenderChunkUploader(RenderChunkExtender section, SectionPos pos) {
            this.section = section;
            this.pos = pos;
            this.section.backToRAM();
        }
        
        public void appendRenderData() {
            if (!section.appendRenderData(entries))
                markReadyForUpdate();
        }
        
        public void markReadyForUpdate() {
            section.markReadyForUpdate(false);
        }
        
        public void queue(RenderDataLevel.RenderDataToAdd data) {
            this.entries.add(data);
        }
    }
}