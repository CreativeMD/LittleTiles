package team.creative.littletiles.client.render.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.type.list.BucketList;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
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
    private BucketList<LittleRenderChunk> toRender = new BucketList<>();
    
    @Nullable
    public Future<?> lastFullRenderChunkUpdate;
    
    public LittleLevelRenderManager(LittleLevel level) {
        this.level = level;
    }
    
    public synchronized LittleRenderChunk getChunk(BlockPos pos) {
        return chunks.get(SectionPos.asLong(pos));
    }
    
    public synchronized LittleRenderChunk getChunk(SectionPos pos) {
        return chunks.get(pos.asLong());
    }
    
    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer rendered, VertexBuffer buffer) {
        return LittleTilesClient.ANIMATION_HANDLER.uploadChunkLayer(rendered, buffer);
    }
    
    public void schedule(LittleRenderChunk.ChunkCompileTask task) {
        LittleTilesClient.ANIMATION_HANDLER.schedule(task);
    }
    
    public void addRecentlyCompiledChunk(LittleRenderChunk chunk) {
        LittleTilesClient.ANIMATION_HANDLER.addRecentlyCompiledChunk(chunk);
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
        int i = Math.floorMod(x, this.chunkGridSizeX);
        int j = Math.floorMod(y - this.level.getMinSection(), this.chunkGridSizeY);
        int k = Math.floorMod(z, this.chunkGridSizeZ);
        LittleRenderChunk chunk = this.chunks[this.getChunkIndex(i, j, k)];
        chunk.setDirty(playerChanged);
    }
    
    public void setCameraPosition(Vec3d vec) {
        this.camera = vec;
        this.cameraPos.set(vec.x, vec.y, vec.z);
    }
    
    public void setChunkCameraPosition(Vec3d vec) {
        this.chunkCamera = vec;
    }
    
    public Vec3d getCameraPosition() {
        return camera;
    }
    
    public BlockPos getCameraBlockPos() {
        return cameraPos;
    }
    
    public Vec3d getChunkCameraPosition() {
        return chunkCamera;
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
        return toRender;
    }
    
    public Iterator<LittleRenderChunk> visibleChunksInverse() {
        return toRender.inverseIterator();
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
    }
    
}
