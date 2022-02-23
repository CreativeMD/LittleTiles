package team.creative.littletiles.client.render.cache;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.client.rendering.model.CreativeModelPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.optifine.shaders.SVertexBuilder;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBakedModel;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.LevelAccesorFake;
import team.creative.creativecore.common.level.SubClientLevel;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.SingletonList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.api.IFakeRenderingBlock;
import team.creative.littletiles.client.render.LittleRenderUtils;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
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
        
        if (be.isEmpty()) {
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
                
                if (level != null && !QUEUE.isEmpty()) {
                    RenderingBlockContext data = QUEUE.poll();
                    
                    try {
                        if (LittleTilesProfilerOverlay.isActive())
                            duration = System.nanoTime();
                        
                        data.checkRemoved();
                        
                        data.index = data.be.render.startBuildingCache();
                        BlockPos pos = data.be.getBlockPos();
                        
                        data.checkLoaded();
                        
                        data.beforeBuilding();
                        
                        for (RenderType layer : LittleRenderUtils.CHUNK_RENDER_TYPES) {
                            List<LittleRenderBox> cubes = data.be.render.getRenderingBoxes(data, layer);
                            
                            for (int j = 0; j < cubes.size(); j++) {
                                RenderBox cube = cubes.get(j);
                                if (cube.doesNeedQuadUpdate) {
                                    if (ArrayUtils.contains(fakeLeveldMods, cube.state.getBlock().getRegistryName().getNamespace())) {
                                        fakeAccess.set(data.be.getLevel(), pos, cube.state);
                                        level = fakeAccess;
                                    } else
                                        level = data.be.getLevel();
                                    
                                    BlockState modelState = cube.state;
                                    BakedModel blockModel = OptifineHelper.getRenderModel(mc.getBlockRenderer().getBlockModel(modelState), level, modelState, pos);
                                    modelState = cube.getModelState(modelState, level, pos);
                                    BlockPos offset = cube.getOffset();
                                    for (int h = 0; h < Facing.VALUES.length; h++) {
                                        Facing facing = Facing.VALUES[h];
                                        if (cube.renderSide(facing)) {
                                            if (cube.getQuad(facing) == null)
                                                cube.setQuad(facing, CreativeBakedModel
                                                        .getBakedQuad(level, cube, pos, offset, modelState, blockModel, layer, facing, Mth.getSeed(pos), false));
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
                        LayeredRenderBufferCache layerBuffer = data.be.render.getBufferCache();
                        VertexFormat format = DefaultVertexFormat.BLOCK;
                        try {
                            Level renderWorld = data.be.getLevel();
                            if (renderWorld instanceof SubClientLevel && !((SubClientLevel) renderWorld).shouldRender)
                                renderWorld = ((SubClientLevel) renderWorld).getRealLevel();
                            
                            // Render vertex buffer
                            for (Entry<RenderType, List<LittleRenderBox>> entry : data.be.render.boxCache.entrySet()) {
                                RenderType layer = entry.getKey();
                                ForgeHooksClient.setRenderType(layer);
                                
                                List<LittleRenderBox> cubes = entry.getValue();
                                BufferBuilder buffer = null;
                                
                                if (cubes != null && cubes.size() > 0)
                                    buffer = LayeredRenderBufferCache.createVertexBuffer(format, cubes);
                                
                                if (buffer != null) {
                                    buffer.begin(VertexFormat.Mode.QUADS, format);
                                    if (OptifineHelper.installed() && OptifineHelper.isRenderRegions() && !data.subWorld) {
                                        int bits = 8;
                                        RenderChunk chunk = (RenderChunk) data.chunk;
                                        int dx = chunk.getOrigin().getX() >> bits << bits;
                                        int dy = chunk.getOrigin().getY() >> bits << bits;
                                        int dz = chunk.getOrigin().getZ() >> bits << bits;
                                        
                                        dx = OptifineHelper.getRenderChunkRegionX(chunk);
                                        dz = OptifineHelper.getRenderChunkRegionZ(chunk);
                                        
                                        buffer.setTranslation(-dx, -dy, -dz);
                                    } else {
                                        int chunkX = Mth.intFloorDiv(pos.getX(), 16);
                                        int chunkY = Mth.intFloorDiv(pos.getY(), 16);
                                        int chunkZ = Mth.intFloorDiv(pos.getZ(), 16);
                                        buffer.setTranslation(-chunkX * 16, -chunkY * 16, -chunkZ * 16);
                                    }
                                    
                                    boolean smooth = Minecraft.useAmbientOcclusion() && data.state.getLightEmission(renderWorld, pos) == 0; //&& modelIn.isAmbientOcclusion(stateIn);
                                    
                                    BitSet bitset = null;
                                    float[] afloat = null;
                                    Object ambientFace = null;
                                    if (FMLClientHandler.instance().hasOptifine())
                                        ambientFace = OptifineHelper.getEnv(buffer, renderWorld, data.state, pos);
                                    else if (smooth) {
                                        bitset = new BitSet(3);
                                        afloat = new float[EnumFacing.VALUES.length * 2];
                                        ambientFace = CreativeModelPipeline.createAmbientOcclusionFace();
                                    }
                                    
                                    for (int j = 0; j < cubes.size(); j++) {
                                        RenderBox cube = cubes.get(j);
                                        BlockState state = cube.getBlockState();
                                        
                                        if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders()) {
                                            if (state.getBlock() instanceof IFakeRenderingBlock)
                                                state = ((IFakeRenderingBlock) state.getBlock()).getFakeState(state);
                                            SVertexBuilder.pushEntity(state, pos, data.te.getWorld(), buffer);
                                        }
                                        
                                        for (int h = 0; h < EnumFacing.VALUES.length; h++) {
                                            EnumFacing facing = EnumFacing.VALUES[h];
                                            Object quadObject = cube.getQuad(facing);
                                            List<BakedQuad> quads = null;
                                            if (quadObject instanceof List) {
                                                quads = (List<BakedQuad>) quadObject;
                                            } else if (quadObject instanceof BakedQuad) {
                                                bakedQuadWrapper.setElement((BakedQuad) quadObject);
                                                quads = bakedQuadWrapper;
                                            }
                                            if (quads != null && !quads.isEmpty())
                                                if (smooth)
                                                    CreativeModelPipeline
                                                            .renderBlockFaceSmooth(renderWorld, state, pos, buffer, layer, quads, afloat, facing, bitset, ambientFace, cube);
                                                else
                                                    CreativeModelPipeline.renderBlockFaceFlat(renderWorld, state, pos, buffer, layer, quads, facing, bitset, cube, ambientFace);
                                        }
                                        
                                        bakedQuadWrapper.setElement(null);
                                        
                                        if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
                                            SVertexBuilder.popEntity(buffer);
                                        
                                        if (!LittleTiles.CONFIG.rendering.useQuadCache)
                                            cube.deleteQuadCache();
                                    }
                                    
                                    if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
                                        SVertexBuilder.calcNormalChunkLayer(buffer);
                                    
                                    buffer.end();
                                    
                                    synchronized (data.be.render) {
                                        layerBuffer.set(layer, buffer);
                                    }
                                } else
                                    synchronized (data.be.render) {
                                        layerBuffer.set(layer, null);
                                    }
                            }
                            
                            net.minecraftforge.client.ForgeHooksClient.setRenderType(null);
                            
                            if (!LittleTiles.CONFIG.rendering.useCubeCache)
                                data.be.render.boxCache.clear();
                            if (!finish(data, renderState, false))
                                QUEUE.add(data);
                            
                            if (LittleTilesProfilerOverlay.isActive())
                                LittleTilesProfilerOverlay.finishBuildingCache(System.nanoTime() - duration);
                        } catch (Exception e) {
                            if (!(e instanceof RenderingException))
                                e.printStackTrace();
                            if (!finish(data, -1, false))
                                QUEUE.add(data);
                        }
                    } catch (RemovedBlockEntityException e) {
                        finish(data, -1, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        QUEUE.add(data);
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
        
        if (data.subWorld)
            ((LittleRenderChunk) data.chunk).addRenderData(data.be);
        
        if (complete) {
            if (data.subWorld) {
                LittleTilesProfilerOverlay.ltChunksUpdates++;
                ((LittleRenderChunk) data.chunk).markCompleted();
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