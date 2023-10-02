package team.creative.littletiles.mixin.client.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import team.creative.littletiles.client.LittleTilesClient;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;getType()Lnet/minecraft/world/entity/EntityType;"),
            method = "handleAddEntity(Lnet/minecraft/network/protocol/game/ClientboundAddEntityPacket;)V", cancellable = true)
    public void handleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo info) {
        Entity entity = LittleTilesClient.ANIMATION_HANDLER.pollEntityInTransition(packet);
        if (entity != null) {
            Minecraft mc = Minecraft.getInstance();
            mc.level.addEntity(entity);
            ((ClientPacketListenerAccessor) mc.getConnection()).callPostAddEntitySoundInstance(entity);
            info.cancel();
        }
    }
    
}
