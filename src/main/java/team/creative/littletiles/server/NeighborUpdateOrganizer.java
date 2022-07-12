package team.creative.littletiles.server;

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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.creativecore.common.util.type.map.HashMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.packet.update.NeighborUpdate;

public class NeighborUpdateOrganizer {
    
    private HashMapList<Level, BlockPos> positions = new HashMapList<>();
    
    public NeighborUpdateOrganizer() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void add(Level level, BlockPos pos) {
        if (level instanceof IOrientatedLevel)
            return;
        if (!positions.contains(level, pos))
            positions.add(level, pos);
    }
    
    @SubscribeEvent
    public void tick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
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
                    
                } else if (level instanceof ISubLevel)
                    LittleTiles.NETWORK.sendToClientTracking(new NeighborUpdate(level, entry.getValue()), ((ISubLevel) level).getHolder());
            }
            
            positions.clear();
        }
    }
    
    private static int checkerboardDistance(ChunkPos p_140339_, ServerPlayer p_140340_, boolean p_140341_) {
        int i;
        int j;
        if (p_140341_) {
            SectionPos sectionpos = p_140340_.getLastSectionPos();
            i = sectionpos.x();
            j = sectionpos.z();
        } else {
            i = SectionPos.blockToSectionCoord(p_140340_.getBlockX());
            j = SectionPos.blockToSectionCoord(p_140340_.getBlockZ());
        }
        
        return checkerboardDistance(p_140339_, i, j);
    }
    
    private static int checkerboardDistance(ChunkPos p_140207_, int p_140208_, int p_140209_) {
        int i = p_140207_.x - p_140208_;
        int j = p_140207_.z - p_140209_;
        return Math.max(Math.abs(i), Math.abs(j));
    }
    
    @SubscribeEvent
    public void unload(LevelEvent.Unload event) {
        positions.removeKey((Level) event.getLevel());
    }
    
}
