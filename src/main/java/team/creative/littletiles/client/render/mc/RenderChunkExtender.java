package team.creative.littletiles.client.render.mc;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.ChunkLayerUploadManager;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;

public interface RenderChunkExtender {
    
    public static Vec3 offsetCorrection(Vec3i to, Vec3i from) {
        if (to == from)
            return Vec3.ZERO;
        return new Vec3(from.getX() - to.getX(), from.getY() - to.getY(), from.getZ() - to.getZ());
    }
    
    public LittleRenderPipelineType getPipeline();
    
    public void begin(BufferBuilder builder);
    
    public VertexBuffer getVertexBuffer(RenderType layer);
    
    public void markReadyForUpdate(boolean playerChanged);
    
    public default void setQuadSorting(BufferBuilder builder, Vec3 vec) {
        setQuadSorting(builder, vec.x, vec.y, vec.z);
    }
    
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z);
    
    public default void prepareModelOffset(MutableBlockPos modelOffset, BlockPos pos) {
        modelOffset.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }
    
    public boolean isEmpty(RenderType layer);
    
    public SortState getTransparencyState();
    
    public void setHasBlock(RenderType layer);
    
    public BlockPos standardOffset();
    
    public default Vec3 offsetCorrection(RenderChunkExtender chunk) {
        return offsetCorrection(standardOffset(), chunk.standardOffset());
    }
    
    public default int chunkId() {
        return -1;
    }
    
    public default ByteBuffer downloadUploadedData(VertexBufferExtender buffer, long offset, int size) {
        LittleRenderPipelineType type = getPipeline();
        type.bindBuffer(buffer);
        try {
            ByteBuffer result = MemoryTracker.create(size);
            type.getBufferSubData(offset, result);
            return result;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (!(e instanceof IllegalStateException))
                e.printStackTrace();
            return null;
        } finally {
            type.unbindBuffer();
        }
    }
    
    public default void backToRAM() {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBufferExtender buffer = (VertexBufferExtender) getVertexBuffer(layer);
            ChunkLayerUploadManager manager = buffer.getManager();
            if (manager != null)
                manager.backToRAM(this);
        }
    }
    
    public default void startBuilding(RebuildTaskExtender task) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = getVertexBuffer(layer);
            ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
            if (manager != null) {
                synchronized (manager) {
                    manager.queued++;
                }
                manager.backToRAM(this);
            } else
                ((VertexBufferExtender) vertexBuffer).setManager(manager = new ChunkLayerUploadManager(this, layer));
        }
    }
    
    public default void endBuilding(RebuildTaskExtender task) {
        ChunkLayerMap<ChunkLayerCache> caches = task.getLayeredCache();
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            VertexBuffer vertexBuffer = getVertexBuffer(layer);
            ChunkLayerUploadManager manager = ((VertexBufferExtender) vertexBuffer).getManager();
            synchronized (manager) {
                manager.queued--;
            }
            if (caches != null)
                manager.set(caches.get(layer));
        }
    }
    
}
