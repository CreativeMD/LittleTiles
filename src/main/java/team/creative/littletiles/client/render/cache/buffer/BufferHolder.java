package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.MeshData;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.client.render.VertexFormatUtils;
import team.creative.littletiles.mixin.client.render.ByteBufferBuilderResultAccessor;
import team.creative.littletiles.mixin.client.render.MeshDataAccessor;

public class BufferHolder implements BufferCache {
    
    public static BufferHolder combine(BufferHolder first, BufferHolder second) {
        if (first == null && second == null)
            return null;
        if (first == null)
            return second;
        if (second == null)
            return first;
        
        return (BufferHolder) first.combine(second);
    }
    
    private ByteBuffer buffer;
    private int length;
    private int vertexCount;
    /** format is structure index followed by the end index. To get the start of a structure the index before has to be considered (if it is the first the start will be 0).
     * Example: [-1, 120, 1, 160]
     * Structure -1: 0-120
     * Structure 1: 120-160 */
    private final int[] indexes;
    private int groupCount;
    
    private boolean invalid;
    private int uploadIndex;
    
    public BufferHolder(ByteBuffer buffer, int length, int vertexCount, int[] indexes) {
        this.buffer = buffer;
        this.length = length;
        this.vertexCount = vertexCount;
        this.indexes = indexes;
        this.groupCount = indexes != null ? indexes.length / 2 : 0;
    }
    
    public BufferHolder(MeshData buffer, int[] indexes) {
        this.length = ((ByteBufferBuilderResultAccessor) ((MeshDataAccessor) buffer).getVertexBuffer()).getCapacity();
        this.buffer = ByteBuffer.allocateDirect(length);
        this.buffer.put(buffer.vertexBuffer());
        this.buffer.rewind();
        this.vertexCount = buffer.drawState().vertexCount();
        buffer.close();
        this.indexes = indexes;
        this.groupCount = indexes != null ? indexes.length / 2 : 0;
    }
    
    @Override
    public void eraseBuffer() {
        if (uploadIndex >= 0)
            buffer = null;
    }
    
    @Override
    public boolean upload(ChunkBufferUploader uploader) {
        if (!isAvailable())
            return false;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return false;
        uploadIndex = uploader.uploadIndex();
        uploader.upload(buffer);
        buffer.rewind();
        return true;
    }
    
    public boolean upload(int facing, ChunkBufferUploader uploader) {
        if (!isAvailable())
            return false;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return false;
        uploadIndex = uploader.uploadIndex(facing);
        uploader.upload(facing, buffer);
        buffer.rewind();
        return true;
    }
    
    public int[] indexes() {
        return indexes;
    }
    
    @Override
    public int groupCount() {
        return groupCount;
    }
    
    protected void removeEntry(int length, int vertexCount) {
        this.length -= length;
        this.vertexCount -= vertexCount;
        this.groupCount--;
    }
    
    public ByteBuffer byteBuffer() {
        return buffer;
    }
    
    public ByteBuffer byteBufferOrThrow() {
        if (buffer != null)
            return buffer;
        throw new IllegalArgumentException("No buffer found");
    }
    
    public int length() {
        return length;
    }
    
    @Override
    public int lengthToUpload() {
        if (isAvailable())
            return length;
        return 0;
    }
    
    @Override
    public int lengthToUpload(int facing) {
        if (isAvailable() && facing == ModelQuadFacing.UNASSIGNED.ordinal())
            return length;
        return 0;
    }
    
    public int vertexCount() {
        return vertexCount;
    }
    
    @Override
    public boolean isInvalid() {
        return invalid;
    }
    
    @Override
    public void invalidate() {
        invalid = true;
        eraseBuffer();
    }
    
    @Override
    public boolean isAvailable() {
        return buffer != null && length > 0;
    }
    
    @Override
    public boolean download(ChunkBufferDownloader downloader) {
        return download(downloader.downloaded());
    }
    
    public boolean download(ByteBuffer buffer) {
        if (uploadIndex >= 0 && buffer.capacity() >= uploadIndex + length()) {
            ByteBuffer downloaded = ByteBuffer.allocateDirect(length);
            downloaded.put(0, buffer, uploadIndex, length);
            downloaded.rewind();
            this.buffer = downloaded;
            uploadIndex = -1;
            return true;
        }
        
        invalidate();
        return false;
    }
    
