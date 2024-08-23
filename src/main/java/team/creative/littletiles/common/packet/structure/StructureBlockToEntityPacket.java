package team.creative.littletiles.common.packet.structure;

import java.util.UUID;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.LittleTilesClient;
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
    
    private void queueStructure(Long2ObjectMap<SectionPos> chunks, RenderingLevelHandler target, Level targetLevel, RenderingLevelHandler origin, Level originLevel,
            LittleStructure structure, LittleAnimationEntity entity) throws LittleActionException {
        for (BETiles be : structure.blocks()) {
            
            BlockEntity block = entity.getSubLevel().getBlockEntity(be.getBlockPos());
            if (!(block instanceof BETiles))
                continue;
            
            BETiles targetBE = (BETiles) block;
            
            var pos = SectionPos.asLong(be.getBlockPos());
            SectionPos section = chunks.get(pos);
            if (section == null) {
                origin.getRenderChunk(be.getLevel(), pos).backToRAM();
                chunks.put(pos, section = SectionPos.of(be.getBlockPos()));
            }
            
            Vec3 offset = RenderingLevelHandler.offsetCorrection(target, targetLevel, origin, originLevel, section);
            
            targetBE.render.additionalBuffers(x -> {
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
                queueStructure(chunks, target, targetLevel, origin, originLevel, child.getStructure(), entity);
            } catch (LittleActionException e) {}
        }
    }
    
    @Override
    public void execute(Player player, LittleStructure structure) {
        try {
            requiresClient(player);
            Long2ObjectMap<SectionPos> chunks = new Long2ObjectOpenHashMap<>();
            LittleAnimationEntity ani = (LittleAnimationEntity) LittleTilesClient.ANIMATION_HANDLER.find(uuid);
            queueStructure(chunks, RenderingLevelHandler.of(ani.getSubLevel()), ani.getSubLevel(), RenderingLevelHandler.of(structure.getStructureLevel()), structure
                    .getStructureLevel(), structure, ani);
        } catch (LittleActionException | ClassCastException e) {
            e.printStackTrace();
        }
    }
    
}
