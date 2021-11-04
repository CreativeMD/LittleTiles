package team.creative.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.littletiles.client.render.level.RenderUploader;
import team.creative.littletiles.client.render.level.RenderUploader.NotSupportedException;

public class ChunkBlockLayerManager {
    
    public static final Field blockLayerManager = ObfuscationReflectionHelper.findField(VertexBuffer.class, "blockLayerManager");
    
    private final VertexBuffer buffer;
    
    private ChunkBlockLayerCache cache;
    private ChunkBlockLayerCache uploaded;
    
    public ChunkBlockLayerManager(RenderChunk chunk, RenderType layer) {
        this.buffer = chunk.getBuffer(layer);
        try {
            blockLayerManager.set(buffer, this);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }
    
    public synchronized void set(ChunkBlockLayerCache cache) {
        if (this.cache != null)
            this.cache.discard();
        this.cache = cache;
    }
    
    public synchronized void bindBuffer() {
        if (this.uploaded != null)
            backToRAM();
        uploaded = cache;
        cache = null;
        if (uploaded != null)
            uploaded.uploaded();
    }
    
    public void backToRAM() {
        if (uploaded == null)
            return;
        Supplier<Boolean> run = () -> {
            synchronized (this) {
                if (Minecraft.getInstance().level == null || uploaded == null || RenderUploader.getBufferId(buffer) == -1) {
                    if (uploaded != null)
                        uploaded.discard();
                    uploaded = null;
                    return false;
                }
                buffer.bind();
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
