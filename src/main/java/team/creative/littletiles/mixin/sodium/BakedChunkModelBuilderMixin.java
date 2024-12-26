package team.creative.littletiles.mixin.sodium;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.mod.sodium.buffer.SodiumBufferUploader;

@Mixin(BakedChunkModelBuilder.class)
public class BakedChunkModelBuilderMixin implements SodiumBufferUploader {
    
    @Unique
    private TranslucentGeometryCollector collector;
    
    @Override
    public int uploadIndex() {
        return uploadIndex(ModelQuadFacing.UNASSIGNED.ordinal());
    }
    
    @Override
    public void upload(ByteBuffer buffer) {
        upload(ModelQuadFacing.UNASSIGNED.ordinal(), buffer);
    }
    
    @Override
    public boolean hasFacingSupport() {
        return true;
    }
    
    @Override
    public int uploadIndex(int facing) {
        ChunkMeshBufferBuilderAccessor vertex = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) this).getVertexBuffer(ModelQuadFacing.VALUES[facing]);
        return vertex.getVertexCount() * vertex.getStride();
    }
    
    @Override
    public void upload(int facing, ByteBuffer buffer) {
        ChunkMeshBufferBuilderAccessor vertex = (ChunkMeshBufferBuilderAccessor) ((ChunkModelBuilder) this).getVertexBuffer(ModelQuadFacing.VALUES[facing]);
        
        // Add to vertex buffer
        int vertexStart = vertex.getVertexCount();
        int vertexCount = buffer.limit() / vertex.getStride();
        if (vertexStart + vertexCount >= vertex.getVertexCapacity())
            vertex.callGrow(vertexCount);
        ByteBuffer data = vertex.getBuffer();
        int index = vertex.getVertexCount() * vertex.getStride();
        long address = MemoryUtil.memAddress(data, index);
        long insertAddress = MemoryUtil.memAddress(buffer);
        MemoryUtil.memCopy(insertAddress, address, buffer.capacity());
        vertex.setVertexCount(vertex.getVertexCount() + vertexCount);
    }
    
    @Override
    public void addTexture(TextureAtlasSprite texture) {
        ((ChunkModelBuilder) this).addSprite(texture);
    }
    
    @Override
    public boolean isSorted() {
        return collector != null;
    }
    
    @Override
    public TranslucentGeometryCollector getTranslucentCollector() {
        return collector;
    }
    
    @Override
    public void setTranslucentCollector(TranslucentGeometryCollector collector) {
        this.collector = collector;
    }
    
}
