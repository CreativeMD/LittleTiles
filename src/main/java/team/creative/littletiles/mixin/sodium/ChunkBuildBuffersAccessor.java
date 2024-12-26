package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;

@Mixin(ChunkBuildBuffers.class)
public interface ChunkBuildBuffersAccessor {
    
    @Accessor(remap = false)
    public ChunkVertexType getVertexType();
    
    @Accessor(remap = false)
    public int getTranslucentOffset();
}
