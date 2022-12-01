package team.creative.littletiles.client.render.mc;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;

public interface RenderChunkExtender {
    
    public boolean dynamicLightUpdate();
    
    public void dynamicLightUpdate(boolean value);
    
    public void begin(BufferBuilder builder);
    
    public VertexBuffer getVertexBuffer(RenderType layer);
    
    public void markReadyForUpdate(boolean playerChanged);
    
}
