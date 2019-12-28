package com.creativemd.littletiles.client.render.world;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL30;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
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
	
	public static void uploadRenderData(RenderChunk chunk, List<TileEntityLittleTiles> tiles) {
		for (int i = 0; i < BlockRenderLayer.values().length; i++) {
			BlockRenderLayer layer = BlockRenderLayer.values()[i];
			
			int expanded = 0;
			
			for (TileEntityLittleTiles te : tiles) {
				if (te.getBuffer() == null)
					continue;
				
				BufferBuilder teBufferBuilder = te.getBuffer().getBufferByLayer(layer);
				if (teBufferBuilder != null)
					expanded += teBufferBuilder.getByteBuffer().limit();
			}
			
			try {
				ByteBuffer toUpload;
				
				if (expanded > 0) {
					CompiledChunk compiled = chunk.getCompiledChunk();
					VertexBuffer uploadBuffer = chunk.getVertexBufferByLayer(i);
					
					if (layer == BlockRenderLayer.TRANSLUCENT) {
						
						boolean empty = compiled.getState() == null || compiled.isLayerEmpty(BlockRenderLayer.TRANSLUCENT);
						BufferBuilder builder = new BufferBuilder((empty ? 0 : compiled.getState().getRawBuffer().length * 4) + expanded);
						
						builder.begin(7, DefaultVertexFormats.BLOCK);
						builder.setTranslation(-chunk.getPosition().getX(), -chunk.getPosition().getY(), -chunk.getPosition().getZ());
						
						if (!empty)
							builder.setVertexState(compiled.getState());
						
						for (TileEntityLittleTiles te : tiles) {
							if (te.getBuffer() == null)
								continue;
							
							BufferBuilder teBufferBuilder = te.getBuffer().getBufferByLayer(layer);
							if (teBufferBuilder != null)
								BufferBuilderUtils.addBuffer(builder, teBufferBuilder);
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
						ByteBuffer vanillaBuffer = empty ? null : glMapBufferRange(OpenGlHelper.GL_ARRAY_BUFFER, uploadedVertexCount * format.getNextOffset(), GL30.GL_MAP_READ_BIT, null);
						uploadBuffer.unbindBuffer();
						
						if (vanillaBuffer == null)
							empty = true;
						
						toUpload = ByteBuffer.allocateDirect((empty ? 0 : vanillaBuffer.limit()) + expanded);
						if (!empty)
							toUpload.put(vanillaBuffer);
						
						vanillaBuffer = null;
						
						for (TileEntityLittleTiles te : tiles) {
							if (te.getBuffer() == null)
								continue;
							
							BufferBuilder teBufferBuilder = te.getBuffer().getBufferByLayer(layer);
							if (teBufferBuilder != null) {
								ByteBuffer buffer = teBufferBuilder.getByteBuffer();
								buffer.position(0);
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
	}
	
	private static Field arbVboField = ReflectionHelper.findField(OpenGlHelper.class, new String[] { "arbVbo", "field_176090_Y" });
	
	public static ByteBuffer glMapBufferRange(int target, long length, int access, ByteBuffer old_buffer) {
		
		try {
			if (arbVboField.getBoolean(null)) {
				return ARBVertexBufferObject.glMapBufferARB(target, access, length, old_buffer);
			} else {
				return GL30.glMapBufferRange(target, 0, length, access, old_buffer);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}