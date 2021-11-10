package com.creativemd.littletiles.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.packet.LittleNeighborUpdatePacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class NeighborUpdateOrganizer {
    
    private HashMapList<World, BlockPos> positions = new HashMapList<>();
    
    public NeighborUpdateOrganizer() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void add(World world, BlockPos pos) {
        if (world instanceof IOrientatedWorld)
            return;
        if (!positions.contains(world, pos))
            positions.add(world, pos);
    }
    
    @SubscribeEvent
    public void tick(ServerTickEvent event) {
        if (event.phase == Phase.END) {
            for (Entry<World, ArrayList<BlockPos>> entry : positions.entrySet()) {
                World world = entry.getKey();
                if (world instanceof WorldServer) {
                    HashMapList<ChunkPos, BlockPos> chunks = new HashMapList<>();
                    for (BlockPos pos : entry.getValue())
                        chunks.add(new ChunkPos(pos), pos);
                    
                    for (EntityPlayer player : world.playerEntities) {
                        List<BlockPos> collected = new ArrayList<>();
                        for (Entry<ChunkPos, ArrayList<BlockPos>> chunk : chunks.entrySet()) {
                            if (((WorldServer) world).getPlayerChunkMap().isPlayerWatchingChunk((EntityPlayerMP) player, chunk.getKey().x, chunk.getKey().z))
                                collected.addAll(chunk.getValue());
                        }
                        
                        if (!collected.isEmpty())
                            PacketHandler.sendPacketToPlayer(new LittleNeighborUpdatePacket(world, collected), (EntityPlayerMP) player);
                    }
                    
                } else if (world instanceof SubWorld)
                    PacketHandler.sendPacketToTrackingPlayers(new LittleNeighborUpdatePacket(world, entry.getValue()), ((SubWorld) world).parent, null);
            }
            
            positions.clear();
        }
    }
    
    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        positions.removeKey(event.getWorld());
    }
    
}
