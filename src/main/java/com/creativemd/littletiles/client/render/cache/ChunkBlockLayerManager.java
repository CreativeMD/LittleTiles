package com.creativemd.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.lwjgl.opengl.GL30;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache.BufferHolder;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUploader.NotSupportedException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ChunkBlockLayerManager {
	
	private static final Field blockLayerManager = ReflectionHelper.findField(VertexBuffer.class, "blockLayerManager");
	
	private BufferBuilder builder;
	private VertexBuffer buffer;
	private BlockRenderLayer layer;
	private int totalSize;
	private List<BufferHolder> holders = new ArrayList<>();
	
	public ChunkBlockLayerManager(BufferBuilder builder, BlockRenderLayer layer) {
		this.builder = builder;
		this.layer = layer;
	}
	
	public void add(TileEntityLittleTiles te) {
		LayeredRenderBufferCache bufferCache = te.render.getBufferCache();
		BufferHolder holder = bufferCache.get(layer);
		if (holder != null) {
			int index = BufferBuilderUtils.getBufferSizeByte(builder);
			if (holder.getManager() != null)
				holder.getManager().backToRAM();
			holder.perpareVRAM(index);
			holder.add(builder);
			holders.add(holder);
		}
	}
	
	public void readyUp() {
		this.totalSize = BufferBuilderUtils.getBufferSizeByte(builder);
	}
	
	public void bindBuffer(VertexBuffer buffer) {
		try {
			this.buffer = buffer;
			LittleChunkDispatcher.blockLayerManager.set(builder, null);
			blockLayerManager.set(buffer, this);
			this.builder = null;
			for (BufferHolder holder : holders)
				holder.useVRAM(this);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
	}
	
	public void backToRAM() {
		Callable<Boolean> run = () -> {
			buffer.bindBuffer();
			try {
				ByteBuffer uploadedData = RenderUploader.glMapBufferRange(OpenGlHelper.GL_ARRAY_BUFFER, totalSize, GL30.GL_MAP_READ_BIT, null);
				if (uploadedData != null)
					for (BufferHolder holder : holders) {
						ByteBuffer newBuffer = ByteBuffer.allocateDirect(holder.length);
						uploadedData.position(holder.getIndex());
						uploadedData.limit(uploadedData.position() + holder.length);
						newBuffer.put(uploadedData);
						holder.useRAM(newBuffer);
					}
				holders.clear();
				blockLayerManager.set(buffer, null);
			} catch (NotSupportedException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			
			buffer.unbindBuffer();
			buffer = null;
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
	
	public void remove(BufferHolder holder) {
		if (holders != null)
			return;
		try {
			holders.remove(holder);
			if (holders.isEmpty())
				blockLayerManager.set(buffer, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
	}
	
}
