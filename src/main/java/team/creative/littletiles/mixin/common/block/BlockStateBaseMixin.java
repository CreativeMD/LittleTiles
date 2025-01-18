package team.creative.littletiles.mixin.common.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(BlockStateBase.class)
public class BlockStateBaseMixin {
    
    @Inject(at = @At("HEAD"), method = "blocksMotion()Z", require = 1, cancellable = true)
    public void blocksMotion(CallbackInfoReturnable<Boolean> info) {
        if (((BlockStateBase) (Object) this).getBlock() instanceof BlockTile)
            info.setReturnValue(true);
    }
    
    @Inject(at = @At("HEAD"),
            method = "isFaceSturdy(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/SupportType;)Z",
            require = 1, cancellable = true)
    public void isFaceSturdy(BlockGetter level, BlockPos pos, Direction direction, SupportType support, CallbackInfoReturnable<Boolean> info) {
        if (((BlockStateBase) (Object) this).getBlock() instanceof BlockTile b)
            info.setReturnValue(b.isFaceSturdy(level, pos, direction, support));
    }
    
}
