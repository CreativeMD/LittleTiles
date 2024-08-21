package team.creative.littletiles.client.render.cache;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;

public interface AdditionalBufferReceiver {
    
    public void additional(RenderType layer, BufferCache holder);
    
    public void additional(LayeredBufferCache cache);
}
