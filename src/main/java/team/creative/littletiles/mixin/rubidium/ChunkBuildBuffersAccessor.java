package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;

@Mixin(ChunkBuildBuffers.class)
public interface ChunkBuildBuffersAccessor {
    
    @Accessor(remap = false)
    public ChunkVertexType getVertexType();
}
