package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.ByteBufferHolder;

public class RubidiumByteBufferHolder extends ByteBufferHolder implements RubidiumBufferHolder {
    
    private IntArrayList[] facingBuffers;
    private List<TextureAtlasSprite> textures;
    
    public RubidiumByteBufferHolder(ByteBuffer buffer, int length, int vertexCount, int[] indexes, IntArrayList[] facingBuffers, List<TextureAtlasSprite> textures) {
        super(buffer, length, vertexCount, indexes);
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
