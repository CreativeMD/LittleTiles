package team.creative.littletiles.client.render.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.level.little.LittleClientLevel;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.client.render.level.LittleRenderChunk.ChunkCompileTask;
import team.creative.littletiles.client.render.level.LittleRenderChunks;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;

@OnlyIn(Dist.CLIENT)
public class LittleLevelRenderManager extends LittleEntityRenderManager<LittleLevelEntity> implements Iterable<LittleRenderChunk> {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    private Vec3d camera;
    private Vec3d chunkCamera = new Vec3d(0, 0, 0);
    private MutableBlockPos cameraPos = new MutableBlockPos();
    
    private HashMap<Long, LittleRenderChunk> chunks = new HashMap<>();
    private LittleRenderChunks sortedChunks = new LittleRenderChunks();
    
    @Nullable
    public Future<?> lastFullRenderChunkUpdate;
    
    private final BlockingQueue<LittleRenderChunk> queuedCompiled = new LinkedBlockingQueue<>();
    private final BlockingQueue<LittleRenderChunk> emptyCompiled = new LinkedBlockingQueue<>();
    
    public LittleLevelRenderManager(LittleLevelEntity entity) {
        super(entity);
    }
    
    @Override
    public RenderChunkExtender getRenderChunk(BlockPos pos) {
        return getOrCreateChunk(pos);
    }
    
    public synchronized LittleRenderChunk getChunk(BlockPos pos) {
        return chunks.get(SectionPos.asLong(pos));
    }
    
    public synchronized LittleRenderChunk getOrCreateChunk(BlockPos pos) {
        return getChunk(SectionPos.of(pos), true);
    }
    
    public synchronized LittleRenderChunk getChunk(SectionPos pos, boolean create) {
        long value = pos.asLong();
        LittleRenderChunk chunk = chunks.get(value);
        if (chunk == null && create)
            return create(pos);
        return chunk;
    }
    
    private LittleRenderChunk create(SectionPos pos) {
        if (Minecraft.getInstance().isSameThread()) {
            LittleRenderChunk chunk;
            chunks.put(pos.asLong(), chunk = new LittleRenderChunk(this, pos));
            return chunk;
        } else {
            CompletableFuture<LittleRenderChunk> future = Minecraft.getInstance().submit(() -> {
                LittleRenderChunk created = new LittleRenderChunk(this, pos);
                chunks.put(pos.asLong(), created);
                return created;
            });
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer rendered, VertexBuffer buffer) {
        return LittleTilesClient.ANIMATION_HANDLER.uploadChunkLayer(rendered, buffer);
    }
    
    public void schedule(ChunkCompileTask task) {
        LittleTilesClient.ANIMATION_HANDLER.schedule(task);
    }
    
    public void emptyChunk(LittleRenderChunk chunk) {
        emptyCompiled.add(chunk);
    }
    
    public void queueChunk(LittleRenderChunk chunk) {
        if (chunk.considered.compareAndSet(false, true))
            queuedCompiled.add(chunk);
    }
    
    @Override
    public void setupRender(Camera camera, @Nullable Frustum frustum, boolean capturedFrustum, boolean spectator) {
        super.setupRender(camera, frustum, capturedFrustum, spectator);
        
        synchronized (this) {
            while (!emptyCompiled.isEmpty()) {
                LittleRenderChunk chunk = emptyCompiled.poll();
                if (chunk.considered.compareAndSet(true, false))
                    sortedChunks.remove(chunk);
                chunks.remove(chunk.section.asLong());
                chunk.releaseBuffers();
            }
        }
        
        Vec3d cam = new Vec3d(camera.getPosition());
        entity.getOrigin().transformPointToFakeWorld(cam); // from here on the camera is transformed to the sub level
        
        this.camera = cam;
        this.cameraPos.set(cam.x, cam.y, cam.z);
        BlockPos cameraPos = getCameraBlockPos();
        Vec3d chunkCamera = new Vec3d(Math.floor(cam.x / 8.0D), Math.floor(cam.y / 8.0D), Math.floor(cam.z / 8.0D));
        
        needsFullRenderChunkUpdate |= this.chunkCamera.equals(chunkCamera);
        
        this.chunkCamera = chunkCamera;
        /*Level level = (Level) animation.getSubLevel();
        boolean headOccupied = mc.smartCull; This needs to be implemented
        if (spectator && level.getBlockState(cameraPos).isSolidRender(level, cameraPos))
            headOccupied = false;*/
        
        if (capturedFrustum || !isInSight)
            return;
        
        if (needsFullRenderChunkUpdate && (lastFullRenderChunkUpdate == null || lastFullRenderChunkUpdate.isDone())) {
            needsFullRenderChunkUpdate = false;
            queuedCompiled.clear();
            
            sortedChunks.arrangeRings(SectionPos.of(cameraPos), chunks.values());
        } else
            synchronized (this) {
                while (!queuedCompiled.isEmpty()) {
                    LittleRenderChunk chunk = queuedCompiled.poll();
                    if (chunk.considered.compareAndSet(false, true))
                        sortedChunks.add(chunk);
                }
            }
    }
    
    @Override
    public void compileChunks(Camera camera) {
        mc.getProfiler().push("compile_animation_chunks");
        List<LittleRenderChunk> schedule = Lists.newArrayList();
        
        LittleClientLevel level = (LittleClientLevel) getLevel();
        
        for (LittleRenderChunk chunk : this) {
            ChunkPos chunkpos = new ChunkPos(chunk.pos);
            if (chunk.isDirty() && (!entity.isReal() || level.getChunk(chunkpos.x, chunkpos.z).isLightCorrect())) {
                boolean immediate = false;
                if (mc.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.PLAYER_AFFECTED)
                    immediate = chunk.isDirtyFromPlayer();
                else if (mc.options.prioritizeChunkUpdates().get() == PrioritizeChunkUpdates.NEARBY) {
                    immediate = !ForgeConfig.CLIENT.alwaysSetupTerrainOffThread
                            .get() && (chunk.pos.offset(8, 8, 8).distSqr(getCameraBlockPos()) < 768.0D || chunk.isDirtyFromPlayer()); // the target is the else block below, so invert the forge addition to get there early
                }
                
                if (immediate) {
                    chunk.compile();
                    chunk.setNotDirty();
                } else
                    schedule.add(chunk);
            }
        }
        
        for (LittleRenderChunk chunk : schedule) {
            chunk.compileASync();
            chunk.setNotDirty();
        }
        
        mc.getProfiler().pop();
    }
    
    @Override
    protected void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        for (LittleRenderChunk chunk : this) {
            List<BlockEntity> list = chunk.getCompiledChunk().getRenderableBlockEntities();
            if (list.isEmpty())
                continue;
            
            for (BlockEntity blockentity : list)
                renderBlockEntity(blockentity, pose, frustum, cam, frameTime, bufferSource);
        }
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        int i = 0;
        for (LittleRenderChunk chunk : visibleChunks()) {
            if (i > 14)
                return;
            if (chunk.resortTransparency(layer))
                i++;
        }
    }
    
