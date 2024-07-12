package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.impl.gl.arena.GlBufferSegment;
import org.embeddedt.embeddium.impl.render.chunk.data.SectionRenderDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionRenderDataStorage.class)
public interface SectionRenderDataStorageAccessor {
    
    @Accessor(remap = false)
    public GlBufferSegment[] getAllocations();
}
