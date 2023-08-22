package team.creative.littletiles.client.render.cache;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;

public interface LayeredBufferCache {
    
    public BufferCache get(RenderType layer);
    
    public default int length(RenderType type) {
        BufferCache cache = get(type);
        if (cache != null)
            return cache.lengthToUpload();
        return 0;
    }
    
    public default int length(RenderType type, int facing) {
        BufferCache cache = get(type);
        if (cache != null)
            return cache.lengthToUpload(facing);
        return 0;
    }
    
}
