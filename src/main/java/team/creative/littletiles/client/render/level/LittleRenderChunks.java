package team.creative.littletiles.client.render.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.SectionPos;
import team.creative.creativecore.common.util.type.itr.InverseConsecutiveIterator;
import team.creative.creativecore.common.util.type.itr.InverseListIterator;
import team.creative.creativecore.common.util.type.itr.NestedIterator;

public class LittleRenderChunks implements Iterable<LittleRenderChunk> {
    
    private static final SectionPos ZERO = SectionPos.of(0, 0, 0);
    
    private LinkedList<List<LittleRenderChunk>> rings = new LinkedList<>();
    private SectionPos origin = ZERO;
    private int minDistance = -1;
    private int maxUsedRings = -1;
    private int size = 0;
    
    public LittleRenderChunks() {}
    
    public int getChunkDistance(LittleRenderChunk chunk) {
        return chunk.section.distManhattan(origin);
    }
    
    public void arrangeRings(SectionPos newOrigin, Iterable<LittleRenderChunk> chunks) {
        clearRings();
        this.origin = newOrigin;
        addAll(chunks);
    }
    
    public void addAll(Iterable<LittleRenderChunk> chunks) {
        for (LittleRenderChunk chunk : chunks)
            add(chunk);
    }
    
    public void add(LittleRenderChunk chunk) {
        int distance = getChunkDistance(chunk);
        if (minDistance == -1) {
            minDistance = distance;
            getRing(0, true).add(chunk);
        } else if (minDistance > distance) {
            ensureLowerRings(minDistance - distance);
            minDistance = distance;
            getRing(0, true).add(chunk);
        } else
            getRing(distance - minDistance, true).add(chunk);
        maxUsedRings = Math.max(maxUsedRings, distance - minDistance);
        size++;
    }
    
    private void ensureRings(int size) {
        for (int i = rings.size(); i < size; i++)
            rings.add(new ArrayList<LittleRenderChunk>());
    }
    
    private void ensureLowerRings(int count) {
        for (int i = 0; i < count; i++) {
            if (maxUsedRings < rings.size()) {
                rings.addFirst(rings.getLast());
                rings.removeLast();
                maxUsedRings++;
            } else
                rings.addFirst(new ArrayList<LittleRenderChunk>());
        }
        
    }
    
    private void recountSize() {
        size = 0;
        for (List<LittleRenderChunk> bucket : rings)
            size += bucket.size();
    }
    
    protected List<LittleRenderChunk> getRing(int index, boolean create) {
        if (create) {
            ensureRings(index + 1);
            return rings.get(index);
        }
        
        if (rings.size() <= index)
            throw new IllegalArgumentException("Bucket index '" + index + "' is out of bounds (total " + rings.size() + ")");
        return rings.get(index);
    }
    
    public void remove(LittleRenderChunk chunk) {
        int distance = getChunkDistance(chunk);
        if (rings.size() > distance && getRing(distance, false).remove(chunk))
            size--;
    }
    
    public void removeAll(int bucket, Collection<LittleRenderChunk> chunks) {
        for (LittleRenderChunk chunk : chunks)
            remove(chunk);
    }
    
    public List<LittleRenderChunk> removeRing(int distance) {
        List<LittleRenderChunk> ring = rings.remove(distance);
        recountSize();
        return ring;
    }
    
    public void clear() {
        rings.clear();
    }
    
    public void clearRings() {
        for (List<LittleRenderChunk> ring : rings)
            ring.clear();
        size = 0;
        minDistance = -1;
        maxUsedRings = -1;
    }
    
    public Iterable<? extends Iterable<LittleRenderChunk>> rings() {
        return rings;
    }
    
    public int ringCount() {
        return rings.size();
    }
    
    public int size() {
        return size;
    }
    
    @Override
    public Iterator<LittleRenderChunk> iterator() {
        return new NestedIterator<>(rings);
    }
    
    public Iterator<LittleRenderChunk> inverseIterator() {
        Iterator[] itrs = new Iterator[rings.size()];
        for (int i = 0; i < itrs.length; i++)
            itrs[i] = new InverseListIterator<>(rings.get(i));
        return new InverseConsecutiveIterator<>(itrs);
    }
    
}
