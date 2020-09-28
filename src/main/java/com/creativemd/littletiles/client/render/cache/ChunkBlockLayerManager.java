package com.creativemd.littletiles.client.render.cache;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
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
	
	public void add(TileEntityLittleTiles te) {
		LayeredRenderBufferCache bufferCache = te.render.getBufferCache();
		BufferHolder holder = bufferCache.get(layer);
		if (holder != null)
			add(holder);
	}
	
	public void add(BufferHolder holder) {
		holder.add(builder);
		holders.add(holder);
	}
	
	public BufferBuilder getBuilder() {
		return builder;
	}
	
	public void readyUp() {
		this.totalSize = BufferBuilderUtils.getBufferSizeByte(builder);
	}
	
	public void bindBuffer(VertexBuffer buffer) {
		synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
			try {
				this.buffer = buffer;
				LittleChunkDispatcher.blockLayerManager.set(builder, null);
				blockLayerManager.set(buffer, this);
				this.builder = null;
				for (BufferHolder holder : holders)
					holder.useVRAM(this);
			} catch (IllegalArgumentException | IllegalAccessException e) {}
		}
	}
	
	public ByteBuffer getTempBuffer(BufferHolder holder) {
		Callable<ByteBuffer> run = () -> {
			synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
				if (Minecraft.getMinecraft().world == null || RenderUploader.getBufferId(buffer) == -1)
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
	}
	
	public void backToRAM() {
		if (buffer == null)
			return;
		Callable<Boolean> run = () -> {
			synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
				if (Minecraft.getMinecraft().world == null || RenderUploader.getBufferId(buffer) == -1) {
					for (BufferHolder holder : holders)
						holder.remove();
					holders.clear();
					blockLayerManager.set(buffer, null);
					buffer = null;
					return false;
				}
				buffer.bindBuffer();
				try {
					ByteBuffer uploadedData = RenderUploader.glMapBufferRange(totalSize);
					if (uploadedData != null) {
						for (BufferHolder holder : holders) {
							if (holder.isRemoved())
								continue;
							try {
								if (uploadedData.capacity() >= holder.getIndex() + holder.length) {
									ByteBuffer newBuffer = ByteBuffer.allocateDirect(holder.length);
									uploadedData.position(holder.getIndex());
									int end = holder.getIndex() + holder.length;
									while (uploadedData.position() < end)
										newBuffer.put(uploadedData.get());
									holder.useRAM(newBuffer);
								} else
									holder.remove();
							} catch (IllegalArgumentException e) {
								holder.remove();
							}
							
						}
					} else
						for (BufferHolder holder : holders)
							holder.remove();
					holders.clear();
					blockLayerManager.set(buffer, null);
				} catch (NotSupportedException | IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				buffer.unbindBuffer();
				buffer = null;
				return true;
			}
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
