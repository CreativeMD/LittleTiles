package team.creative.littletiles.mixin.common.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(BlockStateBase.class)
public class BlockStateBaseMixin {
    
    @Inject(at = @At("HEAD"), method = "blocksMotion()Z", require = 1, cancellable = true)
    public void blocksMotion(CallbackInfoReturnable<Boolean> info) {
        if (((BlockStateBase) (Object) this).getBlock() instanceof BlockTile)
            info.setReturnValue(true);
    }
    
}
