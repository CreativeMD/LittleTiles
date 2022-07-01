package team.creative.littletiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.mojang.blaze3d.vertex.VertexBuffer;

import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.VertexBufferLittle;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferLittle {
    
    @Unique
    public ChunkLayerUploadManager manager;
    
    @Override
    public ChunkLayerUploadManager getManager() {
        return manager;
    }
    
    @Override
    public void setManager(ChunkLayerUploadManager manager) {
        this.manager = manager;
    }
    
    @Inject(at = @At("HEAD"), method = "bind()V")
    public void bind() {
        manager.bindBuffer();
    }
    
    @Override
    @Accessor
    public abstract int getIndexCount();
    
    @Override
    @Accessor
    public abstract int getVertexBufferId();
    
}
