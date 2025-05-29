package team.creative.littletiles.client.render.cache;

import java.util.UUID;

import team.creative.littletiles.client.render.cache.buffer.AdditionalBuffers;

public interface AdditionalBufferReceiver {
    
    public void additional(UUID uuid, LayeredBufferCache cache);
    
    public default void additional(AdditionalBuffers buffers) {
        additional(buffers, null);
    }
    
    public void additional(AdditionalBuffers buffers, Runnable hook);
    
}
