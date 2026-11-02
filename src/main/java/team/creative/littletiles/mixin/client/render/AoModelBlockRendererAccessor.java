package team.creative.littletiles.mixin.client.render;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModelBlockRenderer.class, remap = false)
public interface AoModelBlockRendererAccessor {
    @Accessor("CACHE")
    static ThreadLocal<?> getLightingCache() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
