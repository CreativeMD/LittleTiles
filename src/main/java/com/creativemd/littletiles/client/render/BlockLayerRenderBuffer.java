package com.creativemd.littletiles.client.render;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockLayerRenderBuffer {
	
	private AtomicBoolean isDrawing = new AtomicBoolean(false);
	
	public synchronized void setDrawing() throws RenderOverlapException
	{
		if(isDrawing.get())
			throw new RenderOverlapException();
		isDrawing.set(true);
	}
	
	public synchronized void setFinishedDrawing()
	{
		isDrawing.set(false);
	}
	
	public synchronized boolean isDrawing()
	{
		return isDrawing.get();
	}
	
	public final VertexFormat format;
	
	public final int bufferSizePerQuad;
	
	public BlockLayerRenderBuffer() {
		this(DefaultVertexFormats.BLOCK);
	}
	
	public BlockLayerRenderBuffer(VertexFormat format) {
		this.format = format;
		bufferSizePerQuad = format.getNextOffset();
	}
	
	private BufferBuilder solid;
	private BufferBuilder cutout_mipped;
	private BufferBuilder cutout;
	private BufferBuilder translucent;
	
	public BufferBuilder createVertexBuffer(List<? extends RenderCubeObject> cubes)
	{
		int size = 1;
		for (RenderCubeObject cube : cubes) {
			size += cube.getQuads();
		}
		return new BufferBuilder(bufferSizePerQuad * size);
	}
	
	public BufferBuilder getBufferByLayer(BlockRenderLayer layer)
	{
		switch(layer)
		{
		case SOLID:
			return solid;
		case CUTOUT_MIPPED:
			return cutout_mipped;
		case CUTOUT:
			return cutout;
		case TRANSLUCENT:
			return translucent;
		}
		return null;
	}
	
	public void setBufferByLayer(BufferBuilder buffer, BlockRenderLayer layer)
	{
		switch(layer)
		{
		case SOLID:
			solid = buffer;
			break;
		case CUTOUT_MIPPED:
			cutout_mipped = buffer;
			break;
		case CUTOUT:
			cutout = buffer;
			break;
		case TRANSLUCENT:
			translucent = buffer;
			break;
		}
	}
	
	public void clear()
	{
		solid = null;
		cutout_mipped = null;
		cutout = null;
		translucent = null;		
	}
	
	public static class RenderOverlapException extends Exception{
		
		public RenderOverlapException() {
			super("Buffer is already rendering!");
		}
		
	}
	
}
