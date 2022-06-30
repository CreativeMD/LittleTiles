package team.creative.littletiles.client.render.level;

import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.client.render.model.BufferBuilderUtils;
import team.creative.littletiles.client.render.cache.ChunkBlockLayerCache;
import team.creative.littletiles.client.render.cache.ChunkBlockLayerManager;
import team.creative.littletiles.client.render.mc.RebuildTaskLittle;
import team.creative.littletiles.client.render.mc.RenderChunkLittle;
import team.creative.littletiles.client.render.mc.VertexBufferLittle;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.mixin.CompiledChunkAccessor;

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
            ChunkBlockLayerManager oldManager = ((VertexBufferLittle) vertexBuffer).getManager();
            if (oldManager != null)
                oldManager.backToRAM();
            else
                ((VertexBufferLittle) vertexBuffer).setManager(new ChunkBlockLayerManager(chunk, layer));
        }
        
    }
    
    public static void add(ChunkBufferBuilderPack pack, RenderChunk chunk, BETiles be, Set<RenderType> types) {
        if (((RenderChunkLittle) chunk).dynamicLightUpdate())
            be.render.hasLightChanged = true;
        be.updateQuadCache(chunk);
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            
        }
    }
    
    public static void beforeUploadRebuild(final ChunkBufferBuilderPack bufferPack, final CompiledChunk compiled, final RenderChunk chunk, RebuildTaskLittle result) {
        List<BETiles> blocks = result.getBlocks();
        
        if (blocks.isEmpty()) {
            ((RenderChunkLittle) chunk).dynamicLightUpdate(false);
            return;
        }
        
        boolean dynamicUpdate = ((RenderChunkLittle) chunk).dynamicLightUpdate();
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = chunk.getBuffer(layer);
            ChunkBlockLayerManager oldManager = ((VertexBufferLittle) vertexBuffer).getManager();
            if (oldManager != null)
                oldManager.backToRAM();
            
            ChunkBlockLayerCache cache = new ChunkBlockLayerCache(layer);
            
            for (BETiles be : blocks) {
                if (!be.hasLoaded())
                    continue;
                
                if (layer == RenderType.solid()) {
                    if (dynamicUpdate)
                        be.render.hasLightChanged = true;
                    
                    be.updateQuadCache(chunk);
                }
                
                cache.add(be.render, be.render.getBufferCache().get(layer));
            }
            
            if (cache.expanded() > 0) {
                if (compiled.isEmpty(layer) && compiled != CompiledChunk.UNCOMPILED)
                    ((CompiledChunkAccessor) compiled).getHasBlocks().add(layer);
                
                BufferBuilder buffer = bufferPack.builder(layer);
                
                BufferBuilderUtils.growBufferSmall(buffer, cache.expanded() + buffer.getVertexFormat().getVertexSize());
                cache.fillBuilder(buffer);
                
                if (layer == RenderType.translucent() && buffer.getVertexFormat() != null && mc.getCameraEntity() != null) { // Not sure if that is even necessary
                    Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
                    buffer.setQuadSortOrigin((float) camera.x - chunk.getOrigin().getX(), (float) camera.y - chunk.getOrigin().getY(), (float) camera.z - chunk.getOrigin().getZ());
                }
                
                BufferBuilderUtils.getBuffer(buffer).position(0);
                BufferBuilderUtils.getBuffer(buffer).limit(buffer.getVertexFormat().getVertexSize() * BufferBuilderUtils.getVertexCount(buffer));
                
                if (layer != RenderType.translucent() && vertexBuffer != null) {
                    ChunkBlockLayerManager manager = ((VertexBufferLittle) vertexBuffer).getManager();
                    if (manager == null)
                        manager = new ChunkBlockLayerManager(chunk, layer);
                    
                    manager.set(cache);
                }
                LittleTilesProfilerOverlay.uploaded++;
                
            }
            
            result.setBlocks(null);
        }
        
        ((RenderChunkLittle) chunk).dynamicLightUpdate(false);
    }
    
}
