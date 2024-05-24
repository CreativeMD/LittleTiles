package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;

@Mixin(BakedChunkModelBuilder.class)
public interface BakedChunkModelBuilderAccessor {
    
    @Accessor(remap = false)
    public boolean getSplitBySide();
    
}
