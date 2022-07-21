package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import team.creative.littletiles.client.render.mc.RenderChunkLittle;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin implements RenderChunkLittle {
    
    @Unique
    public int updateQueue;
    
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
    @Shadow
    public void beginLayer(BufferBuilder builder) {}
    
}
