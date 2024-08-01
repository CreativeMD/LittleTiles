package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkBuildBuffers.class)
public interface ChunkBuildBuffersAccessor {
    
    @Accessor(remap = false)
    public ChunkVertexType getVertexType();
}
