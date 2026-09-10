package team.creative.littletiles.client.render.cache;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public interface IBlockBufferCache {
    
    public boolean has(RenderType layer);
    
    public BufferCache getIncludingAdditional(RenderType layer);
    
    public void upload(@Nullable Function<RenderType, ChunkBufferUploader> builderSupplier, Function<RenderType, BufferCollection> bufferSupplier);
    
    public BufferCache extract(RenderType layer, int toExtract);
    
}
