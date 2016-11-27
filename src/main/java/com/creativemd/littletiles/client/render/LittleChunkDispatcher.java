package com.creativemd.littletiles.client.render;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class LittleChunkDispatcher extends ChunkRenderDispatcher {

	public LittleChunkDispatcher() {
		super();
	}
	
	//private Field vertexCountField = ReflectionHelper.findField(VertexBuffer.class, "vertexCount", "field_178997_d");
	private Field rawIntBufferField = ReflectionHelper.findField(VertexBuffer.class, "rawIntBuffer", "field_178999_b");
	/*private Field rawShortBufferField = ReflectionHelper.findField(VertexBuffer.class, "rawShortBuffer", "field_181676_c");
	private Field rawFloatBufferField = ReflectionHelper.findField(VertexBuffer.class, "rawFloatBuffer", "field_179000_c");
	private Field byteBufferField = ReflectionHelper.findField(VertexBuffer.class, "byteBuffer", "field_179001_a");
	private Field vertexFormatField = ReflectionHelper.findField(VertexBuffer.class, "vertexFormat", "field_179011_q");*/
	
	private Method setLayerUseMethod = ReflectionHelper.findMethod(CompiledChunk.class, null, new String[]{"setLayerUsed", "func_178486_a"}, BlockRenderLayer.class);
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	/*private static Field queueChunkUploadsField = ReflectionHelper.findField(ChunkRenderDispatcher.class, "queueChunkUploads", "field_178524_h");
	private static Class pendingUploadClass = ReflectionHelper.getClass(LittleChunkDispatcher.class.getClassLoader(), "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$PendingUpload", "bqt$a");
	
	private static Constructor pendingUploadConstructor = findPendingUploadConstructor();
			
	private static Constructor findPendingUploadConstructor()
	{
		try {
			Constructor constructor = pendingUploadClass.getConstructors()[0]; //.getConstructor(ListenableFutureTask.<Object.class>.class, double.class);
			constructor.setAccessible(true);
			return constructor;
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}*/
	
	
	/*public ListenableFuture<Object> uploadChunk(final ChunkCompileTaskGenerator.Type type, final BlockRenderLayer layer, final VertexBuffer buffer, final RenderChunk chunk, final CompiledChunk compiled, final double p_188245_5_)
    {
		if(Minecraft.getMinecraft().isCallingFromMinecraftThread())
		{
			if(type != Type.RESORT_TRANSPARENCY)
				return uploadChunk(layer, buffer, chunk, compiled, p_188245_5_);
			else{
				return super.uploadChunk(layer, buffer, chunk, compiled, p_188245_5_);
			}
		}
		else
        {
            ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.<Object>create(new Runnable()
            {
                public void run()
                {
                    uploadChunk(type, layer, buffer, chunk, compiled, p_188245_5_);
                }
            }, (Object)null);
            Queue queueChunkUploads;
			try {
				queueChunkUploads = (Queue) queueChunkUploadsField.get(this);
				synchronized (queueChunkUploads)
	            {
	            	Object object = pendingUploadConstructor.newInstance(this, listenablefuturetask, p_188245_5_);
	            	//queueChunkUploads.add(new ChunkRenderDispatcher.PendingUpload(listenablefuturetask, p_188245_5_));
	            	queueChunkUploads.add(object);
	                
	            }
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
				e.printStackTrace();
			}
            
			return listenablefuturetask;
        }
    }*/
	
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
				BlockLayerRenderBuffer blockLayerBuffer = ((TileEntityLittleTiles) te).getBuffer();
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
		
		
		if(bufferExpand > 0)
		{
			/*if(compiled.getState() != null && buffer.getVertexFormat() == null)
			{
				buffer.begin(7, DefaultVertexFormats.BLOCK);
				BlockPos pos = chunk.getPosition();
				buffer.setTranslation((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()));
				buffer.setVertexState(compiled.getState());
				//buffer.getByteBuffer().limit(compiled.getState().getRawBuffer().length * 4);
			}*/
			
			/*ByteBuffer vanillaBuffer = buffer.getByteBuffer();
			
			if(vanillaBuffer.capacity() < vanillaBuffer.limit() + bufferExpand)
			{
				ByteBuffer tempBuffer = ByteBuffer.allocateDirect(vanillaBuffer.limit()+bufferExpand);
				tempBuffer.put(vanillaBuffer);
				tempBuffer.limit(vanillaBuffer.limit());
				try {
					byteBufferField.set(buffer, tempBuffer);
					rawIntBufferField.set(buffer, tempBuffer.asIntBuffer());
					rawFloatBufferField.set(buffer, tempBuffer.asFloatBuffer());
					rawShortBufferField.set(buffer, tempBuffer.asShortBuffer());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				vanillaBuffer = tempBuffer;
			}*/
			
			if(compiled.isLayerEmpty(layer))
				try {
					if(compiled != CompiledChunk.DUMMY)
						setLayerUseMethod.invoke(compiled, layer);
					if(chunk.getCompiledChunk() != CompiledChunk.DUMMY)
						setLayerUseMethod.invoke(chunk.getCompiledChunk(), layer);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
			
			if(buffer.getVertexFormat() != null)
			{
				//System.out.println(buffer.getVertexFormat());
				
				for (int i = 0; i < tiles.size(); i++) {
					TileEntityLittleTiles te = tiles.get(i);
					BlockLayerRenderBuffer blockLayerBuffer = ((TileEntityLittleTiles) te).getBuffer();
					VertexBuffer teBuffer = blockLayerBuffer.getBufferByLayer(layer);
					if(teBuffer != null)
					{
						//System.out.println(teBuffer.getVertexFormat());
						/*vanillaBuffer.position(vanillaBuffer.limit());
						vanillaBuffer.limit(vanillaBuffer.limit() + teBuffer.getByteBuffer().limit());
						teBuffer.getByteBuffer().position(0);
						vanillaBuffer.put(teBuffer.getByteBuffer());
						
						try {
							vertexCountField.set(buffer, vertexCountField.getInt(buffer) + vertexCountField.getInt(teBuffer));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}*/
						int size = teBuffer.getVertexFormat().getIntegerSize() * teBuffer.getVertexCount();
						try {
							IntBuffer rawIntBuffer = (IntBuffer) rawIntBufferField.get(teBuffer);
							rawIntBuffer.rewind();
							rawIntBuffer.limit(size);
							int[] data = new int[size];
							rawIntBuffer.get(data);
							buffer.addVertexData(data);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
				
				//vanillaBuffer.position(0);
				
				if(layer == BlockRenderLayer.TRANSLUCENT)
				{
					Entity entity = mc.getRenderViewEntity();
					float x = (float)entity.posX;
		            float y = (float)entity.posY + entity.getEyeHeight();
		            float z = (float)entity.posZ;
		            
					buffer.sortVertexData(x, y, z);
					
					compiled.setState(buffer.getVertexState());
				}
				
				/*if(compiled.getState() != null && buffer.getVertexFormat() == null)
				{
					buffer.finishDrawing();
					compiled.setState(buffer.getVertexState());
				}else{*/
					buffer.getByteBuffer().position(0);
					buffer.getByteBuffer().limit(buffer.getVertexFormat().getIntegerSize() * buffer.getVertexCount() * 4);
				//}
			}
		}
		return super.uploadChunk(layer, buffer, chunk, compiled, p_188245_5_);
    }
}
