package team.creative.littletiles.client.render.level;

import java.lang.reflect.Field;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.client.render.model.BufferBuilderUtils;
import team.creative.littletiles.client.render.cache.ChunkBlockLayerCache;
import team.creative.littletiles.client.render.cache.ChunkBlockLayerManager;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.common.block.entity.BETiles;

public class LittleChunkDispatcher {
    
    private static final Field hasBlocks = ObfuscationReflectionHelper.findField(CompiledChunk.class, "f_112749_");
    private static final Field hasLayer = ObfuscationReflectionHelper.findField(CompiledChunk.class, "f_112750_");
    private static final Field isCompletelyEmpty = ObfuscationReflectionHelper.findField(CompiledChunk.class, "f_112751_");
    
    private static final Field littleTiles = ObfuscationReflectionHelper.findField(RenderChunk.class, "littleTiles");
    private static final Field dynamicLightUpdate = ObfuscationReflectionHelper.findField(RenderChunk.class, "dynamicLightUpdate");
    public static final Field added = ObfuscationReflectionHelper.findField(BufferBuilder.class, "littleTilesAdded");
    
    public static int currentRenderState = Integer.MIN_VALUE;
    
    public static void onReloadRenderers(LevelRenderer levelRenderer) {
        if (mc.levelRenderer == levelRenderer)
            currentRenderState++;
    }
    
    public static void onOptifineMarksChunkRenderUpdateForDynamicLights(RenderChunk chunk) {
        try {
            dynamicLightUpdate.setBoolean(chunk, true);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    private static Minecraft mc = Minecraft.getInstance();
    
    public static void addTileEntity(List<BETiles> tiles, BlockEntity te) {
        if (te instanceof BETiles)
            tiles.add((BETiles) te);
    }
    
    public static void onDoneRendering(RenderChunk chunk, List<BETiles> tiles) {
        try {
            littleTiles.set(chunk, tiles);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }
    
    // New hook ChunkRenderDispatcher:553 for rebuilding chunk
    public static void beforeUploadRebuild(final ChunkBufferBuilderPack bufferPack, final CompiledChunk compiled, final RenderChunk chunk) {
        try {
            List<BETiles> blocks = (List<BETiles>) littleTiles.get(chunk);
            
            if (blocks.isEmpty()) {
                dynamicLightUpdate.setBoolean(chunk, false);
                return;
            }
            
            boolean dynamicUpdate = dynamicLightUpdate.getBoolean(chunk);
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                VertexBuffer vertexBuffer = chunk.getBuffer(layer);
                ChunkBlockLayerManager oldManager = (ChunkBlockLayerManager) ChunkBlockLayerManager.blockLayerManager.get(vertexBuffer);
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
                    if (compiled.isEmpty(layer) && compiled != CompiledChunk.UNCOMPILED) {
                        ((Set<RenderType>) hasBlocks.get(compiled)).add(layer);
                        ((Set<RenderType>) hasLayer.get(compiled)).add(layer);
                        isCompletelyEmpty.setBoolean(compiled, false);
                    }
                    
                    BufferBuilder buffer = bufferPack.builder(layer);
                    
                    BufferBuilderUtils.growBufferSmall(buffer, cache.expanded() + buffer.getVertexFormat().getVertexSize());
                    cache.fillBuilder(buffer);
                    
                    if (layer == RenderType.translucent() && buffer.getVertexFormat() != null && mc.getCameraEntity() != null) { // Not sure if that is even necessary
                        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
                        buffer.setQuadSortOrigin((float) camera.x - chunk.getOrigin().getX(), (float) camera.y - chunk.getOrigin().getY(), (float) camera.z - chunk.getOrigin()
                                .getZ());
                    }
                    
                    BufferBuilderUtils.getBuffer(buffer).position(0);
                    BufferBuilderUtils.getBuffer(buffer).limit(buffer.getVertexFormat().getVertexSize() * BufferBuilderUtils.getVertexCount(buffer));
                    added.setBoolean(buffer, true);
                    
                    if (layer != RenderType.translucent() && vertexBuffer != null) {
                        ChunkBlockLayerManager manager = (ChunkBlockLayerManager) ChunkBlockLayerManager.blockLayerManager.get(vertexBuffer);
                        if (manager == null)
                            manager = new ChunkBlockLayerManager(chunk, layer);
                        
                        manager.set(cache);
                    }
                    LittleTilesProfilerOverlay.uploaded++;
                    
                }
                
                littleTiles.set(chunk, null); // Clear LTTiles cache
            }
            
            dynamicLightUpdate.setBoolean(chunk, false);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
}
