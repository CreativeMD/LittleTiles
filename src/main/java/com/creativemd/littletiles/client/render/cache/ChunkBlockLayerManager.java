package com.creativemd.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache.BufferHolder;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.client.render.world.RenderUploader;
import com.creativemd.littletiles.client.render.world.RenderUploader.NotSupportedException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
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
	
	public synchronized void add(TileEntityLittleTiles te) {
		LayeredRenderBufferCache bufferCache = te.render.getBufferCache();
		BufferHolder holder = bufferCache.get(layer);
		if (holder != null)
			add(holder);
	}
	
	public synchronized void add(BufferHolder holder) {
		int index = BufferBuilderUtils.getBufferSizeByte(builder);
		if (holder.getManager() != null)
			holder.getManager().backToRAM();
		holder.perpareVRAM(index);
		holder.add(builder);
		holders.add(holder);
	}
	
	public BufferBuilder getBuilder() {
		return builder;
	}
	
	public synchronized void readyUp() {
		this.totalSize = BufferBuilderUtils.getBufferSizeByte(builder);
	}
	
	public synchronized void bindBuffer(VertexBuffer buffer) {
		try {
			this.buffer = buffer;
			LittleChunkDispatcher.blockLayerManager.set(builder, null);
			blockLayerManager.set(buffer, this);
			this.builder = null;
			for (BufferHolder holder : holders)
				holder.useVRAM(this);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
	}
	
	public synchronized void backToRAM() {
		if (buffer == null)
			return;
		Callable<Boolean> run = () -> {
			if (Minecraft.getMinecraft().world == null)
				return false;
			buffer.bindBuffer();
			try {
				ByteBuffer uploadedData = RenderUploader.glMapBufferRange(totalSize);
				if (uploadedData != null) {
					uploadedData.rewind();
					for (BufferHolder holder : holders) {
						ByteBuffer newBuffer = ByteBuffer.allocateDirect(holder.length);
						try {
							if (uploadedData.capacity() >= holder.getIndex() + holder.length) {
								uploadedData.position(holder.getIndex());
								int end = holder.getIndex() + holder.length;
								while (uploadedData.position() < end)
									newBuffer.put(uploadedData.get());
							}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						holder.useRAM(newBuffer);
					}
				} else
					System.out.println("No uploaded data found");
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
	
	public synchronized void remove(BufferHolder holder) {
		if (holders == null)
			return;
		try {
			holders.remove(holder);
			if (holders.isEmpty() && buffer != null)
				blockLayerManager.set(buffer, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
	}
	
}
