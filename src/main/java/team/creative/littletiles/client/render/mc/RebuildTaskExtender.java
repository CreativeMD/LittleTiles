package team.creative.littletiles.client.render.mc;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;

public interface RebuildTaskExtender {
    
    public BufferCache upload(RenderType layer, BufferCache cache);
    
}
