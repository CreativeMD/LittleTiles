package team.creative.littletiles.client.render.level;

import java.util.Set;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.mc.RenderChunkLittle;
import team.creative.littletiles.client.render.mc.VertexBufferLittle;
import team.creative.littletiles.common.block.entity.BETiles;

public class LittleChunkDispatcher {
    
    public static int currentRenderState = Integer.MIN_VALUE;
    private static final Minecraft mc = Minecraft.getInstance();
    
    public static void onReloadRenderers(LevelRenderer levelRenderer) {
        if (mc.levelRenderer == levelRenderer)
            currentRenderState++;
    }
    
    public static void onOptifineMarksChunkRenderUpdateForDynamicLights(RenderChunk chunk) {
        ((RenderChunkLittle) chunk).dynamicLightUpdate(true);
    }
    
    public static void startCompile(RenderChunk chunk) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = chunk.getBuffer(layer);
            ChunkLayerUploadManager manager = ((VertexBufferLittle) vertexBuffer).getManager();
            if (manager != null)
                manager.backToRAM();
            else
                ((VertexBufferLittle) vertexBuffer).setManager(manager = new ChunkLayerUploadManager(chunk, layer));
            
            manager.set(new ChunkLayerCache());
        }
    }
    
    public static void add(ChunkBufferBuilderPack pack, RenderChunk chunk, BETiles be, Set<RenderType> types) {
        if (((RenderChunkLittle) chunk).dynamicLightUpdate())
            be.render.hasLightChanged = true;
        be.updateQuadCache(chunk);
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            if (!be.render.getBufferCache().has(layer))
                continue;
            
            ChunkLayerUploadManager manager = ((VertexBufferLittle) chunk.getBuffer(layer)).getManager();
            BufferBuilder builder = pack.builder(layer);
            if (types.add(layer))
                ((RenderChunkLittle) chunk).beginLayer(builder);
            
            be.render.getBufferCache().add(layer, builder, manager.get());
        }
    }
}
