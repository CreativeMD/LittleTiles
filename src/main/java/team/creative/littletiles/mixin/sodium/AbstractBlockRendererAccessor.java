package team.creative.littletiles.mixin.sodium;

import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.client.mod.sodium.BlockRenderingStateAccess;

@Mixin(BlockRenderer.class)
public abstract class AbstractBlockRendererAccessor extends AbstractBlockRenderContext implements BlockRenderingStateAccess {

    @Shadow
    @Final
    private Vector3f posOffset;

    @Shadow
    private @Nullable ColorProvider<BlockState> colorProvider;

    @Shadow
    @Final
    private ColorProviderRegistry colorProviderRegistry;

    @Shadow
    @Final
    private int[] vertexColors;
    @Unique
    private ColorProvider<FluidState> fluidProvider;

    @Unique
    private int customTint = -1;

    @Override
    public void setupState(BlockPos pos, BlockPos origin, BlockState state, LevelSlice slice, RenderType type) {
        this.state = state;
        this.pos = pos;
        this.randomSeed = state.getSeed(pos);
        this.posOffset.set((float) origin.getX(), (float) origin.getY(), (float) origin.getZ());
        if (state.hasOffsetFunction()) {
            Vec3 modelOffset = state.getOffset(this.level, pos);
            this.posOffset.add((float) modelOffset.x, (float) modelOffset.y, (float) modelOffset.z);
        }

        this.colorProvider = this.colorProviderRegistry.getColorProvider(state.getBlock());
        this.fluidProvider = state.getFluidState().isEmpty() ? null : this.colorProviderRegistry.getColorProvider(state.getFluidState().getType());
        this.prepareCulling(true);
        this.prepareAoInfo(false);
        this.type = type;
    }

    @Override
    public void setCustomTint(int color) {
        this.customTint = color;
    }

    @Inject(remap = false, method = "colorizeQuad", at = @At("HEAD"), cancellable = true)
    private void allowFluidColorizing(MutableQuadViewImpl quad, int colorIndex, CallbackInfo ci) {
        if (fluidProvider != null && colorIndex != -1) {
            ci.cancel();
            ColorProvider<FluidState> colorProvider = this.fluidProvider;
            if (colorProvider != null) {
                int[] vertexColors = this.vertexColors;
                colorProvider.getColors(this.slice, this.pos, this.state.getFluidState(), quad, vertexColors);

                for (int i = 0; i < 4; ++i) {
                    quad.color(i, ColorHelper.multiplyColor(-16777216 | vertexColors[i], quad.color(i)));
                }
            }
        } else if (customTint != -1) {
            ci.cancel();
            for (int i = 0; i < 4; ++i) {
                quad.color(i, customTint);
            }
        }
    }
}
