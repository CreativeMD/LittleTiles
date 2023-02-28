package team.creative.littletiles.common.packet.entity.level;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.level.little.LittleLevel;
import team.creative.littletiles.common.packet.entity.LittleEntityPacket;
import team.creative.littletiles.server.level.little.LittleChunkMap;
import team.creative.littletiles.server.level.little.LittleServerChunkCache;

public class LittleLevelInitPacket extends LittleEntityPacket<LittleLevelEntity> {
    
    public List<ClientboundLevelChunkWithLightPacket> chunks;
    public CompoundTag extraData;
    
    public LittleLevelInitPacket() {}
    
    public LittleLevelInitPacket(LittleLevelEntity entity) {
        super(entity);
        LittleServerChunkCache cache = (LittleServerChunkCache) entity.getSubLevel().getChunkSource();
        chunks = new ArrayList<>(cache.chunkMap.size());
        for (LevelChunk chunk : cache.all())
            chunks.add(((LittleChunkMap) cache.chunkMap).createPacket(chunk));
        this.extraData = entity.saveExtraClientData();
    }
    
    @Override
    public void execute(Player player, LittleLevelEntity entity) {
        requiresClient(player);
        
        entity.initSubLevelClient(extraData);
        
        ClientGamePacketListener listener = (ClientGamePacketListener) ((LittleLevel) entity.getSubLevel()).getPacketListener(player);
        for (ClientboundLevelChunkWithLightPacket packet : chunks)
            packet.handle(listener);
    }
    
}
