package team.creative.littletiles.client.render.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.level.LittleRenderChunk;
import team.creative.littletiles.client.render.level.LittleRenderChunk.ChunkCompileTask;
import team.creative.littletiles.client.render.level.LittleRenderChunks;
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.level.little.LittleLevel;

@OnlyIn(Dist.CLIENT)
public class LittleLevelRenderManager implements Iterable<LittleRenderChunk> {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    public final LittleLevel level;
    
    public boolean needsFullRenderChunkUpdate = false;
    public Boolean isInSight;
    
    private Vec3d camera;
    private Vec3d chunkCamera = new Vec3d(0, 0, 0);
    private MutableBlockPos cameraPos = new MutableBlockPos();
    
    private HashMap<Long, LittleRenderChunk> chunks = new HashMap<>();
    private LittleRenderChunks sortedChunks = new LittleRenderChunks();
    
    @Nullable
    public Future<?> lastFullRenderChunkUpdate;
    
    private final BlockingQueue<LittleRenderChunk> queuedCompiled = new LinkedBlockingQueue<>();
    private final BlockingQueue<LittleRenderChunk> emptyCompiled = new LinkedBlockingQueue<>();
    
    public LittleLevelRenderManager(LittleLevel level) {
        this.level = level;
    }
    
    public synchronized LittleRenderChunk getChunk(BlockPos pos) {
        return chunks.get(SectionPos.asLong(pos));
    }
    
    public synchronized LittleRenderChunk getChunk(SectionPos pos, boolean create) {
        long value = pos.asLong();
        LittleRenderChunk chunk = chunks.get(value);
        if (chunk != null)
            chunks.put(value, chunk = new LittleRenderChunk(this, pos));
        return chunk;
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
    
    public void setupRender(LittleLevelEntity animation, Camera camera, Frustum frustum, boolean capturedFrustum, boolean spectator) {
        Vec3d cam = new Vec3d(camera.getPosition());
        
        isInSight = LittleLevelEntityRenderer.INSTANCE.shouldRender(animation, frustum, cam.x, cam.y, cam.z); // needs to original camera position
        
        synchronized (this) {
            while (!emptyCompiled.isEmpty()) {
                LittleRenderChunk chunk = emptyCompiled.poll();
                if (chunk.considered.compareAndSet(true, false))
                    sortedChunks.remove(chunk);
                chunks.remove(chunk.section.asLong());
                chunk.releaseBuffers();
            }
        }
        
        animation.getOrigin().transformPointToFakeWorld(cam); // from here on the camera is transformed to the sub level
        
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
            
            lastFullRenderChunkUpdate = Util.backgroundExecutor().submit(() -> sortedChunks.arrangeRings(SectionPos.of(cameraPos), chunks.values()));
        } else
            synchronized (this) {
                while (!queuedCompiled.isEmpty()) {
                    LittleRenderChunk chunk = queuedCompiled.poll();
                    if (chunk.considered.compareAndSet(false, true))
                        sortedChunks.add(chunk);
                }
            }
    }
    
    public ChunkBufferBuilderPack fixedBuffers() {
        return LittleTilesClient.ANIMATION_HANDLER.fixedBuffers;
    }
    
    public void blockChanged(BlockGetter level, BlockPos pos, BlockState actualState, BlockState setState, int updateType) {
        this.setBlockDirty(pos, (updateType & 8) != 0);
    }
    
    private void setBlockDirty(BlockPos pos, boolean playerChanged) {
        for (int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i)
            for (int j = pos.getX() - 1; j <= pos.getX() + 1; ++j)
                for (int k = pos.getY() - 1; k <= pos.getY() + 1; ++k)
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), playerChanged);
    }
    
    public void setBlocksDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (int i = minZ - 1; i <= maxZ + 1; ++i)
            for (int j = minX - 1; j <= maxX + 1; ++j)
                for (int k = minY - 1; k <= maxY + 1; ++k)
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i));
                
    }
    
    public void setBlockDirty(BlockPos pos, BlockState actualState, BlockState setState) {
        if (mc.getModelManager().requiresRender(actualState, setState))
            this.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }
    
    public void setSectionDirtyWithNeighbors(int x, int y, int z) {
        this.setSectionDirty(x - 1, y - 1, z - 1);
        this.setSectionDirty(x - 1, y, z - 1);
        this.setSectionDirty(x - 1, y + 1, z - 1);
        
        this.setSectionDirty(x, y - 1, z - 1);
        this.setSectionDirty(x, y, z - 1);
        this.setSectionDirty(x, y + 1, z - 1);
        
        this.setSectionDirty(x + 1, y - 1, z - 1);
        this.setSectionDirty(x + 1, y, z - 1);
        this.setSectionDirty(x + 1, y + 1, z - 1);
        
        this.setSectionDirty(x - 1, y - 1, z);
        this.setSectionDirty(x - 1, y, z);
        this.setSectionDirty(x - 1, y + 1, z);
        
        this.setSectionDirty(x, y - 1, z);
        this.setSectionDirty(x, y, z);
        this.setSectionDirty(x, y + 1, z);
        
        this.setSectionDirty(x + 1, y - 1, z);
        this.setSectionDirty(x + 1, y, z);
        this.setSectionDirty(x + 1, y + 1, z);
        
        this.setSectionDirty(x - 1, y - 1, z + 1);
        this.setSectionDirty(x - 1, y, z + 1);
        this.setSectionDirty(x - 1, y + 1, z + 1);
        
        this.setSectionDirty(x, y - 1, z + -1);
        this.setSectionDirty(x, y, z + 1);
        this.setSectionDirty(x, y + 1, z + 1);
        
        this.setSectionDirty(x + 1, y - 1, z + 1);
        this.setSectionDirty(x + 1, y, z + 1);
        this.setSectionDirty(x + 1, y + 1, z + 1);
    }
    
    public void setSectionDirty(int x, int y, int z) {
        this.setSectionDirty(x, y, z, false);
    }
    
    private void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        LittleRenderChunk chunk = chunks.get(SectionPos.asLong(x, y, z));
        if (chunk == null) {
            SectionPos pos = SectionPos.of(x, y, z);
            chunks.put(pos.asLong(), chunk = new LittleRenderChunk(this, pos));
        }
        chunk.setDirty(playerChanged);
    }
    
    public Vec3d getCameraPosition() {
        return camera;
    }
    
    public BlockPos getCameraBlockPos() {
        return cameraPos;
    }
    
    public synchronized void unload() {
        for (LittleRenderChunk chunk : this)
            chunk.updateGlobalBlockEntities(Collections.EMPTY_LIST);
        
        for (LittleRenderChunk chunk : this)
            chunk.releaseBuffers();
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
    
    public void allChanged() {
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
