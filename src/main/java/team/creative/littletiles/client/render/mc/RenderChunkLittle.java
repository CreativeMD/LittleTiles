package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;

public interface RenderChunkLittle {
    
    public boolean dynamicLightUpdate();
    
    public void dynamicLightUpdate(boolean value);
    
    public void beginLayer(BufferBuilder builder);
    
}
