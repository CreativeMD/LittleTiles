package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.render.chunk.vertex.format.impl.CompactChunkVertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CompactChunkVertex.class)
public interface CompactChunkVertexAccessor {
    
    @Invoker
    public static short callEncodePosition(float value) {
        throw new UnsupportedOperationException();
    }
    
}
