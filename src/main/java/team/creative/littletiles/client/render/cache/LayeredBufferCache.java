package team.creative.littletiles.client.render.cache;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;

public interface LayeredBufferCache {
    
    public BufferHolder get(RenderType layer);
    
    public int length(RenderType type);
    
}
