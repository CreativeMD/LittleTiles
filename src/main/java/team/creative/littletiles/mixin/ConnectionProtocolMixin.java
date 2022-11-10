package team.creative.littletiles.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import team.creative.littletiles.common.packet.mc.ClientboundAddLevelEntityPacket;
import team.creative.littletiles.common.packet.mc.PacketSetInterface;

@Mixin(ConnectionProtocol.class)
public class ConnectionProtocolMixin {
    
    @Shadow
    private Map<PacketFlow, ?> flows;
    
    @Inject(at = @At("TAIL"), method = "<init>(ILnet/minecraft/network/ConnectionProtocol$ProtocolBuilder;)V")
    public void create(CallbackInfo info) {
        if (((Object) this) == ConnectionProtocol.PLAY) {
            PacketSetInterface client = (PacketSetInterface) flows.get(PacketFlow.CLIENTBOUND);
            client.register(ClientboundAddLevelEntityPacket.class, ClientboundAddLevelEntityPacket::new);
        }
    }
    
}
