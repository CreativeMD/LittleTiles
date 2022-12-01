package team.creative.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL15;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import team.creative.littletiles.client.render.level.RenderUploader;
import team.creative.littletiles.client.render.level.RenderUploader.NotSupportedException;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;

public class ChunkLayerUploadManager {
    
    private final VertexBuffer buffer;
    
    private ChunkLayerCache cache;
    private ChunkLayerCache uploaded;
    
    public int queued;
    
    public ChunkLayerUploadManager(RenderChunkExtender chunk, RenderType layer) {
        this.buffer = chunk.getVertexBuffer(layer);
        ((VertexBufferExtender) buffer).setManager(this);
    }
    
    public ChunkLayerCache get() {
        return cache;
    }
    
    public synchronized void set(ChunkLayerCache cache) {
        if (this.cache != null)
            this.cache.discard();
        this.cache = cache;
    }
    
    public void uploaded() {
        synchronized (this) {
            if (this.uploaded != null)
                backToRAM();
            uploaded = cache;
            cache = null;
            if (uploaded != null)
                uploaded.uploaded(queued == 0);
        }
    }
    
    public void backToRAM() {
        if (uploaded == null)
            return;
        Supplier<Boolean> run = () -> {
            synchronized (this) {
                if (Minecraft.getInstance().level == null || uploaded == null || ((VertexBufferExtender) buffer).getVertexBufferId() == -1) {
                    if (uploaded != null)
                        uploaded.discard();
                    uploaded = null;
                    return false;
                }
                GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((VertexBufferExtender) buffer).getVertexBufferId());
                try {
                    ByteBuffer uploadedData = RenderUploader.glMapBufferRange(uploaded.totalSize());
                    if (uploadedData != null)
                        uploaded.download(uploadedData);
                    else
                        uploaded.discard();
                    uploaded = null;
                } catch (NotSupportedException e) {
                    e.printStackTrace();
                }
                VertexBuffer.unbind();
                return true;
            }
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
    
}
