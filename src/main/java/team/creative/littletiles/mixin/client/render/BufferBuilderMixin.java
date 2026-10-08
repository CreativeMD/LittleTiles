package team.creative.littletiles.mixin.client.render;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.iris.IrisManager;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements ChunkBufferUploader {
    
    @Final
    @Shadow
    private ByteBufferBuilder buffer;
    
    @Final
    @Shadow
    private VertexFormat format;
    
    @Shadow
    private int vertices;
    
    @Shadow
    private long vertexPointer = -1L;
    
    @Shadow
    private int vertexSize;
    
    @Override
    public int uploadIndex() {
        return ((ByteBufferBuilderAccessor) buffer).getWriteOffset();
    }
    
    @Override
    public void upload(ByteBuffer buffer) {
        if (!tryUpload(buffer, buffer.remaining() / vertexSize))
            LittleTiles.LOGGER.warn("Skipping cached vertex data incompatible with the target vertex stride");
    }

    @Override
    public boolean tryUpload(ByteBuffer buffer, int vertexCount) {
        int length = buffer.remaining();
        if (vertexCount < 0 || length != (long) vertexCount * vertexSize)
            return false;
        if (length == 0)
            return true;

        this.ensureBuilding();
        this.endLastVertex();

        if (IrisManager.uploadCachedVertices((BufferBuilder) (Object) this, buffer, format))
            return true;

        this.vertices += length / vertexSize;
        this.vertexPointer = this.buffer.reserve(length);
        long address = MemoryUtil.memAddress(buffer);
        MemoryUtil.memCopy(address, vertexPointer, length);
        return true;
    }
    
    @Shadow
    public abstract void ensureBuilding();
    
    @Shadow
    public abstract void endLastVertex();
    
    @Override
    public boolean hasFacingSupport() {
        return false;
    }
    
    @Override
    public int uploadIndex(int facing) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void upload(int facing, ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addTexture(TextureAtlasSprite texture) {}
    
}
