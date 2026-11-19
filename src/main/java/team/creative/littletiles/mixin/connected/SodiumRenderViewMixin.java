package team.creative.littletiles.mixin.connected;

import java.util.HashMap;
import java.util.Map;
import team.creative.littletiles.client.render.material.FullBlockStateAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice", remap = false)
public abstract class SodiumRenderViewMixin {
    @Unique private final Map<BlockPos, BlockState> yuushyaConnected$states = new HashMap<>();

    @Inject(method = {"copyData", "reset"}, at = @At("HEAD"), require = 2)
    private void yuushyaConnected$reset(CallbackInfo callback) {
        yuushyaConnected$states.clear();
    }

    // Sodium's mesher calls the integer overload directly, before choosing the model.
    @Inject(method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("RETURN"), cancellable = true, require = 1)
    private void yuushyaConnected$materialState(int x, int y, int z, CallbackInfoReturnable<BlockState> callback) {
        BlockState original = callback.getReturnValue();
        if (!FullBlockStateAdapter.eligible(original)) return;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState resolved = yuushyaConnected$states.get(pos);
        if (resolved == null) {
            resolved = FullBlockStateAdapter.resolve((BlockAndTintGetter) this, pos, original);
            yuushyaConnected$states.put(pos, resolved);
        }
        callback.setReturnValue(resolved);
    }
}
