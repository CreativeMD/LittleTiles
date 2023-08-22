package team.creative.littletiles.mixin.rubidium;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

@Mixin(ChunkModelBuilder.class)
public interface ChunkModelBuilderMixin extends ChunkBufferUploader {
    
    @Override
    public default int uploadIndex() {
        return uploadIndex(ModelQuadFacing.UNASSIGNED.ordinal());
    }
    
    @Override
    public default void upload(ByteBuffer buffer) {
        upload(ModelQuadFacing.UNASSIGNED.ordinal(), buffer);
    }
    
    @Override
    public default boolean hasFacingSupport() {
        return true;
    }
    
    @Override
    public default int uploadIndex(int facing) {
        ChunkMeshBufferBuilderAccessor vertex = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) this).getVertexBuffer(ModelQuadFacing.VALUES[facing]);
        return vertex.getCount() * vertex.getStride();
    }
    
    @Override
    public default void upload(int facing, ByteBuffer buffer) {
        ChunkMeshBufferBuilderAccessor vertex = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) this).getVertexBuffer(ModelQuadFacing.VALUES[facing]);
        
        // Add to vertex buffer
        int vertexStart = vertex.getCount();
        int vertexCount = buffer.limit() / vertex.getStride();
        if (vertexStart + vertexCount >= vertex.getCapacity())
            vertex.callGrow(vertex.getStride() * vertexCount);
        ByteBuffer data = vertex.getBuffer();
        int index = vertex.getCount() * vertex.getStride();
        data.position(index);
        data.put(buffer);
        buffer.rewind();
        data.rewind();
        vertex.setCount(vertex.getCount() + vertexCount);
    }
    
    @Override
    public default void addSprite(TextureAtlasSprite texture) {
        ((ChunkModelBuilder) this).addSprite(texture);
    }
    
}
