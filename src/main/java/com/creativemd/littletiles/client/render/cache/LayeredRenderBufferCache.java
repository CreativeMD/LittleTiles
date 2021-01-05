package com.creativemd.littletiles.client.render.cache;

import java.nio.ByteBuffer;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayeredRenderBufferCache {
	
	private BufferBuilderWrapper queue[] = new BufferBuilderWrapper[BlockRenderLayer.values().length];
	private BufferLink uploaded[] = new BufferLink[BlockRenderLayer.values().length];
	
	public LayeredRenderBufferCache() {
		
	}
	
	public IRenderDataCache get(int layer) {
		if (queue[layer] == null)
			return uploaded[layer];
		return queue[layer];
	}
	
	public synchronized void setEmptyIfEqual(BufferLink link, int layer) {
		if (uploaded[layer] == link)
			uploaded[layer] = null;
	}
	
	public synchronized void setUploaded(BufferLink link, int layer) {
		queue[layer] = null;
		uploaded[layer] = link;
	}
	
	public synchronized void set(int layer, BufferBuilder buffer) {
		queue[layer] = buffer != null ? new BufferBuilderWrapper(buffer) : null;
	}
	
	public synchronized void setEmpty() {
		for (int i = 0; i < queue.length; i++) {
			queue[i] = null;
			uploaded[i] = null;
		}
	}
	
	/*public void remove(int layer) {
		if (buffers[layer] != null)
			buffers[layer].onRemoved();
		buffers[layer] = null;
	}
	
	public BufferHolder get(BlockRenderLayer layer) {
		return buffers[layer.ordinal()];
	}
	
	public void combine(LayeredRenderBufferCache cache) {
		synchronized (BufferHolder.BUFFER_CHANGE_LOCK) {
			for (int i = 0; i < buffers.length; i++)
				buffers[i] = combine(i, buffers[i], cache.buffers[i]);
		}
	}
	
	private BufferHolder combine(int layer, BufferHolder first, BufferHolder second) {
		int vertexCount = 0;
		int length = 0;
		ByteBuffer firstBuffer = null;
		if (first != null && !first.isRemoved()) {
			firstBuffer = first.hasBufferInRAM() ? first.getBufferRAM() : first.tryGetBufferVRAM();
			if (firstBuffer != null) {
				vertexCount += first.vertexCount;
				length += first.length;
			}
		}
		
		ByteBuffer secondBuffer = null;
		if (second != null && !second.isRemoved()) {
			secondBuffer = second.hasBufferInRAM() ? second.getBufferRAM() : second.tryGetBufferVRAM();
			if (secondBuffer != null) {
				vertexCount += second.vertexCount;
				length += second.length;
			}
		}
		
		if (vertexCount == 0)
			return null;
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
		
		if (firstBuffer != null) {
			firstBuffer.position(0);
			firstBuffer.limit(first.length);
			byteBuffer.put(firstBuffer);
		}
		
		if (secondBuffer != null) {
			secondBuffer.position(0);
			secondBuffer.limit(second.length);
			byteBuffer.put(secondBuffer);
		}
		if (first != null)
			first.onRemoved();
		return new BufferHolder(this, layer, byteBuffer, length, vertexCount);
	}*/
	
	public static BufferBuilder createVertexBuffer(VertexFormat format, List<? extends RenderBox> cubes) {
		int size = 1;
		for (RenderBox cube : cubes)
			size += cube.countQuads();
		return new BufferBuilder(format.getNextOffset() * size);
	}
	
	public static class BufferBuilderWrapper implements IRenderDataCache {
		
		public final BufferBuilder builder;
		
		public BufferBuilderWrapper(BufferBuilder builder) {
			this.builder = builder;
		}
		
		@Override
		public ByteBuffer byteBuffer() {
			return builder.getByteBuffer();
		}
		
		@Override
		public int length() {
			return BufferBuilderUtils.getBufferSizeByte(builder);
		}
		
		@Override
		public int vertexCount() {
			return builder.getVertexCount();
		}
		
	}
}
