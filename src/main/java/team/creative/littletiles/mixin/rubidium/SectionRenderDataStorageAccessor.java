package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import me.jellysquid.mods.sodium.client.gl.arena.GlBufferSegment;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;

@Mixin(SectionRenderDataStorage.class)
public interface SectionRenderDataStorageAccessor {
    
    @Accessor(remap = false)
    public GlBufferSegment[] getAllocations();
}
