package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.gl.arena.GlBufferArena;
import org.embeddedt.embeddium.impl.gl.arena.GlBufferSegment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlBufferSegment.class)
public interface GlBufferSegmentAccessor {
    
    @Accessor(remap = false)
    public GlBufferArena getArena();
}
