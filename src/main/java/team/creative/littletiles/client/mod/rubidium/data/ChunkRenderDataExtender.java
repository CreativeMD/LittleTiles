package team.creative.littletiles.client.mod.rubidium.data;

import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;

public interface ChunkRenderDataExtender {
    
    public ChunkLayerMap<ChunkLayerCache> getCaches();
    
    public void setCaches(ChunkLayerMap<ChunkLayerCache> caches);
    
}
