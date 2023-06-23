package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.world.level.BlockAndTintGetter;

@Mixin(BlockRenderContext.class)
public interface BlockRenderContextAccessor {
    
    @Accessor(remap = false)
    @Mutable
    public void setWorld(BlockAndTintGetter world);
    
}
