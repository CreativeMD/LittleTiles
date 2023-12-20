package team.creative.littletiles.client.render.cache.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;

public abstract class LittleRenderPipelineType {
    
    private static final List<LittleRenderPipelineType> TYPES = new ArrayList<>();
    public static final LittleRenderPipelineTypeForge FORGE = new LittleRenderPipelineTypeForge();
    
    public static int typeCount() {
        return TYPES.size();
    }
    
    public static LittleRenderPipelineType get(int id) {
        return TYPES.get(id);
    }
    
    public static void startCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
        chunk.startBuilding(task);
    }
    
    public static void compile(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask) {
        be.updateQuadCache(chunk);
        
        BlockBufferCache cache = be.render.getBufferCache();
        synchronized (be.render) {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                if (!cache.has(layer))
                    continue;
                cache.setUploaded(layer, rebuildTask.upload(layer, cache.get(layer)));
            }
        }
    }
    
    public static void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
        chunk.endBuilding(task);
    }
    
    public final Supplier<LittleRenderPipeline> factory;
    public final int id;
    
    protected LittleRenderPipelineType(Supplier<LittleRenderPipeline> factory) {
        this.factory = factory;
        id = TYPES.size();
        TYPES.add(this);
    }
    
    public static class LittleRenderPipelineTypeForge extends LittleRenderPipelineType {
        
        private LittleRenderPipelineTypeForge() {
            super(LittleRenderPipelineForge::new);
        }
        
    }
    
}
