package team.creative.littletiles.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {
    
    @ModifyVariable(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", require = 1, at = @At("STORE"))
    public int modifyPayloadLimit(int value) {
        if (value > 32767)
            return 32767;
        return value;
    }
    
    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", require = 1,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readBytes(I)Lio/netty/buffer/ByteBuf;"))
    public ByteBuf readBytes(FriendlyByteBuf buf, int size) {
        return buf.readBytes(buf.readableBytes());
    }
    
}
