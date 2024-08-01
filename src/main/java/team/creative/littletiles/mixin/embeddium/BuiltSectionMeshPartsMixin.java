package team.creative.littletiles.mixin.embeddium;

import org.embeddedt.embeddium.impl.render.chunk.data.BuiltSectionMeshParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import team.creative.littletiles.client.mod.embeddium.data.BuiltSectionMeshPartsExtender;
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
