package team.creative.littletiles.common.packet.level;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.LittleLevel;

public class LittleLevelPackets extends CreativePacket {
    
    public UUID uuid;
    public List<? extends Packet> packets;
    
    public LittleLevelPackets() {}
    
    public LittleLevelPackets(LittleLevel level, List<? extends Packet> packets) {
        this.uuid = level.key();
        this.packets = packets;
    }
    
    public LittleLevelPackets(LittleLevel level, Packet... packets) {
        this.uuid = level.key();
        this.packets = Arrays.asList(packets);
    }
    
    @Override
    public void execute(Player player) {
        LittleLevelEntity entity = LittleAnimationHandlers.find(player.level.isClientSide, uuid);
        if (entity == null)
            return;
        
        PacketListener listener = ((LittleLevel) entity.getSubLevel()).getPacketListener(player);
        for (Packet packet : packets)
            packet.handle(listener);
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
