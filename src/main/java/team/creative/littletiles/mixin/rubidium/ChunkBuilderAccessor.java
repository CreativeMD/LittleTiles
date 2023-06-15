package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexType;

@Mixin(ChunkBuilder.class)
public interface ChunkBuilderAccessor {
    
    @Accessor(remap = false)
    public BlockRenderPassManager getRenderPassManager();
    
    @Accessor(remap = false)
    public ChunkVertexType getVertexType();
}
