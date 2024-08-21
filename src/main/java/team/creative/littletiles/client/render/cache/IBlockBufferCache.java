package team.creative.littletiles.client.render.cache;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;

public interface IBlockBufferCache {
    
    public boolean has(RenderType layer);
    
    public BufferCache get(RenderType layer);
    
    public BufferCache extract(RenderType layer, int index);
    
    public void setUploaded(RenderType layer, BufferCache upload);
    
}
