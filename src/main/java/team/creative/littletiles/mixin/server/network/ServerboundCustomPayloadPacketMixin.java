package team.creative.littletiles.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {
    
    @ModifyVariable(
            method = "readUnknownPayload(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/DiscardedPayload;",
            require = 1, at = @At("STORE"))
    private static int modifyPayloadLimit(int value) {
        if (value > 32767)
            return 32767;
        return value;
    }
    
}
