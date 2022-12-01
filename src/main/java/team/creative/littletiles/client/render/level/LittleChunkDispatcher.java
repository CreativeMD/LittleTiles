package team.creative.littletiles.client.render.level;

import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;

public class LittleChunkDispatcher {
    
    public static int currentRenderState = Integer.MIN_VALUE;
    private static final Minecraft mc = Minecraft.getInstance();
    
    public static void onReloadRenderers(LevelRenderer levelRenderer) {
        if (mc.levelRenderer == levelRenderer)
            currentRenderState++;
        LittleTilesClient.ANIMATION_HANDLER.allChanged();
    }
    
    public static void onOptifineMarksChunkRenderUpdateForDynamicLights(RenderChunk chunk) {
        ((RenderChunkExtender) chunk).dynamicLightUpdate(true);
    }
    
    public static void startCompile(RenderChunk chunk) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = chunk.getBuffer(layer);
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
    
    public static void add(RenderChunk chunk, BETiles be, RebuildTaskExtender rebuildTask) {
        if (((RenderChunkExtender) chunk).dynamicLightUpdate())
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
