package team.creative.littletiles.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

@Mixin(CompiledChunk.class)
public interface CompiledChunkAccessor {
    
    @Accessor
    public BufferBuilder.SortState getTransparencyState();
    
    @Accessor
    public Set<RenderType> getHasBlocks();
    
}
