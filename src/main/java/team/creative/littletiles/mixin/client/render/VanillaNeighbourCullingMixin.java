package team.creative.littletiles.mixin.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.littletiles.client.render.block.VanillaNeighbourCulling;

@Mixin(Block.class)
public class VanillaNeighbourCullingMixin {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void littletiles$hideCoveredVanillaFace(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            BlockPos neighbourPos, CallbackInfoReturnable<Boolean> callback) {
        if (VanillaNeighbourCulling.fullyHidden(state, level, pos, direction, neighbourPos))
            callback.setReturnValue(false);
    }
}
