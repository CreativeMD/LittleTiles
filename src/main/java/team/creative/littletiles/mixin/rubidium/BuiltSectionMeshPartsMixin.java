package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import team.creative.littletiles.client.mod.rubidium.data.BuiltSectionMeshPartsExtender;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;

@Mixin(BuiltSectionMeshParts.class)
public class BuiltSectionMeshPartsMixin implements BuiltSectionMeshPartsExtender {
    
    @Unique
    public BufferCollection buffers;
    
    @Override
    public BufferCollection getBuffers() {
        return buffers;
    }
    
    @Override
    public void setBuffers(BufferCollection buffers) {
        this.buffers = buffers;
    }
    
}
