package team.creative.littletiles.common.packet.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
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
    
    private void queueStructure(HashMap<RenderChunkExtender, RenderCacheHolder> chunks, LittleStructure structure, LittleAnimationEntity entity) throws LittleActionException {
        HashSet<RenderChunkExtender> backToRAM = new HashSet<>();
        for (BETiles be : structure.blocks()) {
            
            BlockEntity block = entity.getSubLevel().getBlockEntity(be.getBlockPos());
            if (!(block instanceof BETiles))
                continue;
            
            BETiles target = (BETiles) block;
            RenderChunkExtender toRam = be.render.getRenderChunk();
            if (backToRAM.add(toRam)) {
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    VertexBufferExtender buffer = (VertexBufferExtender) toRam.getVertexBuffer(layer);
                    ChunkLayerUploadManager manager = buffer.getManager();
                    if (manager != null)
                        manager.backToRAM();
                }
            }
            
            RenderChunkExtender chunk = target.render.getRenderChunk();
            
            Vec3 offset = chunk.offsetCorrection(toRam);
            RenderCacheHolder holder = chunks.get(chunk);
            if (holder == null)
                chunks.put(chunk, holder = new RenderCacheHolder());
            holder.add(target, be.render.getBufferCache(), structure.getIndex(), offset);
        }
        
        for (StructureChildConnection child : structure.children.all()) {
            if (child.isLinkToAnotherWorld())
                continue;
            try {
                queueStructure(chunks, child.getStructure(), entity);
            } catch (LittleActionException e) {}
        }
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        try {
            requiresClient(player);
            HashMap<RenderChunkExtender, RenderCacheHolder> chunks = new HashMap<>();
            queueStructure(chunks, structure, (LittleAnimationEntity) LittleTilesClient.ANIMATION_HANDLER.find(uuid));
        } catch (LittleActionException | ClassCastException e) {
            e.printStackTrace();
        }
    }
    
    private static class RenderCacheHolder implements LayeredBufferCache {
        
        private final ChunkLayerMap<BufferHolder> holders = new ChunkLayerMap<>();
        
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
        
        public void add(BETiles target, BlockBufferCache cache, int index, Vec3 offset) {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                BufferHolder holder = cache.extract(layer, index);
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
