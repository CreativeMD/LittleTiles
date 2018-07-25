package com.creativemd.littletiles.client.render.entity;

import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer;
import com.creativemd.littletiles.client.render.LittleChunkDispatcher;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LittleRenderChunk {
	
	public final BlockPos pos;
	protected final net.minecraft.client.renderer.vertex.VertexBuffer[] vertexBuffers = new net.minecraft.client.renderer.vertex.VertexBuffer[BlockRenderLayer.values().length];
	protected VertexBuffer[] tempBuffers = new VertexBuffer[BlockRenderLayer.values().length];
	
	protected List<VertexBuffer>[] queuedBuffers = new List[BlockRenderLayer.values().length];
	protected boolean[] bufferChanged = new boolean[BlockRenderLayer.values().length];
	
	public LittleRenderChunk(BlockPos pos) {
		this.pos = pos;
	}
	
	public int transparencySortedIndex = 0;
	
	public void addRenderData(TileEntityLittleTiles te)
	{
		BlockLayerRenderBuffer layers = te.getBuffer();
		if(layers != null)
		{
			for (int i = 0; i < BlockRenderLayer.values().length; i++) {
				BlockRenderLayer layer = BlockRenderLayer.values()[i];
				net.minecraft.client.renderer.VertexBuffer tempBuffer = layers.getBufferByLayer(layer);
				if(tempBuffer != null)
				{
					if(queuedBuffers[i] == null)
						queuedBuffers[i] = new ArrayList<>();
					
					queuedBuffers[i].add(tempBuffer);
				}
			}
		}
	}
	
	public void resortTransparency(int index, float x, float y, float z)
	{
		if(index == transparencySortedIndex)
			return ;
		
		this.transparencySortedIndex = index;
		
		int translucentIndex = BlockRenderLayer.TRANSLUCENT.ordinal();
		
		VertexBuffer builder = tempBuffers[translucentIndex];
		if(builder != null)
		{
			builder.sortVertexData(x, y, z);
			if(vertexBuffers[translucentIndex] != null)
				vertexBuffers[translucentIndex].deleteGlBuffers();
			vertexBuffers[translucentIndex] = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.BLOCK);
			vertexBuffers[translucentIndex].bufferData(tempBuffers[translucentIndex].getByteBuffer());
		}
	}
	
	protected void processQueue()
	{
		for (int i = 0; i < queuedBuffers.length; i++) {
			if(queuedBuffers[i] != null)
			{
				int expand = 0;
				for (VertexBuffer teBuffer : queuedBuffers[i]) {
					expand += teBuffer.getVertexCount();
				}
				
				VertexBuffer tempBuffer = tempBuffers[i];
				if(tempBuffer == null)
				{
					tempBuffer = new VertexBuffer(DefaultVertexFormats.BLOCK.getIntegerSize() * expand * 4);
					tempBuffer.begin(7, DefaultVertexFormats.BLOCK);
			        tempBuffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());
			        tempBuffers[i] = tempBuffer;
				}else {
					BufferBuilderUtils.growBuffer(tempBuffer, tempBuffer.getVertexFormat().getIntegerSize() * expand * 4);
				}
				
				for (VertexBuffer teBuffer : queuedBuffers[i]) {
					BufferBuilderUtils.addBuffer(tempBuffer, teBuffer);					
				}
				
				queuedBuffers[i] = null;				
				bufferChanged[i] = true;
			}
		}
		
	}
	
	public void uploadBuffer()
	{
		processQueue();
		
		for (int i = 0; i < bufferChanged.length; i++) {
			if(bufferChanged[i])
			{
				if(vertexBuffers[i] != null)
					vertexBuffers[i].deleteGlBuffers();
				
				vertexBuffers[i] = new net.minecraft.client.renderer.vertex.VertexBuffer(DefaultVertexFormats.BLOCK);
				vertexBuffers[i].bufferData(tempBuffers[i].getByteBuffer());
				
				bufferChanged[i] = false;
			}
		}
	}
	
	public net.minecraft.client.renderer.vertex.VertexBuffer getLayerBuffer(BlockRenderLayer layer)
	{
		return vertexBuffers[layer.ordinal()];
	}
	
	public void markCompleted()
	{
		for (int j = 0; j < tempBuffers.length; j++)
			if(BlockRenderLayer.values()[j] != BlockRenderLayer.TRANSLUCENT)
				tempBuffers[j] = null;
	}
	
	public void unload()
	{
		for (int i = 0; i < vertexBuffers.length; i++) {
			if(vertexBuffers[i] != null)
				vertexBuffers[i].deleteGlBuffers();
		}
	}
	
}
