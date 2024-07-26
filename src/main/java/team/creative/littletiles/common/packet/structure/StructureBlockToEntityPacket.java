package team.creative.littletiles.common.packet.structure;

import java.util.UUID;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.build.RenderingLevelHandler;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;

public class StructureBlockToEntityPacket extends StructurePacket {
    
    public UUID uuid;
    
    public StructureBlockToEntityPacket() {}
    
    public StructureBlockToEntityPacket(StructureLocation location, LittleAnimationEntity entity) {
        super(location);
        this.uuid = entity.getUUID();
    }
    
    private void queueStructure(Long2ObjectMap<RenderCacheHolder> chunks, RenderingLevelHandler targetLevel, RenderingLevelHandler origin, LittleStructure structure,
            LittleAnimationEntity entity) throws LittleActionException {
        for (BETiles be : structure.blocks()) {
            
            BlockEntity block = entity.getSubLevel().getBlockEntity(be.getBlockPos());
            if (!(block instanceof BETiles))
                continue;
            
            BETiles target = (BETiles) block;
            
            var pos = SectionPos.asLong(be.getBlockPos());
            RenderCacheHolder holder = chunks.get(pos);
            if (holder == null) {
                origin.getRenderChunk(be.getLevel(), pos).backToRAM();
                chunks.put(pos, holder = new RenderCacheHolder(SectionPos.of(be.getBlockPos())));
            }
            
            holder.add(target, be.render.getBufferCache(), structure.getIndex(), RenderingLevelHandler.offsetCorrection(targetLevel, origin, holder.pos));
        }
        
        for (StructureChildConnection child : structure.children.all()) {
            if (child.isLinkToAnotherWorld())
                continue;
            try {
                queueStructure(chunks, targetLevel, origin, child.getStructure(), entity);
            } catch (LittleActionException e) {}
        }
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        try {
            requiresClient(player);
            Long2ObjectMap<RenderCacheHolder> chunks = new Long2ObjectOpenHashMap<>();
            LittleAnimationEntity ani = (LittleAnimationEntity) LittleTilesClient.ANIMATION_HANDLER.find(uuid);
            queueStructure(chunks, RenderingLevelHandler.of(ani.getSubLevel()), RenderingLevelHandler.of(structure.getStructureLevel()), structure, ani);
        } catch (LittleActionException | ClassCastException e) {
            e.printStackTrace();
        }
    }
    
    private static class RenderCacheHolder implements LayeredBufferCache {
        
        public final SectionPos pos;
        private final ChunkLayerMap<BufferCache> holders = new ChunkLayerMap<>();
        
        public RenderCacheHolder(SectionPos pos) {
            this.pos = pos;
        }
        
        @Override
        public BufferCache get(RenderType layer) {
            return holders.get(layer);
        }
        
        public void add(BETiles target, BlockBufferCache cache, int index, Vec3 offset) {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                BufferCache holder = cache.extract(layer, index);
                if (holder == null)
                    continue;
                
                if (offset != null)
                    holder.applyOffset(offset);
                
                target.render.getBufferCache().additional(layer, holder);
                holders.put(layer, BlockBufferCache.combine(holders.get(layer), holder));
            }
        }
    }
    
}