    @Override
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset) {
        for (Iterator<LittleRenderChunk> iterator = layer == RenderType.translucent() ? visibleChunksInverse() : visibleChunks().iterator(); iterator.hasNext();) {
            LittleRenderChunk chunk = iterator.next();
            if (!chunk.getCompiledChunk().isEmpty(layer)) {
                VertexBuffer vertexbuffer = chunk.getVertexBuffer(layer);
                if (offset != null) {
                    offset.set((float) (chunk.pos.getX() - x), (float) (chunk.pos.getY() - y), (float) (chunk.pos.getZ() - z));
                    offset.upload();
                }
                
                vertexbuffer.bind();
                vertexbuffer.draw();
            }
        }
        
        if (offset != null)
            offset.set(0F, 0F, 0F);
    }
    
    public ChunkBufferBuilderPack fixedBuffers() {
        return LittleTilesClient.ANIMATION_HANDLER.fixedBuffers;
    }
    
    @Override
    protected void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        LittleRenderChunk chunk = chunks.get(SectionPos.asLong(x, y, z));
        if (chunk == null)
            chunk = create(SectionPos.of(x, y, z));
        chunk.setDirty(playerChanged);
    }
    
    public Vec3d getCameraPosition() {
        return camera;
    }
    
    public BlockPos getCameraBlockPos() {
        return cameraPos;
    }
    
    public void updateGlobalBlockEntities(Collection<BlockEntity> oldBlockEntities, Collection<BlockEntity> newBlockEntities) {
        synchronized (this.globalBlockEntities) {
            this.globalBlockEntities.removeAll(oldBlockEntities);
            this.globalBlockEntities.addAll(newBlockEntities);
        }
    }
    
    @Override
    public synchronized void unload() {
        for (LittleRenderChunk chunk : this)
            chunk.releaseBuffers();
        
        super.unload();
    }
    
    @Override
    public Iterator<LittleRenderChunk> iterator() {
        return chunks.values().iterator();
    }
    
    public Iterable<LittleRenderChunk> visibleChunks() {
        return sortedChunks;
    }
    
    public Iterator<LittleRenderChunk> visibleChunksInverse() {
        return sortedChunks.inverseIterator();
    }
    
    @Override
    public void allChanged() {
        super.allChanged();
        needsFullRenderChunkUpdate = true;
        
        if (this.lastFullRenderChunkUpdate != null)
            try {
                this.lastFullRenderChunkUpdate.get();
                this.lastFullRenderChunkUpdate = null;
            } catch (Exception exception) {
                LittleTiles.LOGGER.warn("Full update failed", exception);
            }
        
        this.queuedCompiled.clear();
    }
    
}
