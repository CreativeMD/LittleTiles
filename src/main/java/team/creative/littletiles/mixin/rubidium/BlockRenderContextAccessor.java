package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.embeddedt.embeddium.impl.world.WorldSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockRenderContext.class)
public interface BlockRenderContextAccessor {
    
    @Accessor(remap = false)
    @Mutable
    public void setWorld(WorldSlice world);
    
}
