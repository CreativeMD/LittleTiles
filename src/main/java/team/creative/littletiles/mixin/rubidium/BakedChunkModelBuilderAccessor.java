package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.BakedChunkModelBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedChunkModelBuilder.class)
public interface BakedChunkModelBuilderAccessor {
    
    @Accessor(remap = false)
    public boolean getSplitBySide();
    
}
