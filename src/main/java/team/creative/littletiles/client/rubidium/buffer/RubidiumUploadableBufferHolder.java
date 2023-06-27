package team.creative.littletiles.client.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.UploadableBufferHolder;

public class RubidiumUploadableBufferHolder extends UploadableBufferHolder implements RubidiumBufferHolder {
    
    public static final int INDEXES_PART_SIZE = ModelQuadFacing.COUNT + 2;
    
    private IntArrayList[] facingBuffers;
    private List<TextureAtlasSprite> textures;
    private int[] facingIndex;
    
    public RubidiumUploadableBufferHolder(ByteBuffer buffer, int vertexIndex, int[] facingIndex, int length, int count, int[] indexes, IntArrayList[] facingBuffers, List<TextureAtlasSprite> textures) {
        super(buffer, vertexIndex, length, count, indexes);
        this.facingIndex = facingIndex;
        this.facingBuffers = facingBuffers;
        this.textures = textures;
    }
    
    @Override
    protected void erase() {
        super.erase();
        facingBuffers = null;
    }
    
    public int facingIndexOffset(ModelQuadFacing facing) {
        return facingIndex[facing.ordinal()];
    }
    
    public int facingIndexCount(ModelQuadFacing facing) {
        int[] indexes = indexes();
        int count = 0;
        for (int i = 0; i < indexes.length; i += INDEXES_PART_SIZE)
            count += indexes[i + 2 + facing.ordinal()];
        return count;
    }
    
    public void prepareFacingBufferDownload() {
        facingBuffers = new IntArrayList[ModelQuadFacing.COUNT];
    }
    
    public void downloadFacingBuffer(IntArrayList list, ModelQuadFacing facing) {
        facingBuffers[facing.ordinal()] = list;
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