    @Override
    public BufferCache combine(BufferCache cache) {
        if (!(cache instanceof BufferHolder))
            return cache.combine(this);
        
        BufferHolder holder = (BufferHolder) cache;
        
        int vertexCount = 0;
        int length = 0;
        ByteBuffer firstBuffer = byteBuffer();
        if (firstBuffer != null) {
            vertexCount += vertexCount();
            length += length();
        }
        
        ByteBuffer secondBuffer = holder.byteBuffer();
        if (secondBuffer != null) {
            vertexCount += holder.vertexCount();
            length += holder.length();
        }
        
        if (vertexCount == 0)
            return null;
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        
        if (firstBuffer != null) {
            firstBuffer.position(0);
            firstBuffer.limit(length());
            byteBuffer.put(firstBuffer);
            firstBuffer.rewind();
        }
        
        if (secondBuffer != null) {
            secondBuffer.position(0);
            secondBuffer.limit(holder.length());
            byteBuffer.put(secondBuffer);
            secondBuffer.rewind();
        }
        byteBuffer.rewind();
        return new BufferHolder(byteBuffer, length, vertexCount, null);
    }
    
    @Override
    public BufferCache combine(Iterator<BufferCache> itr) {
        int vertexCount = vertexCount();
        int length = length();
        
        List<BufferCache> holders = new ArrayList<>();
        while (itr.hasNext()) {
            BufferHolder n = (BufferHolder) itr.next();
            vertexCount += n.vertexCount();
            length += n.length();
            holders.add(n);
        }
        
        if (vertexCount == 0)
            return null;
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        
        var b = byteBufferOrThrow();
        b.position(0);
        b.limit(length());
        byteBuffer.put(b);
        b.rewind();
        
        for (BufferCache bufferCache : holders) {
            b = ((BufferHolder) bufferCache).byteBufferOrThrow();
            b.position(0);
            b.limit(length());
            byteBuffer.put(b);
            b.rewind();
        }
        
        byteBuffer.rewind();
        return new BufferHolder(byteBuffer, length, vertexCount, null);
    }
    
    @Override
    public BufferCache copy() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        var b = byteBufferOrThrow();
        b.position(0);
        b.limit(length());
        byteBuffer.put(b);
        b.rewind();
        byteBuffer.rewind();
        return new BufferHolder(byteBuffer, length, vertexCount, null);
    }
    
    @Override
    public BufferHolder extract(int index) {
        int[] indexes = indexes(); // format of one entry: [index of structure, start index of vertex data]
        if (indexes == null)
            return null;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return null;
        
        if (indexes.length == 2 && indexes[0] == index)
            return new BufferHolder(buffer, length(), vertexCount(), indexes.clone());
        
        int start = -1;
        int length = -1;
        int entryIndex = -1;
        for (int i = 0; i < indexes.length; i += 2) {
            if (indexes[i] == index) {
                start = i == 0 ? 0 : indexes[i - 1];
                entryIndex = i;
                length = indexes[i + 1] - start;
                break;
            }
        }
        if (start == -1)
            return null;
        if (length == 0)
            return null;
        
        int div = length() / vertexCount();
        int vertexCount = length / div;
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(length); // Create new vertex buffer
        newBuffer.put(0, buffer, start, length);
        newBuffer.rewind();
        removeEntry(length, vertexCount); // Notify buffer holder of changed length and vertexCount
        indexes[entryIndex + 1] = start;
        
        if (entryIndex < indexes.length - 2) { // Remove extracted data from buffer holder
            buffer.put(start, buffer, start + length, buffer.limit() - (start + length));
            for (int i = entryIndex + 2; i < indexes.length; i += 2)
                indexes[i + 1] -= length;
        }
        buffer.limit(buffer.limit() - length);
        return new BufferHolder(newBuffer, length, vertexCount, null);
    }
    
    @Override
    public void applyOffset(Vec3 vec) {
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
    
    public void moveUploadIndex(int offset) {
        uploadIndex += offset;
    }
}