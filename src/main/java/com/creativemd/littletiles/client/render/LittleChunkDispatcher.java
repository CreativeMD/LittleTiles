package com.creativemd.littletiles.client.render;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.optifine.OptifineVertexBuffer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator.Type;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadersmod.client.SVertexBuilder;

public class LittleChunkDispatcher extends ChunkRenderDispatcher {

	public LittleChunkDispatcher() {
		super();
	}
	
	private static Method growBuffer = ReflectionHelper.findMethod(VertexBuffer.class, null, new String[]{"growBuffer",  "func_181670_b"}, int.class);
	
	private static Field rawIntBufferField = ReflectionHelper.findField(VertexBuffer.class, "rawIntBuffer", "field_178999_b");
	private static Field vertexCountField = ReflectionHelper.findField(VertexBuffer.class, "vertexCount", "field_178997_d");
	
	private static Method setLayerUseMethod = ReflectionHelper.findMethod(CompiledChunk.class, null, new String[]{"setLayerUsed", "func_178486_a"}, BlockRenderLayer.class);
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	@Override
	public ListenableFuture<Object> uploadChunk(final BlockRenderLayer layer, final VertexBuffer buffer, final RenderChunk chunk, final CompiledChunk compiled, final double p_188245_5_)
    {
		List<TileEntity> tileEntities = compiled.getTileEntities();
		int bufferExpand = 0;
		List<TileEntityLittleTiles> tiles = new ArrayList<>();
		for (int i = 0; i < tileEntities.size(); i++) {
			TileEntity te = tileEntities.get(i);
			
			if(te instanceof TileEntityLittleTiles)
			{
				if(layer == BlockRenderLayer.SOLID)
					((TileEntityLittleTiles) te).updateQuadCache(chunk);
				
				BlockLayerRenderBuffer blockLayerBuffer = ((TileEntityLittleTiles) te).getBuffer();
				if(blockLayerBuffer != null)
				{
					VertexBuffer teBuffer = blockLayerBuffer.getBufferByLayer(layer);
					if(teBuffer != null && (layer != BlockRenderLayer.TRANSLUCENT || !((TileEntityLittleTiles) te).getBeenAddedToBuffer().get()))
					{
						bufferExpand += teBuffer.getByteBuffer().limit();
						tiles.add((TileEntityLittleTiles) te);
						if(layer == BlockRenderLayer.TRANSLUCENT)
							((TileEntityLittleTiles) te).getBeenAddedToBuffer().set(true);
					}
				}
			}
		}
		
		
		if(bufferExpand > 0)
		{			
			if(compiled.isLayerEmpty(layer))
				try {
					if(compiled != CompiledChunk.DUMMY)
						setLayerUseMethod.invoke(compiled, layer);
					if(chunk.getCompiledChunk() != CompiledChunk.DUMMY)
						setLayerUseMethod.invoke(chunk.getCompiledChunk(), layer);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
			
			if(buffer.getVertexFormat() != null && !(compiled.getState() instanceof LittleVertexBufferState))
			{				
				for (int i = 0; i < tiles.size(); i++) {
					TileEntityLittleTiles te = tiles.get(i);
					BlockLayerRenderBuffer blockLayerBuffer = ((TileEntityLittleTiles) te).getBuffer();
					VertexBuffer teBuffer = blockLayerBuffer.getBufferByLayer(layer);
					if(teBuffer != null)
					{
						int size = teBuffer.getVertexFormat().getIntegerSize() * teBuffer.getVertexCount();
						try {
							IntBuffer rawIntBuffer = (IntBuffer) rawIntBufferField.get(teBuffer);
							rawIntBuffer.rewind();
							rawIntBuffer.limit(size);
							int[] data = new int[size];
							rawIntBuffer.get(data);
							
							growBuffer.invoke(buffer, data.length * 4);
							IntBuffer chunkIntBuffer = (IntBuffer) rawIntBufferField.get(buffer);
					        chunkIntBuffer.position(buffer.getVertexFormat().getIntegerSize() * buffer.getVertexCount());
					        chunkIntBuffer.put(data);
					        vertexCountField.setInt(buffer, vertexCountField.getInt(buffer) + data.length / buffer.getVertexFormat().getIntegerSize());
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				if(layer == BlockRenderLayer.TRANSLUCENT)
				{
					Entity entity = mc.getRenderViewEntity();
					float x = (float)entity.posX;
		            float y = (float)entity.posY + entity.getEyeHeight();
		            float z = (float)entity.posZ;
		            
					buffer.sortVertexData(x, y, z);
					compiled.setState(new LittleVertexBufferState(buffer, buffer.getVertexState()));
				}
				
				buffer.getByteBuffer().position(0);
				buffer.getByteBuffer().limit(buffer.getVertexFormat().getIntegerSize() * buffer.getVertexCount() * 4);
			}
		}
		return super.uploadChunk(layer, buffer, chunk, compiled, p_188245_5_);
    }
	
	public static class LittleVertexBufferState extends VertexBuffer.State {

		public LittleVertexBufferState(VertexBuffer buffer, VertexBuffer.State state) {
			buffer.super(state.getRawBuffer(), state.getVertexFormat());
		}
		
		
	}
}
