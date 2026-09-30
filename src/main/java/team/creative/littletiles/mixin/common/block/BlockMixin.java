package team.creative.littletiles.mixin.common.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.api.common.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockProvider;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(Block.class)
public class BlockMixin implements LittleBlockProvider {
    
    @Unique
    private boolean specialBlock;
    
    @Unique
    private LittleBlock block;
    
    private void checkCache() {
        if (block == null)
            LittleBlockRegistry.calculateCache((Block) (Object) this);
    }
    
    @Override
    public LittleBlock getLittleBlock() {
        checkCache();
        return block;
    }
    
    @Override
    public boolean isSpecialBlock() {
        checkCache();
        return specialBlock;
    }
    
    @Override
    public void setCache(LittleBlock block, boolean special) {
        this.block = block;
        this.specialBlock = special;
    }
    
    @Inject(method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;skipRendering(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"),
            require = 1, cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void shouldRenderFace(BlockState otherState, BlockGetter level, BlockPos otherPos, Direction direction, BlockPos pos, CallbackInfoReturnable<Boolean> info,
            BlockState blockstate) {
        if (blockstate.getBlock() instanceof BlockTile b)
            info.setReturnValue(!b.hidesNeighborFace(level, pos, blockstate, otherState, direction));
    }
}
