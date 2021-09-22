package team.creative.littletiles.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.world.SubWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.SubLevel;
import team.creative.creativecore.common.util.type.HashMapList;
import team.creative.littletiles.common.packet.update.LittleNeighborUpdatePacket;

public class NeighborUpdateOrganizer {
    
    private HashMapList<LevelAccessor, BlockPos> positions = new HashMapList<>();
    
    public NeighborUpdateOrganizer() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void add(LevelAccessor level, BlockPos pos) {
        if (level instanceof IOrientatedLevel)
            return;
        if (!positions.contains(level, pos))
            positions.add(level, pos);
    }
    
    @SubscribeEvent
    public void tick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
            for (Entry<LevelAccessor, ArrayList<BlockPos>> entry : positions.entrySet()) {
                LevelAccessor level = entry.getKey();
                if (level instanceof ServerLevel) {
                    HashMapList<ChunkPos, BlockPos> chunks = new HashMapList<>();
                    for (BlockPos pos : entry.getValue())
                        chunks.add(new ChunkPos(pos), pos);
                    
                    for (Player player : level.players()) {
                        List<BlockPos> collected = new ArrayList<>();
                        for (Entry<ChunkPos, ArrayList<BlockPos>> chunk : chunks.entrySet()) {
                            if (((ServerLevel) level).getPlayerChunkMap().isPlayerWatchingChunk((ServerPlayer) player, chunk.getKey().x, chunk.getKey().z))
                                collected.addAll(chunk.getValue());
                        }
                        
                        if (!collected.isEmpty())
                            PacketHandler.sendPacketToPlayer(new LittleNeighborUpdatePacket(world, collected), (EntityPlayerMP) player);
                    }
                    
                } else if (level instanceof SubLevel)
                    PacketHandler.sendPacketToTrackingPlayers(new LittleNeighborUpdatePacket(level, entry.getValue()), ((SubWorld) world).parent, (WorldServer) ((SubWorld) world)
                            .getRealWorld(), null);
            }
            
            positions.clear();
        }
    }
    
    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        positions.removeKey(event.getWorld());
    }
    
}
