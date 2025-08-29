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

public class RenderAdditional {
    
    public final Level targetLevel;
    public final RenderingLevelHandler target;
    private final Long2ObjectMap<SectionAdditional> sections = new Long2ObjectOpenHashMap<>();
    private int waitTill;
    
    public RenderAdditional(Level level) {
        this.targetLevel = level;
        this.target = RenderingLevelHandler.of(level);
    }
    
    private SectionAdditional getOrCreateSection(RenderingLevelHandler origin, Level originLevel, BlockPos pos) {
        long section = SectionPos.asLong(pos);
        var s = sections.get(section);
        if (s == null) {
            origin.getRenderChunk(originLevel, section).backToRAM(); // Make sure data is available
            sections.put(section, s = new SectionAdditional(target.getRenderChunk(targetLevel, section), SectionPos.of(pos)));
        }
        return s;
    }
    
    public boolean queue(UUID uuid, Level originLevel, Iterable<BETiles> blocks) {
        RenderingLevelHandler origin = RenderingLevelHandler.of(originLevel);
        
        for (BETiles be : blocks)
            getOrCreateSection(origin, originLevel, be.getBlockPos()).queueNew(uuid, origin, originLevel, be);
        
        waitTill = LittleTilesClient.ANIMATION_HANDLER.longTickIndex + LittleAnimationHandlerClient.MAX_INTERVALS_WAITING;
        
        for (Iterator<Long2ObjectMap.Entry<SectionAdditional>> iterator = sections.long2ObjectEntrySet().iterator(); iterator.hasNext();)
            if (iterator.next().getValue().finishInitial())
                iterator.remove();
            
        return sections.isEmpty();
    }
    
    public boolean isEmpty() {
        return sections.isEmpty();
    }
    
    public boolean notifyReceiveClientUpdate(BETiles be) {
        var pos = SectionPos.asLong(be.getBlockPos());
        var s = sections.get(pos);
        if (s != null && s.notifyReceiveClientUpdate(be))
            sections.remove(pos);
        return sections.isEmpty();
    }
    
    public boolean longTick(int index) {
        return index >= waitTill;
    }
    
    public class SectionAdditional {
        
        public final RenderChunkExtender section;
        public final SectionPos pos;
        private final HashMap<BlockPos, BlockAdditional> entries = new HashMap<>();
        
        public SectionAdditional(RenderChunkExtender section, SectionPos pos) {
            this.section = section;
            this.pos = pos;
            this.section.backToRAM();
            this.section.setAdditional(this);
        }
        
        private BlockAdditional getOrCreateBlock(BlockPos pos) {
            BlockAdditional data = entries.get(pos);
            if (data == null)
                entries.put(pos, data = new BlockAdditional());
            return data;
        }
        
        public void queueNew(UUID uuid, RenderingLevelHandler origin, Level originLevel, BETiles be) {
            getOrCreateBlock(be.getBlockPos()).queueNew(uuid, origin, originLevel, be, pos);
        }
        
        public void appendRenderData(boolean markUpdateOtherwise) {
            List<LayeredBufferCache> buffers = new ArrayList<>();
            for (BlockAdditional d : entries.values())
                for (LayeredBufferCache b : d.additionals())
                    buffers.add(b);
            if (!section.appendRenderData(buffers) && markUpdateOtherwise)
                markReadyForUpdate();
        }
        
        public void onSectionUploads() {
            if (deleteAndRemoveIfEmpty())
                sections.remove(pos.asLong());
            else
                appendRenderData(false);
        }
        
        public void markReadyForUpdate() {
            section.markReadyForUpdate(false);
        }
        
        private boolean deleteAndRemoveIfEmpty() {
            for (Iterator<Entry<BlockPos, BlockAdditional>> iterator = entries.entrySet().iterator(); iterator.hasNext();)
                if (iterator.next().getValue().isDone())
                    iterator.remove();
                
            return removeIfEmpty();
        }
        
        public boolean finishInitial() {
            if (LittleTiles.CONFIG.rendering.uploadToVBODirectly)
                appendRenderData(true);
            else
                markReadyForUpdate();
            
            return false;
        }
        
        public boolean notifyReceiveClientUpdate(BETiles be) {
            // keep blocks which are received while rendering. They will be added separately and done be removed
            BlockAdditional data = entries.get(be.getBlockPos());
            if (data != null)
                data.receiveUpdate(be);
            return false;
        }
        
        /** For important to be called otherwise it will remain bound to the render section **/
        public boolean removeIfEmpty() {
            if (entries.isEmpty()) {
                section.setAdditional(null);
                return true;
            }
            return false;
        }
    }
    
    private class BlockAdditional extends AdditionalBuffers {
        
        private boolean done;
        
        public void queueNew(UUID uuid, RenderingLevelHandler origin, Level originLevel, BETiles be, SectionPos pos) {
            IBlockBufferCache cache = be.render.buffers();
            Vec3 vec = RenderingLevelHandler.offsetCorrection(target, targetLevel, origin, originLevel, pos);
            int sectionIndex = target.sectionIndex(targetLevel, pos.asLong());
            LayeredBufferCache layers = new LayeredBufferCache();
            for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
                BufferCache holder = cache.getIncludingAdditional(layer);
                if (holder == null || !holder.isAvailable())
                    continue;
                if (vec != null)
                    holder.applyOffset(vec, sectionIndex);
                layers.put(layer, holder);
            }
            additional(uuid, layers);
            
            var target = BlockTile.loadBE(targetLevel, be.getBlockPos());
            if (target != null)
                target.render.additionalBuffers(x -> x.additional(this, () -> done = true));
        }
        
        public boolean isDone() {
            return done;
        }
        
        public void receiveUpdate(BETiles be) {
            be.render.additionalBuffers(x -> x.additional(this, () -> done = true));
        }
        
    }
    
}
