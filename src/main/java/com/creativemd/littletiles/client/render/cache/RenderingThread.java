package com.creativemd.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.CreativeModelPipeline;
import com.creativemd.creativecore.common.world.IBlockAccessFake;
import com.creativemd.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.optifine.shaders.SVertexBuilder;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.level.SubLevel;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.creativecore.common.util.type.SingletonList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.api.IFakeRenderingBlock;
import team.creative.littletiles.client.render.LittleRenderUtils;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

@OnlyIn(Dist.CLIENT)
public class RenderingThread extends Thread {
    
    private static final String[] fakeWorldMods = new String[] { "chisel" };
    
    public static List<RenderingThread> threads;
    
    private static int threadIndex;
    
    public static synchronized RenderingThread getNextThread() {
        synchronized (threads) {
            RenderingThread thread = threads.get(threadIndex);
            if (thread == null)
                threads.set(threadIndex, thread = new RenderingThread(threadIndex));
            threadIndex++;
            if (threadIndex >= threads.size())
                threadIndex = 0;
            
            return thread;
        }
    }
    
    public static void initThreads(int count) {
        if (count <= 0)
            throw new IllegalArgumentException("count has to be at least equal or greater than one");
        if (threads != null) {
            for (RenderingThread thread : threads)
                if (thread != null)
                    thread.interrupt();
                
            for (RenderingThread thread : threads)
                while (thread != null && thread.updateCoords.size() > 0)
                    thread.updateCoords.poll().be.render.resetRenderingState();
        }
        threadIndex = 0;
        threads = new ArrayList<>();
        for (int i = 0; i < count; i++)
            threads.add(null);
    }
    
    public static final HashMap<Object, Integer> chunks = new HashMap<>();
    public static Minecraft mc = Minecraft.getInstance();
    
    public static boolean addCoordToUpdate(BETiles be) {
        RenderingThread renderer = getNextThread();
        
        Object chunk;
        if (be.getLevel() instanceof IOrientatedLevel)
            chunk = LittleRenderUtils.getRenderChunk((IOrientatedLevel) be.getLevel(), be.getBlockPos());
        else
            chunk = LittleRenderUtils.getRenderChunk(LittleRenderUtils.getViewFrustum(), be.getBlockPos());
        
        if (chunk == null) {
            System.out.println("Invalid tileentity with no rendering chunk! pos: " + be.getBlockPos() + ", level: " + be.getLevel());
            return false;
        }
        
        if (be.isEmpty()) {
            int index = be.render.startBuildingCache();
            be.render.getBoxCache().clear();
            synchronized (be.render) {
                be.render.getBufferCache().setEmpty();
            }
            if (!be.render.finishBuildingCache(index, LittleChunkDispatcher.currentRenderState, true))
                return addCoordToUpdate(be);
            return false;
        }
        
        synchronized (chunks) {
            Integer count = RenderingThread.chunks.get(chunk);
            if (count == null)
                count = 0;
            RenderingThread.chunks.put(chunk, count + 1);
        }
        renderer.updateCoords.add(new RenderingData(be, chunk));
        return true;
    }
    
    static {
        initThreads(LittleTiles.CONFIG.rendering.renderingThreadCount);
    }
    
    public ConcurrentLinkedQueue<RenderingData> updateCoords = new ConcurrentLinkedQueue<>();
    
    final int index;
    
    public RenderingThread(int index) {
        this.index = index;
        start();
    }
    
    public int getThreadIndex() {
        return index;
    }
    
    private final SingletonList<BakedQuad> bakedQuadWrapper = new SingletonList<BakedQuad>(null);
    private final IBlockAccessFake fakeAccess = new IBlockAccessFake();
    public boolean active = true;
    
