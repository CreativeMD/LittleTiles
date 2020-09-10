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
	
	private BufferHolder buffers[] = new BufferHolder[BlockRenderLayer.values().length];
	
	public LayeredRenderBufferCache() {
		
	}
	
	public BufferHolder get(BlockRenderLayer layer) {
		return buffers[layer.ordinal()];
	}
	
	public void setEmpty() {
		for (int i = 0; i < buffers.length; i++) {
			if (buffers[i] != null)
				buffers[i].remove();
			buffers[i] = null;
		}
	}
	
	public void set(BlockRenderLayer layer, BufferBuilder buffer) {
		if (buffers[layer.ordinal()] != null)
			buffers[layer.ordinal()].remove();
		
		if (buffer != null)
			buffers[layer.ordinal()] = new BufferHolder(buffer);
		else
			buffers[layer.ordinal()] = null;
	}
	
	public void combine(LayeredRenderBufferCache cache) {
		for (int i = 0; i < buffers.length; i++)
			buffers[i] = combine(buffers[i], cache.buffers[i]);
	}
	
	public BufferHolder combine(BufferHolder first, BufferHolder second) {
		if (first == null)
			return second;
		else if (second == null)
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
		return new BufferHolder(byteBuffer, first.length + second.length, first.vertexCount + second.vertexCount);
	}
	
	public static BufferBuilder createVertexBuffer(VertexFormat format, List<? extends RenderBox> cubes) {
		int size = 1;
		for (RenderBox cube : cubes)
			size += cube.countQuads();
		return new BufferBuilder(format.getNextOffset() * size);
	}
	
	public class BufferHolder {
		
		private ChunkBlockLayerManager manager;
		private ByteBuffer byteBuffer;
		private int index = -1;
		
		public final int length;
		public final int vertexCount;
		
		public BufferHolder(BufferBuilder buffer) {
			this.length = BufferBuilderUtils.getBufferSizeByte(buffer);
			this.vertexCount = buffer.getVertexCount();
			this.byteBuffer = buffer.getByteBuffer();
		}
		
		public BufferHolder(ByteBuffer buffer, int byteSize, int count) {
			this.length = byteSize;
			this.vertexCount = count;
			this.byteBuffer = buffer;
		}
		
		public ChunkBlockLayerManager getManager() {
			return manager;
		}
		
		public int getIndex() {
			return index;
		}
		
		public void perpareVRAM(int index) {
			this.index = index;
		}
		
		public void add(BufferBuilder builder) {
			BufferBuilderUtils.addBuffer(builder, getBuffer(), length, vertexCount);
		}
		
		public boolean hasBufferInRAM() {
			return byteBuffer != null;
		}
		
		public void useVRAM(ChunkBlockLayerManager manager) {
			this.manager = manager;
			byteBuffer = null;
		}
		
		public void useRAM(ByteBuffer buffer) {
			this.byteBuffer = buffer;
			this.index = -1;
			this.manager = null;
		}
		
		public ByteBuffer getBuffer() {
			if (byteBuffer != null)
				return byteBuffer;
			if (index != -1) {
				manager.backToRAM();
				return byteBuffer;
			}
			throw new IllegalStateException("Index of VRAM buffer is not set!");
		}
		
		public void remove() {
			if (manager != null) {
				manager.remove(this);
				manager = null;
			}
		}
		
	}
}
