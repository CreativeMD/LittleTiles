package team.creative.littletiles.server.level.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.packet.update.NeighborUpdate;

public class NeighborUpdateOrganizer {
    
    private HashMapList<Level, BlockPos> positions = new HashMapList<>();
    
    public NeighborUpdateOrganizer() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    public void add(Level level, BlockPos pos) {
        if (level instanceof IOrientatedLevel)
            return;
        if (!positions.contains(level, pos))
            positions.add(level, pos);
    }
    
    @SubscribeEvent
    public void tick(ServerTickEvent.Post event) {
        for (Entry<Level, ArrayList<BlockPos>> entry : positions.entrySet()) {
            Level level = entry.getKey();
            if (level instanceof ServerLevel) {
                HashMapList<ChunkPos, BlockPos> chunks = new HashMapList<>();
                for (BlockPos pos : entry.getValue())
                    chunks.add(new ChunkPos(pos), pos);
                
                for (Player player : level.players()) {
                    List<BlockPos> collected = new ArrayList<>();
                    for (Entry<ChunkPos, ArrayList<BlockPos>> chunk : chunks.entrySet())
                        if (checkerboardDistance(chunk.getKey(), (ServerPlayer) player, true) <= player.getServer().getPlayerList().getViewDistance())
                            collected.addAll(chunk.getValue());
                        
                    if (!collected.isEmpty())
                        LittleTiles.NETWORK.sendToClient(new NeighborUpdate(level, collected), (ServerPlayer) player);
                }
                
            } else if (level instanceof ISubLevel sub)
                LittleTiles.NETWORK.sendToClientTracking(new NeighborUpdate(level, entry.getValue()), sub.getHolder());
        }
        
        positions.clear();
    }
    
    private static int checkerboardDistance(ChunkPos pos, ServerPlayer player, boolean p_140341_) {
        int i;
        int j;
        if (p_140341_) {
            SectionPos sectionpos = player.getLastSectionPos();
            i = sectionpos.x();
            j = sectionpos.z();
        } else {
            i = SectionPos.blockToSectionCoord(player.getBlockX());
            j = SectionPos.blockToSectionCoord(player.getBlockZ());
        }
        
        return checkerboardDistance(pos, i, j);
    }
    
    private static int checkerboardDistance(ChunkPos pos, int x, int z) {
        return Math.max(Math.abs(pos.x - x), Math.abs(pos.z - z));
    }
    
    @SubscribeEvent
    public void unload(LevelEvent.Unload event) {
        positions.removeKey((Level) event.getLevel());
    }
    
}
