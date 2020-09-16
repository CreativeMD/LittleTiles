package com.creativemd.littletiles.client.render.world;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL15;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBufferCache.BufferHolder;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUploader {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	// VertexBuffer
	private static Field vertexCountField = ReflectionHelper.findField(VertexBuffer.class, new String[] { "count", "field_177364_c" });
	private static Field bufferIdField = ReflectionHelper.findField(VertexBuffer.class, new String[] { "glBufferId", "field_177365_a" });
	private static Field formatField = ReflectionHelper.findField(VertexBuffer.class, new String[] { "vertexFormat", "field_177363_b" });
	
	public static int getBufferId(VertexBuffer buffer) {
		try {
			return bufferIdField.getInt(buffer);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
		return -1;
	}
	
	public static void uploadRenderData(RenderChunk chunk, List<TileEntityLittleTiles> tiles) {
		if ((FMLClientHandler.instance().hasOptifine() && OptifineHelper.isRenderRegions()) || !LittleTiles.CONFIG.rendering.uploadToVBODirectly)
			return;
		try {
			for (int i = 0; i < BlockRenderLayer.values().length; i++) {
				BlockRenderLayer layer = BlockRenderLayer.values()[i];
				
				int expanded = 0;
				
				for (TileEntityLittleTiles te : tiles) {
					BufferHolder holder = te.render.getBufferCache().get(layer);
					if (holder != null && !holder.isInvalid())
						expanded += holder.length();
				}
				
				try {
					ByteBuffer toUpload;
					
					if (expanded > 0) {
						CompiledChunk compiled = chunk.getCompiledChunk();
						VertexBuffer uploadBuffer = chunk.getVertexBufferByLayer(i);
						
						if (uploadBuffer == null)
							return;
						
						if (layer == BlockRenderLayer.TRANSLUCENT) {
							
							boolean empty = compiled.getState() == null || compiled.isLayerEmpty(BlockRenderLayer.TRANSLUCENT);
							BufferBuilder builder = new BufferBuilder((empty ? 0 : compiled.getState().getRawBuffer().length * 4) + expanded);
							
							builder.begin(7, DefaultVertexFormats.BLOCK);
							builder.setTranslation(-chunk.getPosition().getX(), -chunk.getPosition().getY(), -chunk.getPosition().getZ());
							
							if (!empty)
								builder.setVertexState(compiled.getState());
							
							for (TileEntityLittleTiles te : tiles) {
								BufferHolder holder = te.render.getBufferCache().get(layer);
								if (holder != null && !holder.isInvalid())
									holder.add(builder);
							}
							
							Entity entity = mc.getRenderViewEntity();
							float x = (float) entity.posX;
							float y = (float) entity.posY + entity.getEyeHeight();
							float z = (float) entity.posZ;
							builder.sortVertexData(x, y, z);
							compiled.setState(builder.getVertexState());
							builder.finishDrawing();
							
							toUpload = builder.getByteBuffer();
						} else {
							VertexFormat format = (VertexFormat) formatField.get(uploadBuffer);
							int uploadedVertexCount = vertexCountField.getInt(uploadBuffer);
							
							// Retrieve vanilla buffered data
							uploadBuffer.bindBuffer();
							boolean empty = compiled.isLayerEmpty(layer);
							ByteBuffer vanillaBuffer = empty ? null : glMapBufferRange(uploadedVertexCount * format.getNextOffset());
							uploadBuffer.unbindBuffer();
							
							toUpload = ByteBuffer.allocateDirect((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + expanded);
							if (vanillaBuffer != null)
								toUpload.put(vanillaBuffer);
							
							for (TileEntityLittleTiles te : tiles) {
								BufferHolder holder = te.render.getBufferCache().get(layer);
								if (holder != null && !holder.isInvalid()) {
									ByteBuffer buffer = holder.getBuffer();
									buffer.position(0);
									buffer.limit(holder.length());
									toUpload.put(buffer);
								}
							}
						}
						
						uploadBuffer.deleteGlBuffers();
						bufferIdField.setInt(uploadBuffer, OpenGlHelper.glGenBuffers());
						toUpload.position(0);
						uploadBuffer.bufferData(toUpload);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		} catch (NotSupportedException e) {
			
		}
	}
	
	private static Field arbVboField = ReflectionHelper.findField(OpenGlHelper.class, new String[] { "arbVbo", "field_176090_Y" });
	
	public static ByteBuffer glMapBufferRange(long length) throws NotSupportedException {
		try {
			ByteBuffer result = ByteBuffer.allocateDirect((int) length);
			if (arbVboField.getBoolean(null))
				ARBBufferObject.glGetBufferSubDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0, result);
			//return ARBVertexBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, ARBVertexBufferObject.GL_READ_ONLY_ARB, length, null);
			//else if (GLContext.getCapabilities().OpenGL30)
			//return GL30.glMapBufferRange(OpenGlHelper.GL_ARRAY_BUFFER, 0, length, GL30.GL_MAP_READ_BIT, null);
			//else if (OpenGlHelper.useVbo())
			else
				GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, result);
			return result;
		} catch (IllegalArgumentException | IllegalAccessException | IllegalStateException e) {
			if (e instanceof IllegalStateException)
				throw new NotSupportedException(e);
			else
				e.printStackTrace();
		}
		return null;
	}
	
	public static class NotSupportedException extends Exception {
		
		public NotSupportedException(Exception e) {
			super(e);
		}
		
	}
	
}