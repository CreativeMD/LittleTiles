package team.creative.littletiles.mixin.embeddium;

import java.nio.ByteBuffer;

import org.embeddedt.embeddium.impl.render.chunk.sorting.TranslucentQuadAnalyzer;
import org.embeddedt.embeddium.impl.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMeshBufferBuilder.class)
public interface ChunkMeshBufferBuilderAccessor {
    
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
    
    @Accessor(remap = false)
    public TranslucentQuadAnalyzer getAnalyzer();
    
}
