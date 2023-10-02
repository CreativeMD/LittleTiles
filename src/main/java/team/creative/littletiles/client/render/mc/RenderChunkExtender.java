package team.creative.littletiles.client.render.mc;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader.SimpleChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;
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
    
    public default int sectionIndex() {
        return -1;
    }
    
    public int getQueued();
    
    public void setQueued(int queued);
    
    public ChunkLayerMap<BufferCollection> getLastUploaded();
    
    public void setLastUploaded(ChunkLayerMap<BufferCollection> uploaded);
    
    public default void prepareUpload() {
        setLastUploaded(null);
    }
    
    public default void uploaded(RenderType layer, BufferCollection buffers) {
        if (buffers == null)
            return;
        ChunkLayerMap<BufferCollection> uploaded = getLastUploaded();
        if (getLastUploaded() == null)
            setLastUploaded(uploaded = new ChunkLayerMap<>());
        
        uploaded.put(layer, buffers);
        synchronized (this) {
            if (getQueued() == 0) // if the queue is empty the buffers can be removed from RAM (they are only available in VRAM from this point on, until they are downloaded again)
                buffers.eraseBuffers();
        }
    }
    
    public default void backToRAM() {
        ChunkLayerMap<BufferCollection> lastUploaded = getLastUploaded();
        if (lastUploaded == null)
            return;
        Supplier<Boolean> run = () -> {
            SimpleChunkBufferDownloader downloader = new SimpleChunkBufferDownloader();
            for (Tuple<RenderType, BufferCollection> tuple : lastUploaded.tuples()) {
                VertexBuffer buffer = getVertexBuffer(tuple.key);
                BufferCollection uploaded = tuple.value;
                
                if (Minecraft.getInstance().level == null || uploaded == null || ((VertexBufferExtender) buffer).getVertexBufferId() == -1) {
                    if (uploaded != null)
                        uploaded.discard();
                    continue;
                }
                
                ByteBuffer uploadedData = downloadUploadedData((VertexBufferExtender) buffer, 0, ((VertexBufferExtender) buffer).getLastUploadedLength());
                if (uploadedData != null) {
                    downloader.buffer = uploadedData;
                    uploaded.download(downloader);
                    uploadedData.rewind();
                    downloader.buffer = null;
                } else
                    uploaded.discard();
            }
            setLastUploaded(null);
            return true;
        };
        try {
            if (Minecraft.getInstance().isSameThread())
                run.get();
            else {
                CompletableFuture<Boolean> future = Minecraft.getInstance().submit(run);
                future.get();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    public default ByteBuffer downloadUploadedData(VertexBufferExtender buffer, long offset, int size) {
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer.getVertexBufferId());
        try {
            ByteBuffer result = MemoryTracker.create(size);
            GL15C.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, offset, result);
            return result;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (!(e instanceof IllegalStateException))
                e.printStackTrace();
            return null;
        } finally {}
    }
    
    public default boolean appendRenderData(Iterable<? extends LayeredBufferCache> blocks) {
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            
            int size = 0;
            for (LayeredBufferCache data : blocks)
                size += data.length(layer);
            
            if (size == 0)
                continue;
            
            VertexBuffer uploadBuffer = getVertexBuffer(layer);
            
            if (uploadBuffer == null)
                return false;
            
            VertexFormat format = uploadBuffer.getFormat();
            if (format == null)
                format = DefaultVertexFormat.BLOCK;
            
            ByteBuffer vanillaBuffer = null;
            if (!isEmpty(layer))
                vanillaBuffer = downloadUploadedData((VertexBufferExtender) uploadBuffer, 0, ((VertexBufferExtender) uploadBuffer).getLastUploadedLength());
            
            BufferBuilder builder = new BufferBuilder(((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + size + DefaultVertexFormat.BLOCK.getVertexSize()) / 6); // dividing by 6 is risking and could potentially cause issues
            begin(builder);
            if (vanillaBuffer != null) {
                if (layer == RenderType.translucent()) {
                    SortState state = getTransparencyState();
                    if (state != null)
                        builder.restoreSortState(state);
                }
                
                builder.putBulkData(vanillaBuffer);
            }
            
            for (LayeredBufferCache data : blocks)
                data.get(layer).upload((ChunkBufferUploader) builder);
            
            if (layer == RenderType.translucent())
                setQuadSorting(builder, Minecraft.getInstance().levelRenderer.getSectionRenderDispatcher().getCameraPosition());
            
            uploadBuffer.bind();
            uploadBuffer.upload(builder.end());
            VertexBuffer.unbind();
            setHasBlock(layer);
        }
        return true;
    }
    
    public default void startBuilding(RebuildTaskExtender task) {
        synchronized (this) {
            setQueued(getQueued() + 1);
        }
        backToRAM();
    }
    
    public default void endBuilding(RebuildTaskExtender task) {
        synchronized (this) {
            setQueued(getQueued() - 1);
        }
    }
    
}
