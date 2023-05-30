package team.creative.littletiles.client.render.level;

import java.util.Map.Entry;

import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.client.render.mc.VertexFormatUtils;
import team.creative.littletiles.common.block.entity.BETiles;

public class LittleChunkDispatcher {
    
    public static int currentRenderState = Integer.MIN_VALUE;
    private static final Minecraft mc = Minecraft.getInstance();
    
    public static void onReloadRenderers(LevelRenderer levelRenderer) {
        if (mc.levelRenderer == levelRenderer)
            currentRenderState++;
        if (LittleTilesClient.ANIMATION_HANDLER != null)
            LittleTilesClient.ANIMATION_HANDLER.allChanged();
        VertexFormatUtils.update();
    }
    
    public static void onOptifineMarksChunkRenderUpdateForDynamicLights(RenderChunkExtender chunk) {
        chunk.dynamicLightUpdate(true);
    }
    
    public static void startCompile(RenderChunkExtender chunk) {
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
    
    public static void endCompile(RenderChunkExtender chunk, RebuildTaskExtender task) {
        chunk.dynamicLightUpdate(false);
        
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
    
    public static void add(RenderChunkExtender chunk, BETiles be, RebuildTaskExtender rebuildTask) {
        if (chunk.dynamicLightUpdate())
            be.render.hasLightChanged = true;
        
        be.updateQuadCache(chunk);
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            synchronized (be.render.getBufferCache()) {
                if (!be.render.getBufferCache().has(layer))
                    continue;
                
                be.render.getBufferCache().add(layer, rebuildTask.builder(layer), rebuildTask.getOrCreate(layer));
            }
        }
    }
}
