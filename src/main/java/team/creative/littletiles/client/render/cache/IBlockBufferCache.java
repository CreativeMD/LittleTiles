package team.creative.littletiles.client.render.cache;

import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public interface IBlockBufferCache {
    
    public boolean has(RenderType layer);
    
    public BufferCache get(RenderType layer);
    
    public void upload(Function<RenderType, ChunkBufferUploader> builderSupplier, Function<RenderType, BufferCollection> bufferSupplier);
    
    public void markUploaded(Function<RenderType, BufferCollection> bufferSupplier);
    
    public BufferCache extract(RenderType layer, int index);
    
}
