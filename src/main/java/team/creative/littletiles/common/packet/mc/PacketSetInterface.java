package team.creative.littletiles.common.packet.mc;

import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public interface PacketSetInterface {
    
    public <P extends Packet> void register(Class<P> clazz, Function<FriendlyByteBuf, P> func);
    
}
