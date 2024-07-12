package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.executor.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkBuilder.class)
public interface ChunkBuilderAccessor {
    
    @Accessor(remap = false)
    public ChunkBuildContext getLocalContext();
}
