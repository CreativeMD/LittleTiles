package team.creative.littletiles.client.mod.sodium.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import team.creative.creativecore.common.util.type.itr.ComputeNextIterator;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.creativecore.common.util.type.list.Tuple;

public class ChunkLayerMapSodium<T> implements Iterable<T> {
    
    private static final int LAYERS_COUNT = DefaultTerrainRenderPasses.ALL.length;
    
    private final T[] content;
    
    public ChunkLayerMapSodium(ChunkLayerMapSodium<T> map) {
        content = Arrays.copyOf(map.content, LAYERS_COUNT);
    }
    
    public ChunkLayerMapSodium(Function<TerrainRenderPass, T> factory) {
        content = (T[]) new Object[LAYERS_COUNT];
        for (int i = 0; i < content.length; i++)
            content[i] = factory.apply(DefaultTerrainRenderPasses.ALL[i]);
    }
    
    public ChunkLayerMapSodium() {
        content = (T[]) new Object[LAYERS_COUNT];
    }
    
    private int index(TerrainRenderPass pass) {
        if (pass == DefaultTerrainRenderPasses.SOLID)
            return 0;
        else if (pass == DefaultTerrainRenderPasses.CUTOUT)
            return 1;
        else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT)
            return 2;
        throw new IllegalArgumentException("" + pass);
    }
    
    public T get(TerrainRenderPass layer) {
        return content[index(layer)];
    }
    
    public T put(TerrainRenderPass layer, T element) {
        int index = index(layer);
        T result = content[index];
        content[index] = element;
        return result;
    }
    
    public T remove(TerrainRenderPass layer) {
        int index = index(layer);
        T result = content[index];
        content[index] = null;
        return result;
    }
    
    public void clear() {
        Arrays.fill(content, null);
    }
    
    public Iterable<Tuple<TerrainRenderPass, T>> tuples() {
        return new ComputeNextIterator<>() {
            
            private int index;
            private final Tuple<TerrainRenderPass, T> pair = new Tuple<>(null, null);
            
            @Override
            protected Tuple<TerrainRenderPass, T> computeNext() {
                while (index < content.length && content[index] == null)
                    index++;
                if (index >= content.length)
                    return end();
                pair.key = DefaultTerrainRenderPasses.ALL[index];
                pair.value = content[index];
                index++;
                return pair;
            }
        };
    }
    
    public boolean containsKey(TerrainRenderPass layer) {
        return get(layer) != null;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new ComputeNextIterator<T>() {
            
            private int index;
            
            @Override
            protected T computeNext() {
                while (index < content.length && content[index] == null)
                    index++;
                if (index >= content.length)
                    return end();
                T result = content[index];
                index++;
                return result;
            }
        };
    }
    
    public int size() {
        int size = 0;
        for (int i = 0; i < content.length; i++)
            if (content[i] != null)
                size++;
        return size;
    }
    
    public boolean isEmpty() {
        for (int i = 0; i < content.length; i++)
            if (content[i] != null)
                return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "[" + String.join(",", () -> new FunctionIterator<>(this, Object::toString)) + "]";
    }
    
    public void putEach(T value) {
        for (int i = 0; i < content.length; i++)
            content[i] = value;
    }
    
}
