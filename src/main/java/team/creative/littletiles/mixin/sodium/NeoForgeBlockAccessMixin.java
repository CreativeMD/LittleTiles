package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.caffeinemc.mods.sodium.neoforge.block.NeoForgeBlockAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(NeoForgeBlockAccess.class)
public class NeoForgeBlockAccessMixin {
    
    @Inject(method = "shouldSkipRender", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void shouldSkipRender(BlockGetter level, BlockState selfState, BlockState otherState, BlockPos selfPos, BlockPos otherPos, Direction facing,
            CallbackInfoReturnable<Boolean> callback) {
        if (otherState.getBlock() instanceof BlockTile)
            callback.setReturnValue(otherState.hidesNeighborFace(level, otherPos, selfState, DirectionUtil.getOpposite(facing)));
    }
    
}
