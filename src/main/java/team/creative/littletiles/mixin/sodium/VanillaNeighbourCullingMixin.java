package team.creative.littletiles.mixin.sodium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.creative.littletiles.client.render.block.VanillaNeighbourCulling;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache", remap = false)
public class VanillaNeighbourCullingMixin {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void littletiles$hideCoveredVanillaFace(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            CallbackInfoReturnable<Boolean> callback) {
        if (VanillaNeighbourCulling.fullyHidden(state, level, pos, direction, pos.relative(direction)))
            callback.setReturnValue(false);
    }
}
