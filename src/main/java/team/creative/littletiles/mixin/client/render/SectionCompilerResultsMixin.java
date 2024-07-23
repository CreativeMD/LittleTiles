package team.creative.littletiles.mixin.client.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.mc.SectionCompilerResultsExtender;

@Mixin(SectionCompiler.Results.class)
public class SectionCompilerResultsMixin implements SectionCompilerResultsExtender {
    
    @Unique
    public ChunkLayerMap<BufferCollection> caches;
    
    @Override
    public BufferCollection getOrCreate(RenderType layer) {
        if (caches == null)
            caches = new ChunkLayerMap<>();
        BufferCollection cache = caches.get(layer);
        if (cache == null)
            caches.put(layer, cache = new BufferCollection());
        return cache;
    }
    
    @Override
    public ChunkLayerMap<BufferCollection> getCaches() {
        return caches;
    }
    
    @Override
    public boolean isEmpty() {
        var results = (SectionCompiler.Results) (Object) this;
        return results.renderedLayers.isEmpty() && results.globalBlockEntities.isEmpty() && results.blockEntities.isEmpty();
    }
}
