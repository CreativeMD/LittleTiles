package team.creative.littletiles.server.level.little;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class LittleServerEntity extends ServerEntity {
    
    public final BiConsumer<ServerPlayer, Packet<?>> singleBroadcast;
    
    public LittleServerEntity(ServerLevel level, Entity entity, int updateInterval, boolean trackDelta, Consumer<Packet<?>> broadcast, BiConsumer<ServerPlayer, Packet<?>> singleBroadcast) {
        super(level, entity, updateInterval, trackDelta, broadcast);
        this.singleBroadcast = singleBroadcast;
    }
    
    public void sendPacket(ServerPlayer player, Packet packet) {
        singleBroadcast.accept(player, packet);
    }
}
