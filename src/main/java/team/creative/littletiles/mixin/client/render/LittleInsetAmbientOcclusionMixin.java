package team.creative.littletiles.mixin.client.render;

import java.util.BitSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.client.render.cache.pipeline.LittleInsetLighting;

@Mixin(targets = "net.minecraft.client.renderer.block.ModelBlockRenderer$AmbientOcclusionFace", remap = false)
public abstract class LittleInsetAmbientOcclusionMixin {
    @Shadow @Final private float[] brightness;
    @Shadow @Final private int[] lightmap;

    @Shadow public abstract void calculate(BlockAndTintGetter level, BlockState state, BlockPos pos,
            Direction direction, float[] shape, BitSet flags, boolean shade);

    @Inject(method = "calculate", at = @At("RETURN"), require = 1)
    private void littletiles$blendInsetLighting(BlockAndTintGetter level, BlockState state, BlockPos pos,
            Direction direction, float[] shape, BitSet flags, boolean shade, CallbackInfo ci) {
        if (!LittleInsetLighting.isActive() || flags.get(0))
            return;
        float coordinate = shape[direction.get3DDataValue()];
        // Sloped quads need per-vertex depths; leave their existing lighting alone.
        if (Math.abs(coordinate - shape[direction.getOpposite().get3DDataValue()]) > 0.0001F)
            return;
        float weight = Math.clamp(direction.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? coordinate : 1.0F - coordinate, 0.0F, 1.0F);
        if (weight <= 0.0001F)
            return;
        float[] inside = brightness.clone();
        int[] insideLight = lightmap.clone();
        // Setting the boundary flag selects the outside layer and prevents recursion.
        flags.set(0);
        try {
            calculate(level, state, pos, direction, shape, flags, shade);
        } finally {
            flags.clear(0);
        }
        LittleInsetLighting.blend(brightness, lightmap, inside, insideLight, weight);
    }
}
