package team.creative.littletiles.mixin.client.render;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import team.creative.littletiles.client.render.mc.VertexBufferExtender;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferExtender {
    
    @Shadow
    private int vertexBufferId;
    @Nullable
    @Shadow
    private VertexFormat format;
    @Nullable
    @Shadow
    private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
    @Shadow
    private VertexFormat.IndexType indexType;
    @Shadow
    private int indexCount;
    @Shadow
    private VertexFormat.Mode mode;
    @Unique
    public int length;
    
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;glBufferData(ILjava/nio/ByteBuffer;I)V"),
            method = "uploadVertexBuffer(Lcom/mojang/blaze3d/vertex/MeshData$DrawState;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/vertex/VertexFormat;", require = 1)
    public void uploadVertexBuffer(MeshData.DrawState drawState, ByteBuffer buffer, CallbackInfoReturnable<VertexFormat> info) {
        length = drawState.vertexCount() * drawState.format().getVertexSize();
    }
    
    @Override
    public int getLastUploadedLength() {
        return length;
    }
    
    @Override
    public int getVertexBufferId() {
        return vertexBufferId;
    }
    
    @Override
    public void setFormat(VertexFormat format) {
        this.format = format;
    }
    
    @Override
    public int getIndexCount() {
        return indexCount;
    }
    
    @Override
    public void setIndexCount(int count) {
        this.indexCount = count;
    }
    
    @Override
    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }
    
    @Override
    public void setLastUploadedLength(int length) {
        this.length = length;
    }
    
    @Override
    public Mode getMode() {
        return mode;
    }
    
    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    @Override
    public void setSequentialIndices(AutoStorageIndexBuffer indexBuffer) {
        this.sequentialIndices = indexBuffer;
    }
    
    @Override
    public RenderSystem.AutoStorageIndexBuffer getSequentialIndices() {
        return sequentialIndices;
    }
    
}
