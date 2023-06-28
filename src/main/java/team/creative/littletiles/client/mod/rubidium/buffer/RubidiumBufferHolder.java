package team.creative.littletiles.client.mod.rubidium.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.MemoryTracker;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;

public interface RubidiumBufferHolder extends BufferHolder {
    
    public IntArrayList[] facingIndexLists();
    
    public IntArrayList facingIndexList(ModelQuadFacing facing);
    
    public List<TextureAtlasSprite> getUsedTextures();
    
    @Override
    public default RubidiumBufferHolder extract(int index) {
        int[] indexes = indexes(); // format of one entry: [index of structure, start index of vertex data, <start index of each facingBuffer>]
        if (indexes == null)
            return null;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return null;
        
        if (indexes.length == RubidiumUploadableBufferHolder.INDEXES_PART_SIZE && indexes[0] == index)
            return new RubidiumByteBufferHolder(buffer, length(), vertexCount(), null, facingIndexLists(), getUsedTextures());
        
        int[] start = new int[ModelQuadFacing.COUNT + 1];
        Arrays.fill(start, -1);
        int[] length = new int[ModelQuadFacing.COUNT + 1];
        Arrays.fill(length, -1);
        
        int entryIndex = -1;
        for (int i = 0; i < indexes.length; i += RubidiumUploadableBufferHolder.INDEXES_PART_SIZE) {
            if (indexes[i] == index) {
                for (int j = 0; j < start.length; j++)
                    start[j] = indexes[i + 1 + j];
                entryIndex = i;
            } else if (start[i + 1] != -1) {
                for (int j = 0; j < start.length; j++)
                    length[j] = indexes[i + 1 + j] - start[j];
                break;
            }
        }
        IntArrayList[] facingIndexLists = facingIndexLists();
        if (start[0] == -1)
            return null;
        if (length[0] == -1) {
            length[0] = length() - start[0];
            for (int i = 0; i < facingIndexLists.length; i++)
                length[1 + i] = facingIndexLists[i].size() - start[1 + i];
        }
        if (length[0] == 0)
            return null;
        
        int div = length() / vertexCount();
        int vertexCount = length[0] / div;
        ByteBuffer newBuffer = MemoryTracker.create(length[0]); // Create new vertex buffer
        newBuffer.put(0, buffer, start[0], length[0]);
        newBuffer.rewind();
        removeEntry(length[0], vertexCount); // Notify buffer holder of changed length and vertexCount
        
        IntArrayList[] extracted = new IntArrayList[ModelQuadFacing.COUNT]; // Create new facing buffers
        for (int i = 0; i < extracted.length; i++) {
            IntArrayList facingList = new IntArrayList();
            if (length[i + 1] > 0)
                facingList.addElements(0, facingIndexLists[i].elements(), start[i + 1], length[i + 1]);
            extracted[i] = facingList;
        }
        
        if (entryIndex < indexes.length - RubidiumUploadableBufferHolder.INDEXES_PART_SIZE) { // Remove extracted data from buffer holder
            buffer.put(start[0], buffer, start[0] + length[0], buffer.limit() - (start[0] + length[0]));
            for (int i = 0; i < facingIndexLists.length; i++)
                facingIndexLists[i].removeElements(start[i + 1], start[i + 1] + length[i + 1]);
            
            for (int i = entryIndex + RubidiumUploadableBufferHolder.INDEXES_PART_SIZE; i < indexes.length; i += RubidiumUploadableBufferHolder.INDEXES_PART_SIZE)
                for (int j = 0; j < length.length; j++)
                    indexes[i + 1 + j] -= length[j];
        }
        buffer.limit(buffer.limit() - length[0]);
        return new RubidiumByteBufferHolder(newBuffer, length[0], vertexCount, null, extracted, getUsedTextures()); // textures are ignored, so even if the extracted part does not contain an animated texture it will be shipped anyway
    }
    
}
