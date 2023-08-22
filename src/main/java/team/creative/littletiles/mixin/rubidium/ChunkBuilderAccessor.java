package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;

@Mixin(ChunkBuilder.class)
public interface ChunkBuilderAccessor {
    
    @Accessor(remap = false)
    public ChunkBuildContext getLocalContext();
}
