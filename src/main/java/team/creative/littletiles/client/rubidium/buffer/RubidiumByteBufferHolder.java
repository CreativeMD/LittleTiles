package team.creative.littletiles.client.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.ByteBufferHolder;
import team.creative.littletiles.mixin.rubidium.ChunkVertexBufferBuilderAccessor;

public class RubidiumByteBufferHolder extends ByteBufferHolder implements RubidiumBufferHolder {
    
    private IntArrayList[] facingBuffers;
    private List<TextureAtlasSprite> textures;
    
    public RubidiumByteBufferHolder(ChunkVertexBufferBuilder builder, ByteBuffer buffer, int[] indexes, IntArrayList[] facingBuffers, List<TextureAtlasSprite> textures) {
        super(buffer, buffer.limit(), ((ChunkVertexBufferBuilderAccessor) builder).getCount(), indexes);
        this.facingBuffers = facingBuffers;
        this.textures = textures;
    }
    
    @Override
    public IntArrayList[] facingIndexLists() {
        return facingBuffers;
    }
    
    @Override
    public IntArrayList facingIndexList(ModelQuadFacing facing) {
        return facingBuffers[facing.ordinal()];
    }
    
    @Override
    public List<TextureAtlasSprite> getUsedTextures() {
        return textures;
    }
    
}
