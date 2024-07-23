package team.creative.littletiles.client.render.mc;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.MeshData.SortState;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.type.list.Tuple;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.render.cache.LayeredBufferCache;
import team.creative.littletiles.client.render.cache.buffer.BufferCollection;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferDownloader.SimpleChunkBufferDownloader;
import team.creative.littletiles.client.render.cache.buffer.ChunkBufferUploader;

public interface RenderChunkExtender {
    
    public VertexBuffer getVertexBuffer(RenderType layer);
    
    public void markReadyForUpdate(boolean playerChanged);
    
    public default VertexSorting createVertexSorting(Vec3 vec) {
        return createVertexSorting(vec.x, vec.y, vec.z);
    }
    
    public VertexSorting createVertexSorting(double x, double y, double z);
    
    public boolean isEmpty(RenderType layer);
    
    public SortState getTransparencyState();
    
    public void setTransparencyState(SortState state);
    
    public void setHasBlock(RenderType layer);
    
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
            ByteBuffer result = ByteBuffer.allocateDirect(size);
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
            ByteBufferBuilder buffer = new ByteBufferBuilder(((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + size + DefaultVertexFormat.BLOCK.getVertexSize()) / 6); // dividing by 6 is risky and could potentially cause issues
            
            BufferBuilder builder = new BufferBuilder(buffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            if (vanillaBuffer != null)
                ((ChunkBufferUploader) builder).upload(vanillaBuffer);
            
            for (LayeredBufferCache data : blocks)
                data.get(layer).upload((ChunkBufferUploader) builder);
            
            MeshData data = builder.build();
            if (layer == RenderType.translucent()) {
                var cam = Minecraft.getInstance().levelRenderer.getSectionRenderDispatcher().getCameraPosition();
                setTransparencyState(data.sortQuads(buffer, createVertexSorting(cam.x, cam.y, cam.z)));
            }
            
            uploadBuffer.bind();
            uploadBuffer.upload(data);
            buffer.close();
            VertexBuffer.unbind();
            setHasBlock(layer);
        }
        return true;
    }
    
    public default void startBuilding() {
        synchronized (this) {
            setQueued(getQueued() + 1);
        }
        backToRAM();
    }
    
    public default void endBuilding() {
        synchronized (this) {
            setQueued(getQueued() - 1);
        }
    }
    
}
