package team.creative.littletiles.client.render.cache;

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.AdditionalBuffers;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class BlockBufferCache implements IBlockBufferCache {
    
    private ChunkLayerMap<BufferCache> queue = new ChunkLayerMap<>();
    private ChunkLayerMap<BufferCache> uploaded = new ChunkLayerMap<>();
    
    private transient AdditionalBuffers additional = null;
    
    public BlockBufferCache() {}
    
    private BufferCache getUploaded(RenderType layer) {
        BufferCache holder = uploaded.get(layer);
        if (holder != null && holder.isInvalid()) {
            uploaded.remove(layer);
            return null;
        }
        return holder;
    }
    
    private BufferCache getOriginal(RenderType layer) {
        BufferCache queued = queue.get(layer);
        if (queued == null)
            return getUploaded(layer);
        return queued;
    }
    
    @Override
    public BufferCache get(RenderType layer) {
        return getOriginal(layer);
    }
    
    @Override
    public void upload(Function<RenderType, ChunkBufferUploader> builderSupplier, Function<RenderType, BufferCollection> bufferSupplier) {
        for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
            if (!has(layer))
                continue;
            
            var uploader = builderSupplier.apply(layer);
            var collection = bufferSupplier.apply(layer);
            
            var uploadable = getOriginal(layer);
            queue.remove(layer);
            if (uploadable == null)
                uploaded.remove(layer);
            else
                uploaded.put(layer, LittleRenderPipelineType.upload(uploader, collection, uploadable));
            
            if (additional != null && additional.has(layer))
                additional.uploadAdditional(layer, uploader, collection);
        }
    }
    
    @Override
    public void markUploaded(Function<RenderType, BufferCollection> bufferSupplier) {
        for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
            if (!has(layer))
                continue;
            
            var collection = bufferSupplier.apply(layer);
            
            var uploadable = queue.remove(layer);
            if (uploadable == null)
                uploaded.remove(layer);
            else
                uploaded.put(layer, LittleRenderPipelineType.markUploaded(collection, uploadable));
            
            if (additional != null && additional.has(layer))
                additional.markUploadedAdditional(layer, collection);
        }
    }
    
    @Override
    public BufferCache extract(RenderType layer, int index) {
        BufferCache holder = getOriginal(layer);
        if (holder == null)
            return null;
        boolean holderUploaded = uploaded.get(layer) != null;
        BufferCache extracted = holder.extract(index);
        if (holder.groupCount() == 0)
            if (holderUploaded)
                uploaded.remove(layer);
            else
                queue.remove(layer);
        return extracted;
    }
    
    @Override
    public boolean has(RenderType layer) {
        return queue.containsKey(layer) || getUploaded(layer) != null || (additional != null && additional.has(layer));
    }
    
    public synchronized void setEmpty() {
        queue.clear();
        uploaded.clear();
        additional = null;
    }
    
    public boolean hasAdditional() {
        return additional != null;
    }
    
    public synchronized void setBuffers(ChunkLayerMap<BufferCache> buffers) {
        for (RenderType layer : RenderType.CHUNK_BUFFER_LAYERS) {
            BufferCache buffer = buffers.get(layer);
            if (buffer == null)
                uploaded.remove(layer);
            
            if (buffer == null)
                queue.remove(layer);
            else
                queue.put(layer, buffer);
        }
        clearAdditional();
    }
    
    public synchronized void executeAdditional(Consumer<AdditionalBufferReceiver> consumer) {
        if (additional == null)
            additional = new AdditionalBuffers();
        consumer.accept(additional);
    }
    
    public void clearAdditional() {
        additional = null;
    }
    
    public boolean hasInvalidBuffers() {
        for (Entry<RenderType, BufferCache> entry : uploaded.tuples())
            if ((entry.getValue() != null && (entry.getValue().isInvalid() || !entry.getValue().isAvailable())) && !queue.containsKey(entry.getKey()))
                return true;
        return false;
    }
}
