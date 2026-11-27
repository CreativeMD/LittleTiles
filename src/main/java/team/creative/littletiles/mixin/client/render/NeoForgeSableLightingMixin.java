package team.creative.littletiles.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import team.creative.littletiles.client.render.cache.pipeline.LittleSableLighting;
import team.creative.littletiles.common.mod.sable.SableManager;

/** Extend Sable's vanilla directional-shading hooks to NeoForge's lighter. */
@Mixin(value = QuadLighter.class, remap = false)
public abstract class NeoForgeSableLightingMixin {
    @ModifyExpressionValue(method = "computeLightingForQuad([IZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockAndTintGetter;getShade(FFFZ)F"),
            require = 1, allow = 1)
    private float littletiles$dynamicDirectionalShade(float original) {
        return SableManager.INSTALLED && LittleSableLighting.isChunkBuildDynamic() ? 1.0F : original;
    }

    @ModifyVariable(method = "process", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1)
    private VertexConsumer littletiles$dynamicNormals(VertexConsumer consumer) {
        return SableManager.INSTALLED && LittleSableLighting.isChunkBuildDynamic()
                ? LittleSableLighting.wrap(consumer) : consumer;
    }
}
