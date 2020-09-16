package com.creativemd.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.cache.ChunkBlockLayerManager;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache.BufferHolder;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

public class LittleRenderChunk {
	
	protected int lastRenderIndex = LittleChunkDispatcher.currentRenderState;
	
	public final BlockPos pos;
	protected final VertexBuffer[] vertexBuffers = new VertexBuffer[BlockRenderLayer.values().length];
	protected ChunkBlockLayerManager[] managers = new ChunkBlockLayerManager[BlockRenderLayer.values().length];
	
	protected List<BufferHolder>[] queuedBuffers = new List[BlockRenderLayer.values().length];
	protected boolean[] bufferChanged = new boolean[BlockRenderLayer.values().length];
	
	private LinkedHashMap<BlockPos, TileEntityLittleTiles> tileEntities = new LinkedHashMap<>();
	/** if one of the blocks has been modified, which requires the chunk cache to be uploaded again */
	private boolean modified = false;
	private boolean complete = false;
	
	public LittleRenderChunk(BlockPos pos) {
		this.pos = pos;
	}
	
	public int transparencySortedIndex = 0;
	
	public void addRenderData(TileEntityLittleTiles te) {
		synchronized (tileEntities) {
			TileEntityLittleTiles existing = tileEntities.get(te.getPos());
			
			if (existing != null) {
				if (existing != te) {
					if (te.isEmpty())
						tileEntities.remove(te.getPos());
					else
						tileEntities.put(te.getPos(), te);
				} else if (te.isEmpty())
					tileEntities.remove(te.getPos());
				
				modified = true;
			} else {
				if (te.isEmpty())
					return;
				
				tileEntities.put(te.getPos(), te);
				
				if (complete)
					modified = true;
				
				if (!modified)
					addRenderDataInternal(te);
			}
			
			if (modified)
				complete = false;
		}
	}
	
	private void addRenderDataInternal(TileEntityLittleTiles te) {
		LayeredRenderBufferCache cache = te.render.getBufferCache();
		for (int i = 0; i < BlockRenderLayer.values().length; i++) {
			BlockRenderLayer layer = BlockRenderLayer.values()[i];
			BufferHolder tempBuffer = cache.get(layer);
			if (tempBuffer != null) {
				if (queuedBuffers[i] == null)
					queuedBuffers[i] = new ArrayList<>();
				
				queuedBuffers[i].add(tempBuffer);
			}
		}
	}
	
	public void resortTransparency(int index, float x, float y, float z) {
		if (index == transparencySortedIndex)
			return;
		
		this.transparencySortedIndex = index;
		
		int translucentIndex = BlockRenderLayer.TRANSLUCENT.ordinal();
		
		BufferBuilder builder = managers[translucentIndex] != null ? managers[translucentIndex].getBuilder() : null;
		if (builder != null) {
			builder.sortVertexData(x, y, z);
			if (vertexBuffers[translucentIndex] != null)
				vertexBuffers[translucentIndex].deleteGlBuffers();
			vertexBuffers[translucentIndex] = new VertexBuffer(DefaultVertexFormats.BLOCK);
			vertexBuffers[translucentIndex].bufferData(builder.getByteBuffer());
		}
	}
	
	protected void processQueue() {
		for (int i = 0; i < queuedBuffers.length; i++) {
			if (queuedBuffers[i] != null && !queuedBuffers[i].isEmpty()) {
				int expand = 0;
				for (BufferHolder teBuffer : queuedBuffers[i])
					if (!teBuffer.isInvalid())
						expand += teBuffer.vertexCount();
					
				if (managers[i] == null || managers[i].getBuilder() == null) {
					BufferBuilder tempBuffer = new BufferBuilder(DefaultVertexFormats.BLOCK.getNextOffset() * expand + DefaultVertexFormats.BLOCK.getNextOffset());
					tempBuffer.begin(7, DefaultVertexFormats.BLOCK);
					tempBuffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
					managers[i] = new ChunkBlockLayerManager(tempBuffer, BlockRenderLayer.values()[i]);
				} else
					BufferBuilderUtils.growBufferSmall(managers[i].getBuilder(), managers[i].getBuilder().getVertexFormat().getNextOffset() * expand);
				
				BufferBuilder builder = managers[i].getBuilder();
				
				for (BufferHolder holder : queuedBuffers[i]) {
					if (holder.isInvalid())
						continue;
					int index = BufferBuilderUtils.getBufferSizeByte(builder);
					if (holder.getManager() != null)
						holder.getManager().backToRAM();
					managers[i].add(holder);
				}
				
				queuedBuffers[i].clear();
				bufferChanged[i] = true;
			}
		}
		
	}
	
	public void uploadBuffer() {
		synchronized (tileEntities) {
			if (modified) {
				
				for (int i = 0; i < vertexBuffers.length; i++) {
					if (vertexBuffers[i] != null)
						vertexBuffers[i].deleteGlBuffers();
					
					managers[i] = null;
					if (queuedBuffers[i] != null)
						queuedBuffers[i].clear();
				}
				modified = false;
				for (TileEntityLittleTiles te : tileEntities.values())
					addRenderDataInternal(te);
			}
			
			processQueue();
			
			for (int i = 0; i < bufferChanged.length; i++) {
				if (bufferChanged[i]) {
					if (vertexBuffers[i] != null)
						vertexBuffers[i].deleteGlBuffers();
					
					vertexBuffers[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
					vertexBuffers[i].bufferData(managers[i].getBuilder().getByteBuffer());
					
					bufferChanged[i] = false;
				}
			}
			
			if (complete) {
				for (int j = 0; j < managers.length; j++)
					if (j != BlockRenderLayer.TRANSLUCENT.ordinal() && managers[j] != null) {
						managers[j].readyUp();
						managers[j].bindBuffer(vertexBuffers[j]);
					}
				complete = false;
			}
			
			if (lastRenderIndex != LittleChunkDispatcher.currentRenderState) {
				Collection<TileEntityLittleTiles> temp = new ArrayList<>(tileEntities.values());
				tileEntities.clear();
				for (TileEntityLittleTiles te : temp)
					te.updateQuadCache(this);
			}
		}
	}
	
	public VertexBuffer getLayerBuffer(BlockRenderLayer layer) {
		return vertexBuffers[layer.ordinal()];
	}
	
	public void markCompleted() {
		synchronized (tileEntities) {
			complete = true;
		}
	}
	
	public void backToRAM() {
		synchronized (tileEntities) {
			for (int j = 0; j < managers.length; j++)
				if (j != BlockRenderLayer.TRANSLUCENT.ordinal() && managers[j] != null)
					managers[j].backToRAM();
		}
	}
	
	public void unload() {
		synchronized (tileEntities) {
			for (int i = 0; i < vertexBuffers.length; i++) {
				if (vertexBuffers[i] != null)
					vertexBuffers[i].deleteGlBuffers();
				managers[i] = null;
			}
			tileEntities.clear();
		}
	}
}
