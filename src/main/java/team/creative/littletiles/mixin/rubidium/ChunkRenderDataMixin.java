package team.creative.littletiles.mixin.rubidium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.mod.rubidium.data.ChunkRenderDataExtender;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;

@Mixin(ChunkRenderData.class)
public class ChunkRenderDataMixin implements ChunkRenderDataExtender {
    
    @Unique
    public ChunkLayerMap<ChunkLayerCache> caches;
    
    @Override
    public void setCaches(ChunkLayerMap<ChunkLayerCache> caches) {
        this.caches = caches;
    }
    
    @Override
    public ChunkLayerMap<ChunkLayerCache> getCaches() {
        return caches;
    }
    
}
