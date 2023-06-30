package team.creative.littletiles.client.render.cache.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.level.LevelAccesorFake;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.IndexedCollector;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;

@OnlyIn(Dist.CLIENT)
public class RenderingThread extends Thread {
    
    public static volatile int CURRENT_RENDERING_INDEX = Integer.MIN_VALUE;
    private static final String[] fakeLeveldMods = new String[] { "chisel" };
    private static final ChunkLayerMap<BufferHolder> EMPTY_HOLDERS = new ChunkLayerMap<>();
    public static List<RenderingThread> THREADS;
    public static final HashMap<RenderChunkExtender, Integer> CHUNKS = new HashMap<>();
    public static final Minecraft MC = Minecraft.getInstance();
    private static final ConcurrentLinkedQueue<RenderingBlockContext> QUEUE = new ConcurrentLinkedQueue<>();
    
    public static synchronized void initThreads(int count) {
        if (count <= 0)
            throw new IllegalArgumentException("count has to be at least equal or greater than one");
        if (THREADS != null) {
            for (RenderingThread thread : THREADS)
                if (thread != null)
                    thread.interrupt();
                
            while (QUEUE.size() > 0)
                QUEUE.poll().be.render.resetRenderingState();
        }
        THREADS = new ArrayList<>();
        for (int i = 0; i < count; i++)
            THREADS.add(new RenderingThread());
    }
    
    public static synchronized void unload() {
        if (THREADS != null)
            for (RenderingThread thread : THREADS)
                if (thread != null)
                    thread.interrupt();
                
        THREADS = null;
        
        QUEUE.clear();
        CHUNKS.clear();
    }
    
    public static synchronized boolean queue(BETiles be, @Nullable RenderChunkExtender chunk) {
        if (THREADS == null)
            initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
        
        if (chunk == null)
            chunk = be.render.getRenderChunk();
        
        if (chunk == null) {
            System.out.println("Invalid tileentity with no rendering chunk! pos: " + be.getBlockPos() + ", level: " + be.getLevel());
            return false;
        }
        
        if (be.isRenderingEmpty()) {
            int index = be.render.startBuildingCache();
            synchronized (be.render) {
                be.render.boxCache.clear();
                be.render.getBufferCache().setEmpty();
            }
            if (!be.render.finishBuildingCache(index, EMPTY_HOLDERS, CURRENT_RENDERING_INDEX, true))
                return queue(be, chunk);
            return false;
        }
        
        synchronized (CHUNKS) {
            Integer count = CHUNKS.get(chunk);
            if (count == null)
                count = 0;
            RenderingThread.CHUNKS.put(chunk, count + 1);
        }
        QUEUE.add(new RenderingBlockContext(be, chunk));
        return true;
    }
    
    public static int queueSize() {
        return QUEUE.size();
    }
    
    public static synchronized void reload() {
        if (THREADS == null)
            return;
        unload();
    }
    
    static {
        initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
    }
    
    public RenderingThread() {
        start();
    }
    
    private final SingletonList<BakedQuad> bakedQuadWrapper = new SingletonList<BakedQuad>(null);
    private final LevelAccesorFake fakeAccess = new LevelAccesorFake();
    private final ChunkLayerMap<BufferHolder> buffers = new ChunkLayerMap<>();
    public boolean active = true;
    private volatile boolean requiresReload = false;
    private LittleRenderPipeline[] pipelines = new LittleRenderPipeline[LittleRenderPipelineType.typeCount()];
    
    public LittleRenderPipeline get(LittleRenderPipelineType type) {
        LittleRenderPipeline pipeline = pipelines[type.id];
        if (pipeline == null) {
            pipelines[type.id] = pipeline = type.factory.get();
            pipeline.reload();
        }
        return pipeline;
    }
    
