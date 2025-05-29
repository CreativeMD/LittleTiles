package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.render.cache.IBlockBufferCache;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.AdditionalBuffers;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
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
            if (getOrCreate(targetLevel).queue(entity.getUUID(), entity.getSubLevel(), entity.getSubLevel())) // Delete it if all cache has already been added to the blocks otherwise wait
                CACHES.remove(entity.getSubLevel());
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
        
        public final Level targetLevel;
        public final RenderingLevelHandler target;
        private final HashMap<BlockPos, RenderDataToAdd> caches = new HashMap<>();
        private int waitTill;
        
        public RenderDataLevel(Level level) {
            this.targetLevel = level;
            this.target = RenderingLevelHandler.of(level);
        }
        
        private RenderDataToAdd getOrCreateBlock(RenderChunkUploader section, BlockPos pos) {
            RenderDataToAdd data = caches.get(pos);
            if (data == null) {
                caches.put(pos, data = new RenderDataToAdd());
                section.queue(data);
            }
            return data;
        }
        
        private RenderChunkUploader getOrCreateSection(RenderingLevelHandler origin, Level originLevel, Long2ObjectMap<RenderChunkUploader> sections, BlockPos pos) {
            long section = SectionPos.asLong(pos);
            var s = sections.get(section);
            if (s == null) {
                origin.getRenderChunk(originLevel, section).backToRAM(); // Make sure data is available
                sections.put(section, s = new RenderChunkUploader(target.getRenderChunk(targetLevel, section), SectionPos.of(pos)));
            }
            return s;
        }
        
        public boolean queue(UUID uuid, Level originLevel, Iterable<BETiles> blocks) {
            RenderingLevelHandler origin = RenderingLevelHandler.of(originLevel);
            Long2ObjectMap<RenderChunkUploader> sections = new Long2ObjectOpenHashMap<>();
            for (Entry<BlockPos, RenderDataToAdd> entry : caches.entrySet())
                getOrCreateSection(origin, originLevel, sections, entry.getKey()).queue(entry.getValue());
            for (BETiles be : blocks) {
                var section = getOrCreateSection(origin, originLevel, sections, be.getBlockPos());
                getOrCreateBlock(section, be.getBlockPos()).queueNew(uuid, origin, originLevel, be, section.pos);
            }
            
            waitTill = LittleTilesClient.ANIMATION_HANDLER.longTickIndex + LittleAnimationHandlerClient.MAX_INTERVALS_WAITING;
            
            if (LittleTiles.CONFIG.rendering.uploadToVBODirectly)
                for (RenderChunkUploader section : sections.values())
                    section.appendRenderData();
            else
                for (RenderChunkUploader section : sections.values())
                    section.markReadyForUpdate();
                
            for (Iterator<Entry<BlockPos, RenderDataToAdd>> iterator = caches.entrySet().iterator(); iterator.hasNext();) {
                var entry = iterator.next();
                if (entry.getValue().isDone())
                    iterator.remove();
            }
            return caches.isEmpty();
        }
        
        public boolean isEmpty() {
            return caches.isEmpty();
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
        
        private class RenderDataToAdd extends AdditionalBuffers {
            
            private boolean done;
            
            public void queueNew(UUID uuid, RenderingLevelHandler origin, Level originLevel, BETiles be, SectionPos pos) {
                IBlockBufferCache cache = be.render.buffers();
                Vec3 vec = RenderingLevelHandler.offsetCorrection(target, targetLevel, origin, originLevel, pos);
                int sectionIndex = target.sectionIndex(targetLevel, pos.asLong());
                LayeredBufferCache layers = new LayeredBufferCache();
                for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
                    BufferCache holder = cache.getIncludingAdditional(layer);
                    if (holder == null)
                        continue;
                    if (vec != null)
                        holder.applyOffset(vec, sectionIndex);
                    layers.put(layer, holder);
                }
                additional(uuid, layers);
                
                var target = BlockTile.loadBE(targetLevel, be.getBlockPos());
                if (target != null) {
                    target.render.additionalBuffers(x -> x.additional(this));
                    done = true;
                }
            }
            
            public boolean isDone() {
                return done;
            }
            
            public void receiveUpdate(BETiles be) {
                be.render.additionalBuffers(x -> x.additional(this));
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
            List<LayeredBufferCache> buffers = new ArrayList<>();
            for (RenderDataLevel.RenderDataToAdd d : entries)
                for (LayeredBufferCache b : d.additionals())
                    buffers.add(b);
            if (!section.appendRenderData(buffers))
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