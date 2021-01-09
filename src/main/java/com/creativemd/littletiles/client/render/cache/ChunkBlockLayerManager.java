package com.creativemd.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUploader.NotSupportedException;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ChunkBlockLayerManager {
    
    public static final Field blockLayerManager = ReflectionHelper.findField(VertexBuffer.class, "blockLayerManager");
    
    private final RenderChunk chunk;
    private final BlockRenderLayer layer;
    private final VertexBuffer buffer;
    
    private ChunkBlockLayerCache cache;
    private ChunkBlockLayerCache uploaded;
    
    public ChunkBlockLayerManager(RenderChunk chunk, BlockRenderLayer layer) {
        this.chunk = chunk;
        this.layer = layer;
        this.buffer = chunk.getVertexBufferByLayer(layer.ordinal());
        try {
            blockLayerManager.set(buffer, this);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }
    
    public void set(BufferBuilder builder, ChunkBlockLayerCache cache) {
        if (this.cache != null)
            this.cache.discard();
        else if (this.uploaded != null) {
            this.uploaded.discard();
            this.uploaded = null;
        }
        this.cache = cache;
    }
    
    public void bindBuffer() {
        if (uploaded != null)
            uploaded.discard();
        uploaded = cache;
        cache = null;
        if (uploaded != null)
            uploaded.uploaded();
    }
    
    /*public ByteBuffer getTempBuffer(BufferHolder holder) {
    	Callable<ByteBuffer> run = () -> {
    		synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
    			if (Minecraft.getMinecraft().world == null || buffer == null || RenderUploader.getBufferId(buffer) == -1)
    				return null;
    			buffer.bindBuffer();
    			try {
    				ByteBuffer uploadedData = RenderUploader.glMapBufferRange(totalSize);
    				if (uploadedData != null) {
    					if (holder.isRemoved())
    						return null;
    					try {
    						if (uploadedData.capacity() >= holder.getIndex() + holder.length) {
    							ByteBuffer newBuffer = ByteBuffer.allocateDirect(holder.length);
    							uploadedData.position(holder.getIndex());
    							int end = holder.getIndex() + holder.length;
    							while (uploadedData.position() < end)
    								newBuffer.put(uploadedData.get());
    							return newBuffer;
    						}
    					} catch (IllegalArgumentException e) {}
    				}
    				return null;
    			} catch (NotSupportedException | IllegalArgumentException e) {
    				e.printStackTrace();
    			}
    			buffer.unbindBuffer();
    			return null;
    		}
    	};
    	try {
    		if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
    			return run.call();
    		else {
    			ListenableFuture<ByteBuffer> future = Minecraft.getMinecraft().addScheduledTask(run);
    			return future.get();
    		}
    	} catch (Exception e1) {
    		e1.printStackTrace();
    		return null;
    	}
    }*/
    
    public void backToRAM() {
        if (uploaded == null)
            return;
        Callable<Boolean> run = () -> {
            if (Minecraft.getMinecraft().world == null || uploaded == null || RenderUploader.getBufferId(buffer) == -1) {
                if (uploaded != null)
                    uploaded.discard();
                uploaded = null;
                return false;
            }
            buffer.bindBuffer();
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
            buffer.unbindBuffer();
            return true;
        };
        try {
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
                run.call();
            else {
                ListenableFuture<Boolean> future = Minecraft.getMinecraft().addScheduledTask(run);
                future.get();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
}
