package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.CompiledSection;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;
import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

@Mixin(RenderSection.class)
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
    private RenderSection as() {
        return (RenderSection) (Object) this;
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
    @Invoker("getBuffer")
    public abstract VertexBuffer getVertexBuffer(RenderType layer);
    
    @Override
    @Invoker("setDirty")
    public abstract void markReadyForUpdate(boolean playerChanged);
    
    @Override
    public SortState getTransparencyState() {
        return ((CompiledSectionAccessor) as().getCompiled()).getTransparencyState();
    }
    
    @Override
    public void setTransparencyState(SortState state) {
        ((CompiledSectionAccessor) as().getCompiled()).setTransparencyState(state);
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        CompiledSection compiled = as().getCompiled();
        if (compiled != CompiledSection.UNCOMPILED)
            ((CompiledSectionAccessor) compiled).getHasBlocks().add(layer);
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return as().getCompiled().isEmpty(layer);
    }
    
    @Override
    public VertexSorting createVertexSorting(double x, double y, double z) {
        BlockPos origin = as().getOrigin();
        return VertexSorting.byDistance((float) x - origin.getX(), (float) y - origin.getY(), (float) z - origin.getZ());
    }
    
}
