package team.creative.littletiles.mixin.connected;

import java.util.HashMap;
import java.util.Map;
import team.creative.littletiles.client.render.material.FullBlockStateAdapter;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderChunkRegion.class, remap = false)
public abstract class VanillaRenderViewMixin {
    @Unique private final Map<BlockPos, BlockState> yuushyaConnected$states = new HashMap<>();

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true, require = 1)
    private void yuushyaConnected$materialState(BlockPos pos, CallbackInfoReturnable<BlockState> callback) {
        BlockState original = callback.getReturnValue();
        if (!FullBlockStateAdapter.eligible(original)) return;
        BlockState resolved = yuushyaConnected$states.get(pos);
        if (resolved == null) {
            resolved = FullBlockStateAdapter.resolve((BlockAndTintGetter) this, pos, original);
            yuushyaConnected$states.put(pos.immutable(), resolved);
        }
        callback.setReturnValue(resolved);
    }
}