    @Override
    public void run() {
        try {
            while (active) {
                Level world = mc.level;
                long duration = 0;
                
                if (world != null && !updateCoords.isEmpty()) {
                    RenderingData data = updateCoords.poll();
                    
                    try {
                        if (LittleTilesProfilerOverlay.isActive())
                            duration = System.nanoTime();
                        
                        if (data.be.isRemoved())
                            throw new InvalidTileEntityException(data.be.getBlockPos() + "");
                        
                        data.index = data.be.render.startBuildingCache();
                        
                        BlockPos pos = data.be.getBlockPos();
                        LayeredRenderBoxCache cubeCache = data.be.render.getBoxCache();
                        
                        if (cubeCache == null)
                            throw new InvalidTileEntityException(data.be.getBlockPos() + "");
                        
                        if (data.be.getLevel() == null || !data.be.hasLoaded())
                            throw new RenderingException("Tileentity is not loaded yet");
                        
                        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                            cubeCache.set(BlockTile.getRenderingCubes(data.state, data.be, null, layer), layer);
                            
                            List<LittleRenderBox> cubes = cubeCache.get(layer);
                            for (int j = 0; j < cubes.size(); j++) {
                                RenderBox cube = cubes.get(j);
                                if (cube.doesNeedQuadUpdate) {
                                    if (ArrayUtils.contains(fakeWorldMods, cube.block.getRegistryName().getResourceDomain())) {
                                        fakeAccess.set(data.te.getWorld(), pos, cube.getBlockState());
                                        world = fakeAccess;
                                    } else
                                        world = data.te.getWorld();
                                    
                                    BlockState modelState = cube.getBlockState().getActualState(world, pos);
                                    IBakedModel blockModel = OptifineHelper.getRenderModel(mc.getBlockRendererDispatcher().getModelForState(modelState), world, modelState, pos);
                                    modelState = cube.getModelState(modelState, world, pos);
                                    BlockPos offset = cube.getOffset();
                                    for (int h = 0; h < EnumFacing.VALUES.length; h++) {
                                        EnumFacing facing = EnumFacing.VALUES[h];
                                        if (cube.renderSide(facing)) {
                                            if (cube.getQuad(facing) == null)
                                                cube.setQuad(facing, CreativeBakedModel
                                                        .getBakedQuad(world, cube, pos, offset, modelState, blockModel, layer, facing, Mth.getPositionRandom(pos), false));
                                        } else
                                            cube.setQuad(facing, null);
                                    }
                                    cube.doesNeedQuadUpdate = false;
                                }
                            }
                        }
                        
                        cubeCache.sort();
                        fakeAccess.set(null, null, null);
                        if (data.be.isRemoved())
                            throw new InvalidTileEntityException(data.be.getBlockPos() + "");
                        world = mc.level;
                        
                        int renderState = LittleChunkDispatcher.currentRenderState;
                        LayeredRenderBufferCache layerBuffer = data.be.render.getBufferCache();
                        VertexFormat format = DefaultVertexFormats.BLOCK;
                        try {
                            Level renderWorld = data.be.getLevel();
                            if (renderWorld instanceof SubLevel && !((SubLevel) renderWorld).shouldRender)
                                renderWorld = ((SubLevel) renderWorld).getRealLevel();
                            
                            // Render vertex buffer
                            for (int i = 0; i < BlockRenderLayer.values().length; i++) {
                                BlockRenderLayer layer = BlockRenderLayer.values()[i];
                                
                                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(layer);
                                
                                List<LittleRenderBox> cubes = cubeCache.get(layer);
                                BufferBuilder buffer = null;
                                
                                if (cubes != null && cubes.size() > 0)
                                    buffer = LayeredRenderBufferCache.createVertexBuffer(format, cubes);
                                
                                if (buffer != null) {
                                    buffer.begin(7, format);
                                    if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isRenderRegions() && !data.subWorld) {
                                        int bits = 8;
                                        RenderChunk chunk = (RenderChunk) data.chunk;
                                        int dx = chunk.getPosition().getX() >> bits << bits;
                                        int dy = chunk.getPosition().getY() >> bits << bits;
                                        int dz = chunk.getPosition().getZ() >> bits << bits;
                                        
                                        dx = OptifineHelper.getRenderChunkRegionX(chunk);
                                        dz = OptifineHelper.getRenderChunkRegionZ(chunk);
                                        
                                        buffer.setTranslation(-dx, -dy, -dz);
                                    } else {
                                        int chunkX = Mth.intFloorDiv(pos.getX(), 16);
                                        int chunkY = Mth.intFloorDiv(pos.getY(), 16);
                                        int chunkZ = Mth.intFloorDiv(pos.getZ(), 16);
                                        buffer.setTranslation(-chunkX * 16, -chunkY * 16, -chunkZ * 16);
                                    }
                                    
                                    boolean smooth = Minecraft.isAmbientOcclusionEnabled() && data.state.getLightValue(renderWorld, pos) == 0; //&& modelIn.isAmbientOcclusion(stateIn);
                                    
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
                                    
                                    buffer.finishDrawing();
                                    
                                    synchronized (data.be.render) {
                                        layerBuffer.set(layer.ordinal(), buffer);
                                    }
                                } else
                                    synchronized (data.be.render) {
                                        layerBuffer.set(layer.ordinal(), null);
                                    }
                            }
                            
                            net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
                            
                            if (!LittleTiles.CONFIG.rendering.useCubeCache)
                                cubeCache.clear();
                            if (!finish(data, renderState, false))
                                updateCoords.add(data);
                            
                            if (LittleTilesProfilerOverlay.isActive())
                                LittleTilesProfilerOverlay.finishBuildingCache(System.nanoTime() - duration);
                        } catch (Exception e) {
                            if (!(e instanceof RenderingException))
                                e.printStackTrace();
                            if (!finish(data, -1, false))
                                updateCoords.add(data);
                        }
                    } catch (InvalidTileEntityException e) {
                        finish(data, -1, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        updateCoords.add(data);
                    } catch (OutOfMemoryError error) {
                        updateCoords.add(data);
                        error.printStackTrace();
                    }
                    data = null;
                } else if (world == null && (!updateCoords.isEmpty() || !chunks.isEmpty())) {
                    updateCoords.clear();
                    chunks.clear();
                }
                
                if (updateCoords.isEmpty())
                    sleep(1);
                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();
            }
        } catch (InterruptedException e) {}
    }
    
    public static final Field compileTaskField = ReflectionHelper.findField(RenderChunk.class, new String[] { "compileTask", "field_178599_i" });
    
    public static boolean finish(RenderingData data, int renderState, boolean force) {
        if (!data.te.render.finishBuildingCache(data.index, renderState, force))
            return false;
        
        boolean complete = false;
        
        synchronized (chunks) {
            Integer count = chunks.get(data.chunk);
            if (count != null)
                if (count <= 1) {
                    chunks.remove(data.chunk);
                    complete = true;
                } else
                    chunks.put(data.chunk, count - 1);
                
            /*boolean finished = true;
            for (RenderingThread thread : threads) {
            	if (thread != null && !thread.updateCoords.isEmpty()) {
            		finished = false;
            		break;
            	}
            }*/
            //if (finished && !chunks.isEmpty())
            //chunks.clear();
        }
        
        if (data.subWorld)
            ((LittleRenderChunk) data.chunk).addRenderData(data.be);
        
        if (complete) {
            if (data.subWorld) {
                LittleTilesProfilerOverlay.ltChunksUpdates++;
                ((LittleRenderChunk) data.chunk).markCompleted();
            } else {
                LittleTilesProfilerOverlay.vanillaChunksUpdates++;
                markRenderUpdate((RenderChunk) data.chunk);
            }
        }
        
        return true;
        
    }
    
    public static void markRenderUpdate(RenderChunk chunk) {
        try {
            chunk.getLockCompileTask().lock();
            
            if (isChunkCurrentlyUpdating(chunk))
                LittleEventHandler.queueChunkUpdate(chunk);
            else
                chunk.setNeedsUpdate(false);
            
        } finally {
            chunk.getLockCompileTask().unlock();
        }
    }
    
    public static boolean isChunkCurrentlyUpdating(RenderChunk chunk) {
        try {
            ChunkCompileTaskGenerator compileTask = (ChunkCompileTaskGenerator) compileTaskField.get(chunk);
            return chunk.needsUpdate() || (compileTask != null && compileTask
                    .getType() == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK && (compileTask.getStatus() != Status.COMPILING || compileTask.getStatus() != Status.UPLOADING));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static class InvalidTileEntityException extends Exception {
        
        public InvalidTileEntityException(String arg0) {
            super(arg0);
        }
    }
    
    public static class RenderingException extends Exception {
        
        public RenderingException(String arg0) {
            super(arg0);
        }
    }
    
    private static class RenderingData {
        
        public final BETiles be;
        public final BlockState state;
        public final Object chunk;
        public final boolean subWorld;
        public int index;
        
        public RenderingData(BETiles be, Object chunk) {
            this.be = be;
            this.state = be.getBlockTileState();
            this.chunk = chunk;
            this.subWorld = !(chunk instanceof RenderChunk);
        }
    }
}