package team.creative.littletiles.mixin.rubidium;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;

@Mixin(ChunkVertexBufferBuilder.class)
public interface ChunkVertexBufferBuilderAccessor {
    
    @Accessor(remap = false)
    public int getStride();
    
    @Accessor(remap = false)
    public ByteBuffer getBuffer();
    
    @Accessor(remap = false)
    public int getCount();
    
}
