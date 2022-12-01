package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin implements RenderChunkExtender {
    
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
    
}
