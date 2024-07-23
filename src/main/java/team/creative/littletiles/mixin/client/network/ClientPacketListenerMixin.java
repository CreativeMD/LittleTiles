package team.creative.littletiles.mixin.client.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import team.creative.littletiles.client.LittleTilesClient;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    
    @Inject(at = @At("HEAD"), method = "createEntityFromPacket(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V", cancellable = true)
    public void createEntityFromPacket(ClientboundAddEntityPacket packet, CallbackInfoReturnable<Entity> info) {
        Entity entity = LittleTilesClient.ANIMATION_HANDLER.pollEntityInTransition(packet);
        if (entity != null)
            info.setReturnValue(entity);
    }
    
}
