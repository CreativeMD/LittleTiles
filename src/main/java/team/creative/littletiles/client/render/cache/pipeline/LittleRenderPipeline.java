package team.creative.littletiles.client.render.cache.pipeline;

import java.util.Map.Entry;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.build.RenderingBlockContext;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.client.rubidium.LittleRenderPipelineRubidium;
import team.creative.littletiles.common.block.entity.BETiles;

public abstract class LittleRenderPipeline {
    
    public static final Minecraft MC = Minecraft.getInstance();
    
    public abstract void buildCache(PoseStack pose, ChunkLayerMap<BufferHolder> buffers, RenderingBlockContext context, VertexFormat format, SingletonList<BakedQuad> bakedQuadWrapper);
    
    public abstract void reload();
    
    public abstract void release();
    
    public static enum LittleRenderPipelineType {
        
        FORGE(LittleRenderPipelineForge::new) {
            
            @Override
            public void startCompile(RenderChunkExtender chunk) {
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    VertexBuffer vertexBuffer = chunk.getVertexBuffer(layer);
                    ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
                    if (manager != null) {
                        synchronized (manager) {
                            manager.queued++;
                        }
                        manager.backToRAM();
                    } else
                        ((VertexBufferExtender) vertexBuffer).setManager(manager = new ChunkLayerUploadManager(chunk, layer));
                }
            }
            
            @Override
            public void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    VertexBuffer vertexBuffer = chunk.getVertexBuffer(layer);
                    ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
                    synchronized (manager) {
                        manager.queued--;
                    }
                }
                
                ChunkLayerMap<ChunkLayerCache> caches = task.getLayeredCache();
                if (caches != null)
                    for (Entry<RenderType, ChunkLayerCache> entry : caches.tuples()) {
                        VertexBuffer vertexBuffer = chunk.getVertexBuffer(entry.getKey());
                        ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
                        manager.set(entry.getValue());
                    }
                
                task.clear();
            }
            
            @Override
            public void add(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask) {
                be.updateQuadCache(chunk);
                
                for (RenderType layer : RenderType.chunkBufferLayers()) {
                    synchronized (be.render.getBufferCache()) {
                        if (!be.render.getBufferCache().has(layer))
                            continue;
                        
                        be.render.getBufferCache().add(layer, rebuildTask.builder(layer), rebuildTask.getOrCreate(layer));
                    }
                }
            }
        },
        RUBIDIUM(LittleRenderPipelineRubidium::new) {
            @Override
            public void startCompile(RenderChunkExtender chunk) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void add(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask) {
                // TODO Auto-generated method stub
                
            }
        };
        
        public final Supplier<LittleRenderPipeline> supplier;
        
        private LittleRenderPipelineType(Supplier<LittleRenderPipeline> supplier) {
            this.supplier = supplier;
        }
        
        public abstract void startCompile(RenderChunkExtender chunk);
        
        public abstract void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task);
        
        public abstract void add(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask);
        
    }
}
