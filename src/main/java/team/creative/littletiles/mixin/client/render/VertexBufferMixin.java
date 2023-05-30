package team.creative.littletiles.mixin.client.render;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferExtender {
    
    @Unique
    public ChunkLayerUploadManager manager;
    
    @Unique
    public int length;
    
    @Override
    public ChunkLayerUploadManager getManager() {
        return manager;
    }
    
    @Override
    public void setManager(ChunkLayerUploadManager manager) {
        this.manager = manager;
    }
    
    @Inject(at = @At("TAIL"), method = "upload(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V")
    public void upload(CallbackInfo info) {
        if (!((VertexBuffer) (Object) this).isInvalid() && manager != null)
            manager.uploaded();
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"),
            method = "uploadVertexBuffer(Lcom/mojang/blaze3d/vertex/BufferBuilder$DrawState;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/vertex/VertexFormat;", require = 1)
    public void uploadVertexBuffer(BufferBuilder.DrawState drawState, ByteBuffer buffer, CallbackInfoReturnable<VertexFormat> info) {
        length = drawState.vertexBufferSize();
    }
    
    @Override
    public int getLastUploadedLength() {
        return length;
    }
    
    @Override
    @Accessor
    public abstract int getVertexBufferId();
    
}
