package com.creativemd.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderBox;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayeredRenderBufferCache {
	
	private BufferHolder buffers[] = new BufferHolder[BlockRenderLayer.values().length];
	
	public LayeredRenderBufferCache() {
		
	}
	
	public void remove(int layer) {
		if (buffers[layer] != null)
			buffers[layer].onRemoved();
		buffers[layer] = null;
	}
	
	public BufferHolder get(BlockRenderLayer layer) {
		return buffers[layer.ordinal()];
	}
	
	public void setEmpty() {
		synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
			for (int i = 0; i < buffers.length; i++)
				remove(i);
		}
	}
	
	public void set(BlockRenderLayer layer, BufferBuilder buffer) {
		synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
			remove(layer.ordinal());
			
			if (buffer != null)
				buffers[layer.ordinal()] = new BufferHolder(this, layer.ordinal(), buffer);
		}
	}
	
	public void combine(LayeredRenderBufferCache cache) {
		synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
			for (int i = 0; i < buffers.length; i++)
				buffers[i] = combine(i, buffers[i], cache.buffers[i]);
		}
	}
	
	private BufferHolder combine(int layer, BufferHolder first, BufferHolder second) {
		if (first == null || first.isRemoved())
			return second;
		else if (second == null || second.isRemoved())
			return first;
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(first.length + second.length);
		
		ByteBuffer firstBuffer = first.getBuffer();
		firstBuffer.position(0);
		firstBuffer.limit(first.length);
		byteBuffer.put(firstBuffer);
		
		ByteBuffer secondBuffer = second.getBuffer();
		secondBuffer.position(0);
		secondBuffer.limit(second.length);
		byteBuffer.put(secondBuffer);
		first.onRemoved();
		second.onRemoved();
		return new BufferHolder(this, layer, byteBuffer, first.length + second.length, first.vertexCount + second.vertexCount);
	}
	
	public static BufferBuilder createVertexBuffer(VertexFormat format, List<? extends RenderBox> cubes) {
		int size = 1;
		for (RenderBox cube : cubes)
			size += cube.countQuads();
		return new BufferBuilder(format.getNextOffset() * size);
	}
}
