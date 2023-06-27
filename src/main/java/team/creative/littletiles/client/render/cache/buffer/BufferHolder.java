package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mojang.blaze3d.platform.MemoryTracker;

import net.minecraft.world.phys.Vec3;
import team.creative.littletiles.client.render.mc.VertexFormatUtils;

public interface BufferHolder {
    
    public ByteBuffer byteBuffer();
    
    public int[] indexes();
    
    public int indexCount();
    
    public void removeEntry(int length, int vertexCount);
    
    public int length();
    
    public int vertexCount();
    
    public default BufferHolder extract(int index) {
        int[] indexes = indexes(); // format of one entry: [index of structure, start index of vertex data]
        if (indexes == null)
            return null;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return null;
        
        if (indexes.length == 2 && indexes[0] == index)
            return new ByteBufferHolder(buffer, length(), vertexCount(), null);
        
        int start = -1;
        int length = -1;
        int entryIndex = -1;
        for (int i = 0; i < indexes.length; i += 2) {
            if (indexes[i] == index) {
                start = indexes[i + 1];
                entryIndex = i;
            } else if (start != -1) {
                length = indexes[i + 1] - start;
                break;
            }
        }
        if (start == -1)
            return null;
        if (length == -1)
            length = length() - start;
        if (length == 0)
            return null;
        
        int div = length() / vertexCount();
        int vertexCount = length / div;
        ByteBuffer newBuffer = MemoryTracker.create(length); // Create new vertex buffer
        newBuffer.put(0, buffer, start, length);
        newBuffer.rewind();
        removeEntry(length, vertexCount); // Notify buffer holder of changed length and vertexCount
        
        if (entryIndex < indexes.length - 2) { // Remove extracted data from buffer holder
            buffer.put(start, buffer, start + length, buffer.limit() - (start + length));
            for (int i = entryIndex + 2; i < indexes.length; i += 2)
                indexes[i + 1] -= length;
        }
        buffer.limit(buffer.limit() - length);
        return new ByteBufferHolder(newBuffer, length, vertexCount, null);
    }
    
    public default void applyOffset(Vec3 vec) {
        // Move render data by offset, easy but a bit hacky method to do it
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return;
        int positionOffset = VertexFormatUtils.blockPositionOffset();
        int formatSize = VertexFormatUtils.blockFormatSize();
        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
        int i = 0;
        while (i < buffer.limit()) {
            float x = buffer.getFloat(i + positionOffset);
            buffer.putFloat(i + positionOffset, x + (float) vec.x);
            float y = buffer.getFloat(i + positionOffset + 4);
            buffer.putFloat(i + positionOffset + 4, y + (float) vec.y);
            float z = buffer.getFloat(i + positionOffset + 8);
            buffer.putFloat(i + positionOffset + 8, z + (float) vec.z);
            i += formatSize;
        }
    }
    
}
