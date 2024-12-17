package team.creative.littletiles.client.render.cache.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.itr.FunctionNonNullIterator;
import team.creative.littletiles.client.render.cache.AdditionalBufferReceiver;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public class AdditionalBuffers implements AdditionalBufferReceiver {
    
    private final List<AdditionalBuffer> content = new ArrayList<>();
    
    public AdditionalBuffers() {}
    
    public void uploadAdditional(RenderType layer, ChunkBufferUploader uploader, BufferCollection collection) {
        for (AdditionalBuffer a : content) {
            var buffer = a.buffers.get(layer);
            if (buffer != null)
                LittleRenderPipelineType.upload(uploader, collection, buffer);
        }
    }
    
    public void markUploadedAdditional(RenderType layer, BufferCollection collection) {
        for (AdditionalBuffer a : content) {
            var buffer = a.buffers.get(layer);
            if (buffer != null)
                LittleRenderPipelineType.markUploaded(collection, buffer);
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
    
    public boolean contains(UUID uuid) {
        for (int i = 0; i < content.size(); i++)
            if (content.get(i).uuid.equals(uuid))
                return true;
        return false;
    }
    
    @Override
    public synchronized void additional(UUID uuid, LayeredBufferCache cache) {
        if (contains(uuid))
            return;
        
        content.add(new AdditionalBuffer(uuid, cache));
    }
    
    @Override
    public void additional(AdditionalBuffers buffers) {
        content.addAll(buffers.content);
    }
    
    public Iterable<LayeredBufferCache> additionals() {
        return new FunctionNonNullIterator<>(content, x -> x.buffers);
    }
    
    private static record AdditionalBuffer(UUID uuid, LayeredBufferCache buffers) {}
    
}
