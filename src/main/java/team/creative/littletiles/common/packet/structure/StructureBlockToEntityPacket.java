package team.creative.littletiles.common.packet.structure;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
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
    
    private void queueStructure(LittleStructure structure, LittleAnimationEntity entity, HashSet<RenderChunkExtender> backToRAM) throws LittleActionException {
        
        for (BETiles be : structure.blocks()) {
            
            BlockEntity block = entity.getSubLevel().getBlockEntity(be.getBlockPos());
            if (!(block instanceof BETiles))
                continue;
            
            BETiles target = (BETiles) block;
            RenderChunkExtender toRam = be.render.getRenderChunk();
            if (backToRAM.add(toRam))
                toRam.backToRAM();
            
            RenderChunkExtender chunk = target.render.getRenderChunk();
            
            Vec3 offset = chunk.offsetCorrection(toRam);
            
            target.render.additionalBuffers(x -> {
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    BufferCache holder = be.render.buffers().extract(layer, structure.getIndex());
                    if (holder == null)
                        continue;
                    
                    if (offset != null)
                        holder.applyOffset(offset);
                    holder.markAsAdditional();
                    x.additional(layer, holder);
                }
            });
        }
        
        for (StructureChildConnection child : structure.children.all()) {
            if (child.isLinkToAnotherWorld())
                continue;
            try {
                queueStructure(child.getStructure(), entity, backToRAM);
            } catch (LittleActionException e) {}
        }
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        try {
            requiresClient(player);
            HashSet<RenderChunkExtender> backToRAM = new HashSet<>();
            queueStructure(structure, (LittleAnimationEntity) LittleTilesClient.ANIMATION_HANDLER.find(uuid), backToRAM);
        } catch (LittleActionException | ClassCastException e) {
            e.printStackTrace();
        }
    }
    
}
