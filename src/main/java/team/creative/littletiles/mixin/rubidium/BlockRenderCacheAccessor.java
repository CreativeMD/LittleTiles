package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.jellysquid.mods.sodium.client.model.quad.blender.ColorBlender;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;

@Mixin(BlockRenderCache.class)
public interface BlockRenderCacheAccessor {
    
    @Invoker(remap = false)
    public static ColorBlender callCreateBiomeColorBlender() {
        throw new AssertionError();
    }
}
