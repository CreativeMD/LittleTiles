package team.creative.littletiles.common.packet.level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.server.level.little.LittleChunkMap;
import team.creative.littletiles.server.level.little.LittleServerChunkCache;

public class LittleLevelInitPacket extends CreativePacket {
    
    public UUID uuid;
    public StructureAbsolute absolute;
    public List<ClientboundLevelChunkWithLightPacket> chunks;
    
    public LittleLevelInitPacket() {}
    
    public LittleLevelInitPacket(LittleLevelEntity entity) {
        this.uuid = entity.getUUID();
        this.absolute = entity.getCenter();
        LittleServerChunkCache cache = (LittleServerChunkCache) entity.getSubLevel().getChunkSource();
        chunks = new ArrayList<>(cache.chunkMap.size());
        for (LevelChunk chunk : cache.all())
            chunks.add(((LittleChunkMap) cache.chunkMap).createPacket(chunk));
    }
    
    @Override
    public void executeClient(Player player) {
        LittleLevelEntity entity = LittleAnimationHandlers.find(player.level.isClientSide, uuid);
        if (entity == null)
            return;
        
        entity.initSubLevelClient(absolute);
        
        ClientGamePacketListener listener = (ClientGamePacketListener) ((LittleLevel) entity.getSubLevel()).getPacketListener();
        for (ClientboundLevelChunkWithLightPacket packet : chunks)
            packet.handle(listener);
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
