package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin implements RenderChunkExtender {
    
    @Unique
    private RenderChunk as() {
        return (RenderChunk) (Object) this;
    }
    
    @Unique
    public boolean dynamicLightUpdate = false;
    
    @Override
    public boolean dynamicLightUpdate() {
        return dynamicLightUpdate;
    }
    
    @Override
    public void dynamicLightUpdate(boolean value) {
        dynamicLightUpdate = value;
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
    public Vec3i standardOffset() {
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
    public void setQuadSortOrigin(BufferBuilder builder, Vec3 camera) {
        BlockPos origin = as().getOrigin();
        builder.setQuadSortOrigin((float) camera.x - origin.getX(), (float) camera.y - origin.getY(), (float) camera.z - origin.getZ());
    }
    
}
