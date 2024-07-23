package team.creative.littletiles.client.render.mc;

import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;

public interface SectionCompilerResultsExtender {
    
    public BufferCollection getOrCreate(RenderType layer);
    
    public ChunkLayerMap<BufferCollection> getCaches();
    
    public boolean isEmpty();
    
}
