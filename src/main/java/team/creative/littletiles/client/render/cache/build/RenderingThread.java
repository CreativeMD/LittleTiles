package team.creative.littletiles.client.render.cache.build;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.common.level.LevelAccessorFake;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.creativecore.common.util.type.map.ChunkLayerMapList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.buffer.BufferCache;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipeline;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;

@OnlyIn(Dist.CLIENT)
public class RenderingThread extends Thread {
    
    public static volatile int CURRENT_RENDERING_INDEX = Integer.MIN_VALUE;
    private static final ChunkLayerMap<BufferCache> EMPTY_HOLDERS = new ChunkLayerMap<>();
    public static List<RenderingThread> THREADS;
    public static final Minecraft MC = Minecraft.getInstance();
    public static final RenderingBlockQueue QUEUE = new RenderingBlockQueue();
    
    public static synchronized void initThreads(int count) {
        if (count <= 0)
            throw new IllegalArgumentException("count has to be at least equal or greater than one");
        if (THREADS != null) {
            for (RenderingThread thread : THREADS)
                if (thread != null)
                    thread.interrupt();
                
            while (!QUEUE.isEmpty())
                QUEUE.poll().be.render.resetRenderingState();
            
            QUEUE.clear(); // Make sure all levels and sections are reset
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
    }
    
    public static synchronized boolean queue(BETiles be, boolean hasPos, long pos) {
        if (THREADS == null)
            initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
        
        if (be.isRenderingEmpty()) {
            int index = be.render.startBuildingCache();
            synchronized (be.render) {
                be.render.eraseBoxCache();
                be.render.setBuffersEmpty();
            }
            if (!be.render.finishBuildingCache(index, EMPTY_HOLDERS, CURRENT_RENDERING_INDEX, true))
                return queue(be, hasPos, pos);
            return false;
        }
        
        QUEUE.queue(be, hasPos, pos);
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
    
    public static <T extends LittleRenderPipeline> T getOrCreate(LittleRenderPipelineType<T> type) {
        for (RenderingThread thread : THREADS)
            if (thread.pipelines[type.id] != null)
                return (T) thread.pipelines[type.id];
        return THREADS.get(0).get(type);
    }
    
    static {
        initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
    }
    
    public RenderingThread() {
        start();
    }
    
    private final SingletonList<BakedQuad> bakedQuadWrapper = new SingletonList<BakedQuad>(null);
    private final LevelAccessorFake fakeAccess = new LevelAccessorFake();
    private final ChunkLayerMap<BufferCache> buffers = new ChunkLayerMap<>();
    public boolean active = true;
    private volatile boolean requiresReload = false;
    private LittleRenderPipeline[] pipelines = new LittleRenderPipeline[LittleRenderPipelineType.typeCount()];
    private QuadGeneratorContext quadContext;
    
    public <T extends LittleRenderPipeline> T get(LittleRenderPipelineType<T> type) {
        T pipeline = (T) pipelines[type.id];
        if (pipeline == null) {
            pipelines[type.id] = pipeline = type.factory.get();
            pipeline.reload();
        }
        return pipeline;
    }
    
    @Override
    public void run() {
        try {
            quadContext = new QuadGeneratorContext();
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
                        data.checkLoaded();
                        
                        data.index = data.be.render.startBuildingCache();
                        BlockPos pos = data.be.getBlockPos();
                        
                        data.beforeBuilding();
                        
                        Int2ObjectMap<ChunkLayerMapList<LittleRenderBox>> cubes = data.be.render.getRenderingBoxes(data);
                        
                        if (cubes == null || cubes.isEmpty())
                            continue;
                        
                        for (ChunkLayerMapList<LittleRenderBox> map : cubes.values()) {
                            for (Tuple<RenderType, List<LittleRenderBox>> tuple : map.tuples()) {
                                for (LittleRenderBox cube : tuple.value) {
                                    if (cube.doesNeedQuadUpdate) {
                                        fakeAccess.set(data.be.getLevel(), pos, cube.state);
                                        level = fakeAccess;
                                        
                                        BlockState modelState = cube.state;
                                        rand.setSeed(modelState.getSeed(pos));
                                        BakedModel blockModel = MC.getBlockRenderer().getBlockModel(modelState);
                                        var modelData = blockModel.getModelData(level, pos, modelState, level.getModelData(pos));
                                        BlockPos offset = cube.getOffset();
                                        for (int h = 0; h < Facing.VALUES.length; h++) {
                                            Facing facing = Facing.VALUES[h];
                                            if (cube.shouldRenderFace(facing)) {
                                                if (cube.getQuad(facing) == null)
                                                    cube.setQuad(facing, cube.getBakedQuad(quadContext, level, pos, offset, modelState, blockModel, modelData, facing, tuple.key,
                                                        rand, true, ColorUtils.WHITE));
                                            } else
                                                cube.setQuad(facing, null);
                                        }
                                        cube.doesNeedQuadUpdate = false;
                                    }
                                }
                            }
                            
                        }
                        
                        quadContext.clear();
                        data.clearQuadBuilding();
                        fakeAccess.set(null, null, null);
                        data.checkRemoved();
                        level = MC.level;
                        
                        int renderState = CURRENT_RENDERING_INDEX;
                        VertexFormat format = DefaultVertexFormat.BLOCK;
                        try {
                            posestack.setIdentity();
                            get(data.getPipeline()).buildCache(posestack, buffers, data, format, bakedQuadWrapper);
                            
                            if (!LittleTiles.CONFIG.rendering.useCubeCache)
                                data.be.render.eraseBoxCache();
                            
                            if (!finish(data, buffers, renderState, false))
                                QUEUE.requeue(data);
                            
                            buffers.clear();
                            
                            if (LittleTilesProfilerOverlay.isActive())
                                LittleTilesProfilerOverlay.finishBuildingCache(System.nanoTime() - duration);
                        } catch (Exception e) {
                            LittleTiles.LOGGER.error(e);
                            if (!finish(data, EMPTY_HOLDERS, -1, false))
                                QUEUE.requeue(data);
                        }
                    } catch (RemovedBlockEntityException e) {
                        finish(data, EMPTY_HOLDERS, -1, true);
                    } catch (RenderingBlockedException e) {
                        QUEUE.requeue(data);
                    } catch (Exception e) {
                        if (!(e instanceof RenderingException))
                            LittleTiles.LOGGER.error(e);
                        finish(data, EMPTY_HOLDERS, -1, true);
                    } catch (OutOfMemoryError error) {
                        QUEUE.requeue(data);
                        LittleTiles.LOGGER.error(error);
                    } finally {
                        buffers.clear();
                        data.unsetBlocked();
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
    
    public static boolean finish(RenderingBlockContext data, ChunkLayerMap<BufferCache> buffers, int renderState, boolean force) {
        if (!data.be.render.finishBuildingCache(data.index, buffers, renderState, force))
            return false;
        
        RenderChunkExtender chunk = QUEUE.unqeue(data);
        if (chunk != null) {
            LittleTilesProfilerOverlay.chunkUpdates++;
            chunk.markReadyForUpdate(false);
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
    
    public static class RenderingBlockedException extends RenderingException {
        
        public RenderingBlockedException() {
            super("");
        }
    }
}