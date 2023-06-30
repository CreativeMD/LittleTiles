package team.creative.littletiles.mixin.common.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin {
    
    @Redirect(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "NEW", target = "Lnet/minecraft/nbt/NbtAccounter;"), require = 1)
    public NbtAccounter createNbtAccounter(long value) {
        return NbtAccounter.UNLIMITED;
    }
    
}
