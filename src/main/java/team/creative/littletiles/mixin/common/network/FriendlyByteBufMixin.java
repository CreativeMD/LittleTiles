package team.creative.littletiles.mixin.common.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin {
    
    @Redirect(method = "readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/NbtAccounter;create(J)Lnet/minecraft/nbt/NbtAccounter;"), require = 1)
    private static NbtAccounter createNbtAccounter(long value) {
        return NbtAccounter.unlimitedHeap();
    }
    
}
