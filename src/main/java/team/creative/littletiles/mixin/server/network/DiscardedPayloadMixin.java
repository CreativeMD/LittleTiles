package team.creative.littletiles.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.protocol.common.custom.DiscardedPayload;

@Mixin(DiscardedPayload.class)
public class DiscardedPayloadMixin {
    
    @ModifyVariable(method = "codec(Lnet/minecraft/resources/ResourceLocation;I)Lnet/minecraft/network/codec/StreamCodec;", ordinal = 0, at = @At("HEAD"), require = 1)
    private static int codec(int value) {
        return Integer.MAX_VALUE;
    }
}
