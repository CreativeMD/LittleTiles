package team.creative.littletiles.client.render.cache.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.MeshData;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.client.render.VertexFormatUtils;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
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
        
        return (BufferHolder) first.combine(new SingleIterator<>(second));
    }
    
    private ByteBuffer buffer;
    private int length;
    private int vertexCount;
    /** format is structure index followed by the end index. To get the start of a structure the index before has to be considered (if it is the first the start will be 0).
     * Example: [-1, 120, 1, 160]
     * Structure -1: 0-120
     * Structure 1: 120-160 */
    private int[] indexes;
    
    private boolean invalid;
    private int uploadIndex;
    
    public BufferHolder(ByteBuffer buffer, int length, int vertexCount, int[] indexes) {
        this.buffer = buffer;
        this.length = length;
        this.vertexCount = vertexCount;
        this.indexes = indexes;
    }
    
    public BufferHolder(MeshData buffer, int[] indexes) {
        this.length = ((ByteBufferBuilderResultAccessor) ((MeshDataAccessor) buffer).getVertexBuffer()).getCapacity();
        this.buffer = ByteBuffer.allocateDirect(length);
        this.buffer.put(buffer.vertexBuffer());
        this.buffer.rewind();
        this.vertexCount = buffer.drawState().vertexCount();
        buffer.close();
        this.indexes = indexes;
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
    public boolean isEmpty() {
        return indexes == null || indexes.length == 0;
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
    
    private int indexOf(IntList list, int index) {
        for (int i = 0; i < list.size(); i += 2)
            if (list.getInt(i) == index)
                return i;
        return -1;
    }
    
    private void add(IntList list, int index, int length) {
        int foundIndex = indexOf(list, index);
        if (foundIndex == -1) {
            list.add(index);
            list.add(length);
        } else
            list.set(foundIndex + 1, list.getInt(foundIndex + 1) + length);
    }
    
    @Override
    public BufferCache combine(Iterator<BufferCache> itr) {
        int vertexCount = 0;
        int totalLength = 0;
        
        List<BufferHolder> holders = new ArrayList<>();
        IntList indexes = new IntArrayList();
        boolean self = !isEmpty();
        
        while (self || itr.hasNext()) {
            BufferHolder holder = self ? this : (BufferHolder) itr.next();
            self = false;
            if (holder.isEmpty())
                continue;
            
            vertexCount += holder.vertexCount();
            totalLength += holder.length();
            
            for (int i = 0; i < holder.indexes.length; i += 2) {
                int start = i == 0 ? 0 : holder.indexes[i - 1];
                int length = holder.indexes[i + 1] - start;
                add(indexes, holder.indexes[i], length);
            }
            
            holders.add(holder);
        }
        
        if (vertexCount == 0)
            return null;
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(totalLength);
        int totalIndex = 0;
        int[] bufferIndexes = new int[indexes.size()];
        for (int i = 0; i < indexes.size(); i += 2) {
            int length = indexes.getInt(i + 1);
            bufferIndexes[i] = indexes.getInt(i);
            bufferIndexes[i + 1] = totalIndex; // Set to the start and will be used to save the offset once a buffer has been merged
            totalIndex += length;
            indexes.set(i + 1, totalIndex); // set to the end as it is the default format, will be used for the newly created buffer holder
        }
        
        for (BufferHolder holder : holders) {
            var b = holder.byteBufferOrThrow();
            for (int i = 0; i < holder.indexes.length; i += 2) {
                int start = i == 0 ? 0 : holder.indexes[i - 1];
                int length = holder.indexes[i + 1] - start;
                
                int index = indexOf(indexes, holder.indexes[i]);
                
                byteBuffer.put(bufferIndexes[index + 1], b, start, length);
                bufferIndexes[index + 1] += length;
            }
        }
        return new BufferHolder(byteBuffer, totalLength, vertexCount, indexes.toIntArray());
    }
    
    @Override
    public BufferHolder copy() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        var b = byteBufferOrThrow();
        b.position(0);
        b.limit(length());
        byteBuffer.put(b);
        b.rewind();
        byteBuffer.rewind();
        return new BufferHolder(byteBuffer, length, vertexCount, indexes.clone());
    }
    
    private boolean isIdentitcal(int[] toExtract) {
        if (toExtract.length * 2 != indexes.length)
            return false;
        for (int i = 0; i < toExtract.length; i++)
            if (toExtract[i] != indexes[i * 2])
                return false;
        return true;
    }
    
    @Override
    public BufferHolder extract(int toExtract) {
        int[] indexes = indexes(); // format of one entry: [index of structure, end index of vertex data]
        if (indexes == null)
            return null;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return null;
        
        if (indexes.length == 2 && indexes[0] == toExtract) {
            int length = this.length;
            int vertexCount = this.vertexCount;
            this.buffer = null;
            this.indexes = null;
            this.vertexCount = this.length = 0;
            return new BufferHolder(buffer, length, vertexCount, indexes);
        }
        
        // First the array is filled with index followed by length
        int[] extractedIndexes = new int[2];
        int found = 0;
        int extractedTotalLength = 0;
        
        for (int i = 0; i < indexes.length; i += 2) {
            if (indexes[i] == toExtract) {
                int start = i == 0 ? 0 : indexes[i - 1];
                int length = indexes[i + 1] - start;
                
                extractedIndexes[0] = toExtract;
                
                found++;
                extractedTotalLength += length;
            }
        }
        
        if (extractedTotalLength == 0)
            return null;
        
        // Create extracted and newBuffer and fill in the data
        ByteBuffer extractedBuffer = ByteBuffer.allocateDirect(extractedTotalLength);
        int div = length / vertexCount;
        int extractedVertexCount = extractedTotalLength / div;
        
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(length - extractedTotalLength);
        int[] newIndexes = new int[indexes.length - found * 2];
        int otherIndex = 0;
        int newIndex = 0;
        for (int i = 0; i < indexes.length; i += 2) {
            int start = i == 0 ? 0 : indexes[i - 1];
            int length = indexes[i + 1] - start;
            if (indexes[i] == extractedIndexes[otherIndex]) {
                extractedBuffer.put(extractedBuffer.position(), buffer, start, length);
                extractedBuffer.position(extractedBuffer.position() + length);
                extractedIndexes[otherIndex + 1] = extractedBuffer.position();
                otherIndex++;
            } else {
                newBuffer.put(newBuffer.position(), buffer, start, length);
                newBuffer.position(newBuffer.position() + length);
                newIndexes[newIndex] = indexes[i];
                newIndexes[newIndex + 1] = newBuffer.position();
                newIndex++;
            }
        }
        
        extractedBuffer.rewind();
        newBuffer.rewind();
        
        // Saving the new buffer to this object
        this.buffer = newBuffer;
        this.indexes = newIndexes;
        this.length = newBuffer.capacity();
        this.vertexCount = this.length / div;
        
        // Return the extracted stuff
        return new BufferHolder(extractedBuffer, extractedTotalLength, extractedVertexCount, extractedIndexes);
    }
    
    @Override
    public BufferHolder extract(int[] toExtract) {
        int[] indexes = indexes(); // format of one entry: [index of structure, end index of vertex data]
        if (indexes == null)
            return null;
        ByteBuffer buffer = byteBuffer();
        if (buffer == null)
            return null;
        
        if (isIdentitcal(toExtract)) {
            this.buffer = null;
            this.indexes = null;
            this.vertexCount = this.length = 0;
            return new BufferHolder(buffer, length, vertexCount, indexes.clone());
        }
        
        // First the array is filled with index followed by length
        int[] extractedIndexes = new int[toExtract.length * 2];
        int found = 0;
        int extractedTotalLength = 0;
        
        for (int i = 0; i < indexes.length; i += 2) {
            int index = Arrays.binarySearch(toExtract, indexes[i]);
            if (index != -1) {
                int start = i == 0 ? 0 : indexes[i - 1];
                int length = indexes[i + 1] - start;
                
                extractedIndexes[index * 2] = toExtract[index];
                
                found++;
                extractedTotalLength += length;
            }
        }
        
        if (extractedTotalLength == 0)
            return null;
        
        // Create extracted and newBuffer and fill in the data
        ByteBuffer extractedBuffer = ByteBuffer.allocateDirect(extractedTotalLength);
        int div = length / vertexCount;
        int extractedVertexCount = extractedTotalLength / div;
        
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(length - extractedTotalLength);
        int[] newIndexes = new int[indexes.length - found * 2];
        int otherIndex = 0;
        int newIndex = 0;
        for (int i = 0; i < indexes.length; i += 2) {
            int start = i == 0 ? 0 : indexes[i - 1];
            int length = indexes[i + 1] - start;
            if (indexes[i] == extractedIndexes[otherIndex]) {
                extractedBuffer.put(extractedBuffer.position(), buffer, start, length);
                extractedBuffer.position(extractedBuffer.position() + length);
                extractedIndexes[otherIndex + 1] = extractedBuffer.position();
                otherIndex++;
            } else {
                newBuffer.put(newBuffer.position(), buffer, start, length);
                newBuffer.position(newBuffer.position() + length);
                newIndexes[newIndex] = indexes[i];
                newIndexes[newIndex + 1] = newBuffer.position();
                newIndex++;
            }
        }
        
        extractedBuffer.rewind();
        newBuffer.rewind();
        
        // Saving the new buffer to this object
        this.buffer = newBuffer;
        this.indexes = newIndexes;
        this.length = newBuffer.capacity();
        this.vertexCount = this.length / div;
        
        // Return the extracted stuff
        return new BufferHolder(extractedBuffer, extractedTotalLength, extractedVertexCount, extractedIndexes);
    }
    
    @Override
    public void applyOffset(Vec3 vec, int sectionIndex) {
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
    
    @Override
    public String toString() {
        return "length: " + length + ", indexes: " + Arrays.toString(indexes) + ", uploadIndex: " + uploadIndex;
    }
}