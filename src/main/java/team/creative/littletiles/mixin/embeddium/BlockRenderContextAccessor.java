package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.embeddedt.embeddium.api.render.chunk.EmbeddiumBlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.BlockAndTintGetter;

@Mixin(BlockRenderContext.class)
public interface BlockRenderContextAccessor {
    
    @Accessor(remap = false)
    @Mutable
    public void setWorld(EmbeddiumBlockAndTintGetter world);
    
    @Accessor(remap = false)
    @Mutable
    public void setLocalSlice(BlockAndTintGetter world);
    
}
