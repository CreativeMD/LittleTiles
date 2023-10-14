package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin implements RenderChunkExtender {
    
    public ChunkLayerMap<BufferCollection> lastUploaded;
    
    private volatile int queued;
    
    @Override
    public ChunkLayerMap<BufferCollection> getLastUploaded() {
        return lastUploaded;
    }
    
    @Override
    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded) {
        this.lastUploaded = uploaded;
    }
    
    @Unique
    private RenderChunk as() {
        return (RenderChunk) (Object) this;
    }
    
    @Override
    public int getQueued() {
        return queued;
    }
    
    @Override
    public void setQueued(int queued) {
        this.queued = queued;
    }
    
    @Override
    @Invoker("beginLayer")
    public abstract void begin(BufferBuilder builder);
    
    @Override
    @Invoker("getBuffer")
    public abstract VertexBuffer getVertexBuffer(RenderType layer);
    
    @Override
    @Invoker("setDirty")
    public abstract void markReadyForUpdate(boolean playerChanged);
    
    @Override
    public SortState getTransparencyState() {
        return ((CompiledChunkAccessor) as().getCompiledChunk()).getTransparencyState();
    }
    
    @Override
    public BlockPos standardOffset() {
        return as().getOrigin();
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        CompiledChunk compiled = as().getCompiledChunk();
        if (compiled != CompiledChunk.UNCOMPILED)
            ((CompiledChunkAccessor) compiled).getHasBlocks().add(layer);
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return as().getCompiledChunk().isEmpty(layer);
    }
    
    @Override
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z) {
        BlockPos origin = as().getOrigin();
        builder.setQuadSorting(VertexSorting.byDistance((float) x - origin.getX(), (float) y - origin.getY(), (float) z - origin.getZ()));
    }
    
    @Override
    public LittleRenderPipelineType getPipeline() {
        return LittleRenderPipelineType.FORGE;
    }
    
}
