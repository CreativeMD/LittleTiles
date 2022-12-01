package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;

public interface RenderChunkExtender {
    
    public boolean dynamicLightUpdate();
    
    public void dynamicLightUpdate(boolean value);
    
    public void invokeBeginLayer(BufferBuilder builder);
    
}
