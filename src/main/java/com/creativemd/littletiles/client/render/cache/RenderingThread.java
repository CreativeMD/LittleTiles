package com.creativemd.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.CreativeModelPipeline;
import com.creativemd.creativecore.common.utils.type.SingletonList;
import com.creativemd.creativecore.common.world.IBlockAccessFake;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.api.IFakeRenderingBlock;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.client.render.world.RenderUtils;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator.Status;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.SVertexBuilder;

@SideOnly(Side.CLIENT)
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
                    thread.updateCoords.poll().te.render.resetRenderingState();
        }
        threadIndex = 0;
        threads = new ArrayList<>();
        for (int i = 0; i < count; i++)
            threads.add(null);
    }
    
    public static final HashMap<Object, Integer> chunks = new HashMap<>();
    public static Minecraft mc = Minecraft.getMinecraft();
    
    public static boolean addCoordToUpdate(TileEntityLittleTiles te) {
        RenderingThread renderer = getNextThread();
        
        Object chunk;
        if (te.getWorld() instanceof IOrientatedWorld)
            chunk = RenderUtils.getRenderChunk((IOrientatedWorld) te.getWorld(), te.getPos());
        else
            chunk = RenderUtils.getRenderChunk(RenderUtils.getViewFrustum(), te.getPos());
        
        if (chunk == null) {
            System.out.println("Invalid tileentity with no rendering chunk! pos: " + te.getPos() + ", world: " + te.getWorld());
            return false;
        }
        
        if (te.isRenderingEmpty()) {
            int index = te.render.startBuildingCache();
            te.render.getBoxCache().clear();
            synchronized (te.render) {
                te.render.getBufferCache().setEmpty();
            }
            if (!te.render.finishBuildingCache(index, LittleChunkDispatcher.currentRenderState, true))
                return addCoordToUpdate(te);
            return false;
        }
        
        synchronized (chunks) {
            Integer count = RenderingThread.chunks.get(chunk);
            if (count == null)
                count = 0;
            RenderingThread.chunks.put(chunk, count + 1);
        }
        renderer.updateCoords.add(new RenderingData(te, chunk));
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
                IBlockAccess world = mc.world;
                long duration = 0;
                
                if (world != null && !updateCoords.isEmpty()) {
                    RenderingData data = updateCoords.poll();
                    
                    try {
                        if (LittleTilesProfilerOverlay.isActive())
                            duration = System.nanoTime();
                        
                        if (data.te.isInvalid())
                            throw new InvalidTileEntityException(data.te.getPos() + "");
                        
                        data.index = data.te.render.startBuildingCache();
                        
                        BlockPos pos = data.te.getPos();
                        LayeredRenderBoxCache cubeCache = data.te.render.getBoxCache();
                        
                        if (cubeCache == null)
                            throw new InvalidTileEntityException(data.te.getPos() + "");
                        
                        if (data.te.getWorld() == null || !data.te.hasLoaded())
                            throw new RenderingException("Tileentity is not loaded yet");
                        
                        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                            cubeCache.set(BlockTile.getRenderingCubes(data.state, data.te, null, layer), layer);
                            
                            List<LittleRenderBox> cubes = cubeCache.get(layer);
                            for (int j = 0; j < cubes.size(); j++) {
                                RenderBox cube = cubes.get(j);
                                if (cube.doesNeedQuadUpdate) {
                                    if (ArrayUtils.contains(fakeWorldMods, cube.block.getRegistryName().getResourceDomain())) {
                                        fakeAccess.set(data.te.getWorld(), pos, cube.getBlockState());
                                        world = fakeAccess;
                                    } else
                                        world = data.te.getWorld();
                                    
                                    IBlockState modelState = cube.getBlockState().getActualState(world, pos);
                                    IBakedModel blockModel = OptifineHelper.getRenderModel(mc.getBlockRendererDispatcher().getModelForState(modelState), world, modelState, pos);
                                    modelState = cube.getModelState(modelState, world, pos);
                                    BlockPos offset = cube.getOffset();
                                    for (int h = 0; h < EnumFacing.VALUES.length; h++) {
                                        EnumFacing facing = EnumFacing.VALUES[h];
                                        if (cube.renderSide(facing)) {
                                            if (cube.getQuad(facing) == null)
                                                cube.setQuad(facing, CreativeBakedModel
                                                    .getBakedQuad(world, cube, pos, offset, modelState, blockModel, layer, facing, MathHelper.getPositionRandom(pos), false));
                                        } else
                                            cube.setQuad(facing, null);
                                    }
                                    cube.doesNeedQuadUpdate = false;
                                }
                            }
                        }
                        
                        cubeCache.sort();
                        fakeAccess.set(null, null, null);
                        if (data.te.isInvalid())
                            throw new InvalidTileEntityException(data.te.getPos() + "");
                        world = mc.world;
                        
                        int renderState = LittleChunkDispatcher.currentRenderState;
                        LayeredRenderBufferCache layerBuffer = data.te.render.getBufferCache();
                        VertexFormat format = DefaultVertexFormats.BLOCK;
                        try {
                            World renderWorld = data.te.getWorld();
                            if (renderWorld instanceof SubWorld && !((SubWorld) renderWorld).shouldRender)
                                renderWorld = ((SubWorld) renderWorld).getRealWorld();
                            
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
                                        int chunkX = MathHelper.intFloorDiv(pos.getX(), 16);
                                        int chunkY = MathHelper.intFloorDiv(pos.getY(), 16);
                                        int chunkZ = MathHelper.intFloorDiv(pos.getZ(), 16);
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
                                        IBlockState state = cube.getBlockState();
                                        
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
                                    
                                    synchronized (data.te.render) {
                                        layerBuffer.set(layer.ordinal(), buffer);
                                    }
                                } else
                                    synchronized (data.te.render) {
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
                            e.printStackTrace();
                            if (!finish(data, -1, false))
                                updateCoords.add(data);
                        }
                    } catch (InvalidTileEntityException e) {
                        finish(data, -1, true);
                    } catch (Exception e) {
                        if (!(e instanceof RenderingException))
                            e.printStackTrace();
                        //updateCoords.add(data);
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
            ((LittleRenderChunk) data.chunk).addRenderData(data.te);
        
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
        
        public final TileEntityLittleTiles te;
        public final IBlockState state;
        public final Object chunk;
        public final boolean subWorld;
        public int index;
        
        public RenderingData(TileEntityLittleTiles te, Object chunk) {
            this.te = te;
            this.state = te.getBlockTileState();
            this.chunk = chunk;
            this.subWorld = !(chunk instanceof RenderChunk);
        }
    }
}