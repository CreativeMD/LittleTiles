package team.creative.littletiles.mixin.rubidium;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexEncoder;

@Mixin(ChunkVertexBufferBuilder.class)
public interface ChunkVertexBufferBuilderAccessor {
    
    @Accessor(remap = false)
    public int getStride();
    
    @Accessor(remap = false)
    public ByteBuffer getBuffer();
    
    @Accessor(remap = false)
    public int getCount();
    
    @Accessor(remap = false)
    public void setCount(int count);
    
    @Accessor(remap = false)
    public int getCapacity();
    
    @Invoker(remap = false)
    public void callGrow(int len);
    
    @Accessor(remap = false)
    public ChunkVertexEncoder getEncoder();
    
}
