package team.creative.littletiles.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import team.creative.littletiles.server.level.little.LittleServerEntity;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    
    @Redirect(method = "//", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    public void sendRedirect(ServerGamePacketListenerImpl connection, Packet packet) {
        if ((ServerEntity) (Object) this instanceof LittleServerEntity server)
            server.sendPacket(connection.player, packet);
        else
            connection.send(packet);
    }
    
}
