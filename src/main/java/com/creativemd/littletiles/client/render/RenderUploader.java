package com.creativemd.littletiles.client.render;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.util.glu.GLU;

import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.player.PlayerContainerEvent.Open;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUploader {
	
	private static ConcurrentHashMap<BlockPos, ChunkQueue> chunksToUpdate = new ConcurrentHashMap<>();
	
	//private static HashMap<RenderChunk, ArrayList<TileEntityLittleTiles>> directChunkUpdate = new HashMap<>();
	
	//ViewFrustum
	private static Field viewFrustumField;
	
	public static ViewFrustum getViewFrustum()
	{
		if(viewFrustumField == null)
			viewFrustumField = ReflectionHelper.findField(RenderGlobal.class, "viewFrustum", "field_175008_n");
		try {
			return (ViewFrustum) viewFrustumField.get(mc.renderGlobal);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//VertexBuffer
	private static Field vertexCountField = ReflectionHelper.findField(VertexBuffer.class, "count", "field_177364_c");
	private static Field bufferIdField = ReflectionHelper.findField(VertexBuffer.class, "glBufferId", "field_177365_a");
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static void addBlockForUpdate(TileEntityLittleTiles te, BlockPos chunkPos, boolean done)
	{
		synchronized (chunksToUpdate){
			ChunkQueue queue = chunksToUpdate.get(chunkPos);
			if(queue == null)
			{
				queue = new ChunkQueue();
				queue.shouldPushUpdate.set(done);
				chunksToUpdate.put(chunkPos, queue);
			}else if(!queue.shouldPushUpdate.get())
				queue.shouldPushUpdate.set(false);
			
			synchronized (queue.blocks){
				if(!queue.blocks.contains(te))
					queue.blocks.add(te);
			}
		}
		
	}
	
	public static void finishChunkUpdate(BlockPos pos)
	{
		synchronized (chunksToUpdate){
			ChunkQueue queue = chunksToUpdate.get(pos);
			if(queue != null)
				queue.shouldPushUpdate.set(true);
		}
	}
	
	public static void updateRenderData(RenderChunk chunk, Collection<TileEntityLittleTiles> tiles, VertexFormat format)
	{
		for (int i = 0; i < BlockRenderLayer.values().length; i++) {
			BlockRenderLayer layer = BlockRenderLayer.values()[i];
			ArrayList<ByteBuffer> buffers = new ArrayList<>();
			int bufferSize = 0;
			
			for (Iterator iterator = tiles.iterator(); iterator.hasNext();) {
				TileEntityLittleTiles te = (TileEntityLittleTiles) iterator.next();
				net.minecraft.client.renderer.VertexBuffer tempBuffer = te.getBuffer().getBufferByLayer(layer);
				if(tempBuffer != null)
				{
					buffers.add(tempBuffer.getByteBuffer());
					bufferSize += tempBuffer.getByteBuffer().limit();
				}
			}
			
			if(!buffers.isEmpty())
			{
				VertexBuffer layerBuffer = chunk.getVertexBufferByLayer(i);
				
				int bufferId = -1;
				try {
					bufferId = bufferIdField.getInt(layerBuffer);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
				int vertexCount = 0;
				
				try {
					vertexCount = vertexCountField.getInt(layerBuffer);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
				//Retrieve vanilla buffered data
				layerBuffer.bindBuffer();
				ByteBuffer vanillaBuffer = glMapBufferRange(OpenGlHelper.GL_ARRAY_BUFFER, vertexCount * format.getNextOffset(), GL30.GL_MAP_READ_BIT, null); //GL30.glMapBufferRange(OpenGlHelper.GL_ARRAY_BUFFER, 0, vertexCount * format.getNextOffset(), GL30.GL_MAP_READ_BIT, null);
				
				ByteBuffer overridenBuffer = null;
				if(vanillaBuffer != null){
					overridenBuffer = ByteBuffer.allocateDirect(vanillaBuffer.limit() + bufferSize);					
					overridenBuffer.put(vanillaBuffer);
					for (int j = 0; j < buffers.size(); j++) {
						buffers.get(j).position(0);
						overridenBuffer.put(buffers.get(j));
					}
				}
				
				layerBuffer.unbindBuffer();
				
				if(vanillaBuffer != null)
				{
					vanillaBuffer = null;
					layerBuffer.deleteGlBuffers();
					
					try {
						bufferIdField.setInt(layerBuffer, OpenGlHelper.glGenBuffers());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
					overridenBuffer.position(0);
					layerBuffer.bufferData(overridenBuffer);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event)
	{
		if(event.phase == Phase.START || event.phase == Phase.END)
		{
			World world = mc.theWorld;
			VertexFormat format = LittleTilesClient.getBlockVertexFormat();
			
			if(world != null)
			{
				/*if(!directChunkUpdate.isEmpty())
				{
					for (Entry<RenderChunk, ArrayList<TileEntityLittleTiles>> element : directChunkUpdate.entrySet()) {
						updateRenderData(element.getKey(), element.getValue(), format);
					}
					directChunkUpdate.clear();
				}*/
				synchronized (chunksToUpdate) {
					if(!chunksToUpdate.isEmpty())
					{
						ArrayList<BlockPos> deleted = new ArrayList<>();
						for (Entry<BlockPos, ChunkQueue> element : chunksToUpdate.entrySet()) {
							if(element.getValue().shouldPushUpdate.get())
							{
								synchronized (element.getValue().blocks) {
									updateRenderData(getRenderChunkByChunkPosition(getViewFrustum(), element.getKey()), element.getValue().blocks, format);
									deleted.add(element.getKey());
								}
							}
						}
						if(!deleted.isEmpty())
						{
							for (int i = 0; i < deleted.size(); i++) {
								chunksToUpdate.remove(deleted.get(i));
							}
						}
					}
				}
			}else if(world == null)
				chunksToUpdate.clear();
		}
	}
	
	private static Field arbVboField = ReflectionHelper.findField(OpenGlHelper.class, "arbVbo", "field_176090_Y");
	
	public static ByteBuffer glMapBufferRange(int target, long length, int access, ByteBuffer old_buffer)
    {
		
        try {
			if (arbVboField.getBoolean(null))
			{
			    return ARBVertexBufferObject.glMapBufferARB(target, access, length, old_buffer);
			}
			else
			{
			   return GL30.glMapBufferRange(target, 0, length, access, old_buffer);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        return null;
    }
	
	private static Field countChunksXField = ReflectionHelper.findField(ViewFrustum.class, "countChunksX", "field_178165_d");
	private static Field countChunksYField = ReflectionHelper.findField(ViewFrustum.class, "countChunksY", "field_178168_c");
	private static Field countChunksZField = ReflectionHelper.findField(ViewFrustum.class, "countChunksZ", "field_178166_e");
	
	public static BlockPos getRenderChunkPos(BlockPos pos)
	{
		int i = MathHelper.bucketInt(pos.getX(), 16);
        int j = MathHelper.bucketInt(pos.getY(), 16);
        int k = MathHelper.bucketInt(pos.getZ(), 16);
        return new BlockPos(i, j, k);
	}
	
	public static RenderChunk getRenderChunkByChunkPosition(ViewFrustum frustum, BlockPos chunkPos)
	{
		int i = chunkPos.getX();
        int j = chunkPos.getY();
        int k = chunkPos.getZ();
        
        int countChunksX = 0;
        
        try {
			countChunksX = countChunksXField.getInt(frustum);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
        int countChunksY = 0;
        
        try {
			countChunksY = countChunksYField.getInt(frustum);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
        int countChunksZ = 0;
        
        try {
			countChunksZ = countChunksZField.getInt(frustum);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
        if (j >= 0 && j < countChunksY)
        {
            i = i % countChunksX;

            if (i < 0)
            {
                i += countChunksX;
            }

            k = k % countChunksZ;

            if (k < 0)
            {
                k += countChunksZ;
            }

            int l = (k * countChunksY + j) * countChunksX + i;
            return frustum.renderChunks[l];
        }
        return null;
	}
	
	public static class ChunkQueue {
		
		public ConcurrentLinkedQueue<TileEntityLittleTiles> blocks = new ConcurrentLinkedQueue<>();
		public AtomicBoolean shouldPushUpdate = new AtomicBoolean(false);
		
		
	}

}
