package team.creative.littletiles.client.render.cache.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.itr.FunctionNonNullIterator;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.AdditionalBufferReceiver;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class AdditionalBuffers implements AdditionalBufferReceiver {
    
    private final List<AdditionalBuffer> content = new ArrayList<>();
    /** Will be called once when the buffer is used in some way **/
    private Runnable uploadedHook = null;
    private Runnable removedHook = null;
    
    public AdditionalBuffers() {}
    
    public void removed() {
        if (removedHook != null) {
            removedHook.run();
            removedHook = null;
        }
    }
    
    /** done after everything else has compiled. Therefore used is not called */
    public void uploadAdditionalExtra(@Nullable Function<RenderType, ChunkBufferUploader> builderSupplier, Function<RenderType, BufferCollection> bufferSupplier) {
        for (AdditionalBuffer a : content)
            for (int i = 0; i < ChunkLayerMap.LAYERS_COUNT; i++) {
                var buffer = a.buffers().getByIndex(i);
                if (buffer != null) {
                    var layer = ChunkLayerMap.layer(i);
                    LittleRenderPipelineType.upload(builderSupplier != null ? builderSupplier.apply(layer) : null, bufferSupplier.apply(layer), buffer);
                }
            }
    }
    
    public void uploadAdditional(RenderType layer, @Nullable ChunkBufferUploader uploader, BufferCollection collection) {
        if (uploadedHook != null)
            uploadedHook.run();
        for (AdditionalBuffer a : content) {
            var buffer = a.buffers.get(layer);
            if (buffer != null)
                LittleRenderPipelineType.upload(uploader, collection, buffer);
        }
    }
    
    public BufferCache getAdditional(BufferCache original, RenderType layer) {
        return BufferCache.combineOrCopy(original, new FunctionNonNullIterator<BufferCache>(content, x -> x.buffers.get(layer)));
    }
    
    public boolean has(RenderType layer) {
        for (AdditionalBuffer b : content)
            if (b.buffers.containsKey(layer))
                return true;
        return false;
    }
    
    public int indexOf(UUID uuid) {
        for (int i = 0; i < content.size(); i++)
            if (content.get(i).uuid.equals(uuid))
                return i;
        return -1;
    }
    
    @Override
    public synchronized void additional(UUID uuid, LayeredBufferCache cache) {
        int index = indexOf(uuid);
        if (index == -1)
            content.add(new AdditionalBuffer(uuid, cache.copy()));
        else
            content.get(index).add(cache);
    }
    
    @Override
    public void additional(AdditionalBuffers buffers, Runnable removedHook, Runnable uploadedHook) {
        content.addAll(buffers.content);
        this.removedHook = removedHook;
        this.uploadedHook = uploadedHook;
    }
    
    public Iterable<LayeredBufferCache> additionals() {
        return new FunctionNonNullIterator<>(content, x -> x.buffers);
    }
    
    @Override
    public String toString() {
        return content.toString();
    }
    
    private static record AdditionalBuffer(UUID uuid, LayeredBufferCache buffers) {
        
        public void add(LayeredBufferCache cache) {
            for (Tuple<RenderType, BufferCache> tuple : cache.tuples())
                if (buffers.containsKey(tuple.key))
                    buffers.put(tuple.key, BufferCache.combineOrCopy(buffers.get(tuple.key), new SingleIterator<BufferCache>(tuple.value)));
                else
                    buffers.put(tuple.key, tuple.value);
        }
        
        @Override
        public String toString() {
            return uuid + "|" + buffers;
        }
    }
    
}