    @Override
    public void run() {
        try {
            while (active) {
                if (requiresReload) {
                    requiresReload = false;
                    for (int i = 0; i < pipelines.length; i++)
                        if (pipelines[i] != null)
                            pipelines[i].reload();
                }
                
                LevelAccessor level = MC.level;
                long duration = 0;
                RandomSource rand = RandomSource.create();
                PoseStack posestack = new PoseStack();
                
                if (level != null && !QUEUE.isEmpty()) {
                    RenderingBlockContext data = QUEUE.poll();
                    
                    if (data == null)
                        continue;
                    
                    try {
                        if (LittleTilesProfilerOverlay.isActive())
                            duration = System.nanoTime();
                        
                        data.checkRemoved();
                        
                        data.index = data.be.render.startBuildingCache();
                        BlockPos pos = data.be.getBlockPos();
                        
                        data.checkLoaded();
                        
                        data.beforeBuilding();
                        
                        for (RenderType layer : RenderType.chunkBufferLayers()) {
                            IndexedCollector<LittleRenderBox> cubes = data.be.render.getRenderingBoxes(data, layer);
                            
                            if (cubes == null)
                                continue;
                            
                            for (LittleRenderBox cube : cubes) {
                                if (cube.doesNeedQuadUpdate) {
                                    if (ArrayUtils.contains(fakeLeveldMods, cube.state.getBlock().builtInRegistryHolder().key().location().getNamespace())) {
                                        fakeAccess.set(data.be.getLevel(), pos, cube.state);
                                        level = fakeAccess;
                                    } else
                                        level = data.be.getLevel();
                                    
                                    BlockState modelState = cube.state;
                                    rand.setSeed(modelState.getSeed(pos));
                                    BakedModel blockModel = MC.getBlockRenderer().getBlockModel(modelState);
                                    BlockPos offset = cube.getOffset();
                                    for (int h = 0; h < Facing.VALUES.length; h++) {
                                        Facing facing = Facing.VALUES[h];
                                        if (cube.shouldRenderFace(facing)) {
                                            if (cube.getQuad(facing) == null)
                                                cube.setQuad(facing, cube.getBakedQuad(level, pos, offset, modelState, blockModel, facing, layer, rand, true, ColorUtils.WHITE));
                                        } else
                                            cube.setQuad(facing, null);
                                    }
                                    cube.doesNeedQuadUpdate = false;
                                }
                            }
                        }
                        
                        data.clearQuadBuilding();
                        fakeAccess.set(null, null, null);
                        data.checkRemoved();
                        level = MC.level;
                        
                        int renderState = CURRENT_RENDERING_INDEX;
                        VertexFormat format = DefaultVertexFormat.BLOCK;
                        try {
                            posestack.setIdentity();
                            get(data.chunk.getPipeline()).buildCache(posestack, buffers, data, format, bakedQuadWrapper);
                            
                            if (!LittleTiles.CONFIG.rendering.useCubeCache)
                                data.be.render.boxCache.clear();
                            
                            if (!finish(data, buffers, renderState, false))
                                QUEUE.add(data);
                            
                            buffers.clear();
                            
                            if (LittleTilesProfilerOverlay.isActive())
                                LittleTilesProfilerOverlay.finishBuildingCache(System.nanoTime() - duration);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (!finish(data, EMPTY_HOLDERS, -1, false))
                                QUEUE.add(data);
                        }
                    } catch (RemovedBlockEntityException e) {
                        finish(data, EMPTY_HOLDERS, -1, true);
                    } catch (Exception e) {
                        if (!(e instanceof RenderingException))
                            e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        QUEUE.add(data);
                        error.printStackTrace();
                    } finally {
                        buffers.clear();
                    }
                    data = null;
                } else if (level == null || QUEUE.isEmpty())
                    sleep(1);
                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();
            }
        } catch (InterruptedException e) {} finally {
            for (int i = 0; i < pipelines.length; i++)
                if (pipelines[i] != null)
                    pipelines[i].release();
        }
    }
    
    public static boolean finish(RenderingBlockContext data, ChunkLayerMap<BufferHolder> buffers, int renderState, boolean force) {
        if (!data.be.render.finishBuildingCache(data.index, buffers, renderState, force))
            return false;
        
        boolean complete = false;
        
        synchronized (CHUNKS) {
            Integer count = CHUNKS.get(data.chunk);
            if (count != null)
                if (count <= 1) {
                    CHUNKS.remove(data.chunk);
                    complete = true;
                } else
                    CHUNKS.put(data.chunk, count - 1);
        }
        
        if (complete) {
            LittleTilesProfilerOverlay.chunkUpdates++;
            data.chunk.markReadyForUpdate(false);
        }
        return true;
        
    }
    
    public static class RemovedBlockEntityException extends Exception {
        
        public RemovedBlockEntityException(String arg0) {
            super(arg0);
        }
    }
    
    public static class RenderingException extends Exception {
        
        public RenderingException(String arg0) {
            super(arg0);
        }
    }
}