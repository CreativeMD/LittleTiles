package team.creative.littletiles.client.render.cache.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.lighting.QuadLighter;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeQuadLighter;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.LevelAccesorFake;
import team.creative.creativecore.common.level.SubClientLevel;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.creativecore.mixin.ForgeModelBlockRendererAccessor;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.api.IFakeRenderingBlock;
import team.creative.littletiles.client.render.LittleRenderUtils;
import team.creative.littletiles.client.render.cache.BlockBufferCache;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;

@OnlyIn(Dist.CLIENT)
public class RenderingThread extends Thread {
    
    private static final String[] fakeLeveldMods = new String[] { "chisel" };
    public static List<RenderingThread> THREADS;
    public static final HashMap<Object, Integer> CHUNKS = new HashMap<>();
    public static final Minecraft mc = Minecraft.getInstance();
    private static final ConcurrentLinkedQueue<RenderingBlockContext> QUEUE = new ConcurrentLinkedQueue<>();
    
    public static void initThreads(int count) {
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
    
    public static void unload() {
        for (RenderingThread thread : THREADS)
            if (thread != null)
                thread.interrupt();
            
        THREADS = null;
        
        QUEUE.clear();
        CHUNKS.clear();
    }
    
    public static boolean queue(BETiles be) {
        if (THREADS == null)
            initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
        
        Object chunk;
        if (be.getLevel() instanceof IOrientatedLevel)
            chunk = LittleRenderUtils.getRenderChunk((IOrientatedLevel) be.getLevel(), be.getBlockPos());
        else
            chunk = LittleRenderUtils.getRenderChunk(LittleRenderUtils.getViewArea(), be.getBlockPos());
        
        if (chunk == null) {
            System.out.println("Invalid tileentity with no rendering chunk! pos: " + be.getBlockPos() + ", level: " + be.getLevel());
            return false;
        }
        
        if (be.isRenderingEmpty()) {
            int index = be.render.startBuildingCache();
            be.render.boxCache.clear();
            synchronized (be.render) {
                be.render.getBufferCache().setEmpty();
            }
            if (!be.render.finishBuildingCache(index, LittleChunkDispatcher.currentRenderState, true))
                return queue(be);
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
    
    static {
        initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
    }
    
    public RenderingThread() {
        start();
    }
    
    private final SingletonList<BakedQuad> bakedQuadWrapper = new SingletonList<BakedQuad>(null);
    private final LevelAccesorFake fakeAccess = new LevelAccesorFake();
    public boolean active = true;
    
    @Override
    public void run() {
        try {
            while (active) {
                LevelAccessor level = mc.level;
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
                            List<LittleRenderBox> cubes = data.be.render.getRenderingBoxes(data, layer);
                            
                            for (int j = 0; j < cubes.size(); j++) {
                                RenderBox cube = cubes.get(j);
                                if (cube.doesNeedQuadUpdate) {
                                    if (ArrayUtils.contains(fakeLeveldMods, cube.state.getBlock().builtInRegistryHolder().key().location().getNamespace())) {
                                        fakeAccess.set(data.be.getLevel(), pos, cube.state);
                                        level = fakeAccess;
                                    } else
                                        level = data.be.getLevel();
                                    
                                    BlockState modelState = cube.state;
                                    rand.setSeed(modelState.getSeed(pos));
                                    BakedModel blockModel = OptifineHelper.getRenderModel(mc.getBlockRenderer().getBlockModel(modelState), level, modelState, pos);
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
                        level = mc.level;
                        
                        int renderState = LittleChunkDispatcher.currentRenderState;
                        BlockBufferCache layerBuffer = data.be.render.getBufferCache();
                        VertexFormat format = DefaultVertexFormat.BLOCK;
                        try {
                            Level renderLevel = data.be.getLevel();
                            if (renderLevel instanceof SubClientLevel && !((SubClientLevel) renderLevel).shouldRender)
                                renderLevel = ((SubClientLevel) renderLevel).getRealLevel();
                            
                            ForgeModelBlockRendererAccessor renderer = (ForgeModelBlockRendererAccessor) mc.getBlockRenderer().getModelRenderer();
                            boolean smooth = Minecraft.useAmbientOcclusion() && data.state.getLightEmission(data.be.getLevel(), pos) == 0;
                            QuadLighter lighter = smooth ? renderer.getSmoothLighter().get() : renderer.getFlatLighter().get();
                            
                            lighter.setup(renderLevel, pos, data.state);
                            
                            int overlay = OverlayTexture.NO_OVERLAY;
                            //ModelBlockRenderer.enableCaching();
                            
                            posestack.pushPose();
                            posestack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                            
                            // Render vertex buffer
                            for (Entry<RenderType, List<LittleRenderBox>> entry : data.be.render.boxCache.entrySet()) {
                                RenderType layer = entry.getKey();
                                
                                List<LittleRenderBox> cubes = entry.getValue();
                                BufferBuilder buffer = null;
                                
                                if (cubes != null && cubes.size() > 0)
                                    buffer = BlockBufferCache.createVertexBuffer(format, cubes);
                                
                                if (buffer != null) {
                                    buffer.begin(VertexFormat.Mode.QUADS, format);
                                    
                                    for (int j = 0; j < cubes.size(); j++) {
                                        RenderBox cube = cubes.get(j);
                                        BlockState state = cube.state;
                                        
                                        ((CreativeQuadLighter) lighter).setState(state);
                                        ((CreativeQuadLighter) lighter).setCustomTint(cube.color);
                                        
                                        if (OptifineHelper.isShaders()) {
                                            if (state.getBlock() instanceof IFakeRenderingBlock)
                                                state = ((IFakeRenderingBlock) state.getBlock()).getFakeState(state);
                                            OptifineHelper.pushBuffer(state, pos, data.be.getLevel(), buffer);
                                        }
                                        
                                        for (int h = 0; h < Facing.VALUES.length; h++) {
                                            Facing facing = Facing.VALUES[h];
                                            Object quadObject = cube.getQuad(facing);
                                            List<BakedQuad> quads = null;
                                            if (quadObject instanceof List) {
                                                quads = (List<BakedQuad>) quadObject;
                                            } else if (quadObject instanceof BakedQuad) {
                                                bakedQuadWrapper.setElement((BakedQuad) quadObject);
                                                quads = bakedQuadWrapper;
                                            }
                                            if (quads != null && !quads.isEmpty())
                                                for (BakedQuad quad : quads)
                                                    lighter.process(buffer, posestack.last(), quad, overlay);
                                        }
                                        
                                        bakedQuadWrapper.setElement(null);
                                        
                                        if (OptifineHelper.isShaders())
                                            OptifineHelper.popBuffer(buffer);
                                        
                                        if (!LittleTiles.CONFIG.rendering.useQuadCache)
                                            cube.deleteQuadCache();
                                    }
                                    
                                    if (OptifineHelper.isShaders())
                                        OptifineHelper.calcNormalChunkLayer(buffer);
                                    
                                    synchronized (data.be.render) {
                                        layerBuffer.set(layer, buffer.end());
                                    }
                                } else
                                    synchronized (data.be.render) {
                                        layerBuffer.set(layer, null);
                                    }
                            }
                            
                            ((CreativeQuadLighter) lighter).setCustomTint(-1);
                            posestack.popPose();
                            //ModelBlockRenderer.clearCache();
                            lighter.reset();
                            
                            if (!LittleTiles.CONFIG.rendering.useCubeCache)
                                data.be.render.boxCache.clear();
                            if (!finish(data, renderState, false))
                                QUEUE.add(data);
                            
                            if (LittleTilesProfilerOverlay.isActive())
                                LittleTilesProfilerOverlay.finishBuildingCache(System.nanoTime() - duration);
                        } catch (Exception e) {
                            
                            e.printStackTrace();
                            if (!finish(data, -1, false))
                                QUEUE.add(data);
                        }
                    } catch (RemovedBlockEntityException e) {
                        finish(data, -1, true);
                    } catch (Exception e) {
                        if (!(e instanceof RenderingException))
                            e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        QUEUE.add(data);
                        error.printStackTrace();
                    }
                    data = null;
                } else if (level == null || QUEUE.isEmpty())
                    sleep(1);
                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();
            }
        } catch (InterruptedException e) {}
    }
    
    public static boolean finish(RenderingBlockContext data, int renderState, boolean force) {
        if (!data.be.render.finishBuildingCache(data.index, renderState, force))
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
        
        //if (data.subWorld)
        //((LittleRenderChunk) data.chunk).addRenderData(data.be);
        
        if (complete) {
            if (data.subWorld) {
                LittleTilesProfilerOverlay.ltChunksUpdates++;
                //((LittleRenderChunk) data.chunk).markCompleted();
            } else {
                LittleTilesProfilerOverlay.vanillaChunksUpdates++;
                ((RenderChunk) data.chunk).setDirty(true);
            }
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