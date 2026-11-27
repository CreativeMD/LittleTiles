package team.creative.littletiles.mixin.connected;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import team.creative.littletiles.client.mod.continuity.CtmQuadCollector;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/** Prevents Sodium's FRAPI adapter from casting our deliberately minimal collection context. */
@Mixin(value = BakedModel.class, priority = 2000)
public interface BakedModelMixin {
    @Inject(method = "emitBlockQuads", at = @At("HEAD"), cancellable = true, require = 0)
    private void yuushyaLtCtmCompat$emitVanillaModel(BlockAndTintGetter level, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context, CallbackInfo callback) {
        if (CtmQuadCollector.emitVanillaModel((BakedModel) this, state, context))
            callback.cancel();
    }
}
