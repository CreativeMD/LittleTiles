package team.creative.littletiles.mixin.connected;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.mojang.logging.LogUtils;
import team.creative.littletiles.client.render.material.ConnectedMaterialState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Restore source geometry before terrain culling or LT clipping chooses visible faces. */
@Mixin(value = SimpleBakedModel.class, remap = false)
public abstract class ColumnModelFaceMixin {
    @Shadow @Final protected List<BakedQuad> unculledFaces;
    @Unique private static final AtomicBoolean yuushyaConnected$capReported = new AtomicBoolean();

    @Inject(method = "getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;)Ljava/util/List;",
        at = @At("RETURN"), cancellable = true, require = 1)
    private void yuushyaConnected$completeModel(BlockState state, Direction side, RandomSource random,
            CallbackInfoReturnable<List<BakedQuad>> callback) {
        if (state == null || side == null || !callback.getReturnValue().isEmpty()
                || !ConnectedMaterialState.supports(state)
                || side.getAxis() != ConnectedMaterialState.positiveDirection(state).getAxis()) return;
        BlockState capped = ConnectedMaterialState.key(state);
        if (capped == state) return; // The fallback model cannot recursively request itself.
        for (BakedQuad quad : unculledFaces)
            if (quad.getDirection() == side) return; // Resource packs may already supply an unculled cap.
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(capped);
        var caps = model.getQuads(capped, side, random);
        if (caps.isEmpty()) return;
        callback.setReturnValue(caps);
        if (yuushyaConnected$capReported.compareAndSet(false, true))
            LogUtils.getLogger().info("Yuushya source model end face restored: state={} face={} quads={}", state, side, caps.size());
    }
}
