package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.gl.arena.GlBufferArena;
import me.jellysquid.mods.sodium.client.gl.arena.GlBufferSegment;

@Mixin(GlBufferSegment.class)
public interface GlBufferSegmentAccessor {
    
    @Accessor(remap = false)
    public GlBufferArena getArena();
}
