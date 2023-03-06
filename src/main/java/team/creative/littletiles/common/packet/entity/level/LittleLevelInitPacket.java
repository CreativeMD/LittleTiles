package team.creative.littletiles.common.packet.entity.level;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
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
        
        LittleTilesClient.PLAYER_CONNECTION.runInContext(entity.getSubLevel(), x -> {
            for (ClientboundLevelChunkWithLightPacket packet : chunks)
                packet.handle(x);
        });
        
    }
    
}
