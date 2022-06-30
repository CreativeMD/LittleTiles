package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.VertexBuffer;

import team.creative.littletiles.client.render.cache.ChunkBlockLayerManager;
import team.creative.littletiles.client.render.mc.VertexBufferLittle;

@Mixin(VertexBuffer.class)
public class VertexBufferMixin implements VertexBufferLittle {
    
    @Unique
    public ChunkBlockLayerManager manager;
    
    @Override
    public ChunkBlockLayerManager getManager() {
        return manager;
    }
    
    @Override
    public void setManager(ChunkBlockLayerManager manager) {
        this.manager = manager;
    }
    
}
