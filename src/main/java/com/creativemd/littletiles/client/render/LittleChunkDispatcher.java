package com.creativemd.littletiles.client.render;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class LittleChunkDispatcher extends ChunkRenderDispatcher {
	
	public static AtomicInteger currentRenderIndex = new AtomicInteger(0);
	
	public static void onReloadRenderers(RenderGlobal renderGlobal) {
		if (mc.renderGlobal == renderGlobal) {
			currentRenderIndex.incrementAndGet();
			mc.world.addEventListener(new LightChangeEventListener());
		}
	}
	
	public static void onOptifineMarksChunkRenderUpdateForDynamicLights(RenderChunk chunk) {
		try {
			dynamicLightUpdate.setBoolean(chunk, true);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public LittleChunkDispatcher() {
		super();
	}
	
	private static Method setLayerUseMethod = ReflectionHelper.findMethod(CompiledChunk.class, "setLayerUsed", "func_178486_a", BlockRenderLayer.class);
	
	private static Field setTileEntities = ReflectionHelper.findField(RenderChunk.class, "setTileEntities", "field_181056_j");
	
	private static Field littleTiles = ReflectionHelper.findField(RenderChunk.class, "littleTiles");
	private static Field dynamicLightUpdate = ReflectionHelper.findField(RenderChunk.class, "dynamicLightUpdate");
	
	// private static Field tempLittleTiles =
	// ReflectionHelper.findField(RenderChunk.class, "tempLittleTiles");
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	/*
	 * public static void onStartRendering(RenderChunk chunk) {
	 * List<TileEntityLittleTiles> tiles = new ArrayList<>(); try {
	 * tempLittleTiles.set(chunk, new ArrayList<>()); } catch
	 * (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace();
	 * } }
	 */
	
	public static void addTileEntity(List<TileEntityLittleTiles> tiles, TileEntity te) {
		if (te instanceof TileEntityLittleTiles) {
			tiles.add((TileEntityLittleTiles) te);
			/*
			 * try { ((ArrayList<TileEntityLittleTiles>)
			 * tempLittleTiles.get(chunk)).add((TileEntityLittleTiles) te); } catch
			 * (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace();
			 * }
			 */
		}
	}
	
	public static void onDoneRendering(RenderChunk chunk, List<TileEntityLittleTiles> tiles) {
		try {
			littleTiles.set(chunk, tiles);
			// littleTiles.set(chunk, tempLittleTiles.get(chunk));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
	public static List<TileEntityLittleTiles> getLittleTE(RenderChunk chunk) {
		try {
			return (List<TileEntityLittleTiles>) littleTiles.get(chunk);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Field added = ReflectionHelper.findField(BufferBuilder.class, "littleTilesAdded");
	
	@Override
	public ListenableFuture<Object> uploadChunk(final BlockRenderLayer layer, final BufferBuilder buffer, final RenderChunk chunk, final CompiledChunk compiled, final double p_188245_5_) {
		try {
			if (added.getBoolean(buffer))
				return super.uploadChunk(layer, buffer, chunk, compiled, p_188245_5_);
		} catch (IllegalArgumentException | IllegalAccessException e2) {
			e2.printStackTrace();
		}
		
		if (buffer.getVertexFormat() != null && (layer != BlockRenderLayer.TRANSLUCENT || (compiled.getState() != emptyState && !(compiled.getState() instanceof LittleVertexBufferState)))) {
			// System.out.println("LittleTiles updating " + chunk.boundingBox + " layer=" +
			// layer + " state=" + compiled.getState());
			List<TileEntityLittleTiles> tiles = getLittleTE(chunk);
			
			if (tiles == null)
				tiles = new ArrayList<>();
			
			int expanded = 0;
			
			// long time = System.currentTimeMillis();
			boolean dynamicUpdate = false;
			try {
				if (layer == BlockRenderLayer.SOLID)
					dynamicUpdate = dynamicLightUpdate.getBoolean(chunk);
			} catch (IllegalArgumentException | IllegalAccessException e2) {
				e2.printStackTrace();
			}
			
			if (!tiles.isEmpty()) {
				for (Iterator<TileEntityLittleTiles> iterator = tiles.iterator(); iterator.hasNext();) {
					TileEntityLittleTiles te = iterator.next();
					
					if (layer == BlockRenderLayer.SOLID) {
						if (dynamicUpdate)
							te.hasLightChanged = true;
						
						((TileEntityLittleTiles) te).updateQuadCache(chunk);
					}
					
					BlockLayerRenderBuffer blockLayerBuffer = ((TileEntityLittleTiles) te).getBuffer();
					if (blockLayerBuffer != null) {
						BufferBuilder teBuffer = blockLayerBuffer.getBufferByLayer(layer);
						if (teBuffer != null)
							expanded += teBuffer.getVertexCount();
					}
				}
			}
			
			try {
				if (layer == BlockRenderLayer.SOLID)
					dynamicLightUpdate.setBoolean(chunk, false);
			} catch (IllegalArgumentException | IllegalAccessException e2) {
				e2.printStackTrace();
			}
			
			// System.out.println("First: " + (System.currentTimeMillis() - time));
			
			if (expanded > 0) {
				if (compiled.isLayerEmpty(layer))
					try {
						if (compiled != CompiledChunk.DUMMY)
							setLayerUseMethod.invoke(compiled, layer);
						if (chunk.getCompiledChunk() != CompiledChunk.DUMMY)
							setLayerUseMethod.invoke(chunk.getCompiledChunk(), layer);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						e1.printStackTrace();
					}
				
				BufferBuilderUtils.growBuffer(buffer, buffer.getVertexFormat().getIntegerSize() * expanded * 4);
				
				for (Iterator<TileEntityLittleTiles> iterator = tiles.iterator(); iterator.hasNext();) {
					TileEntityLittleTiles te = iterator.next();
					BlockLayerRenderBuffer blockLayerBuffer = ((TileEntityLittleTiles) te).getBuffer();
					if (blockLayerBuffer == null)
						continue;
					BufferBuilder teBuffer = blockLayerBuffer.getBufferByLayer(layer);
					if (teBuffer != null)
						BufferBuilderUtils.addBuffer(buffer, teBuffer);
				}
				
				if (layer == BlockRenderLayer.TRANSLUCENT && buffer.getVertexFormat() != null) {
					Entity entity = mc.getRenderViewEntity();
					float x = (float) entity.posX;
					float y = (float) entity.posY + entity.getEyeHeight();
					float z = (float) entity.posZ;
					
					buffer.sortVertexData(x, y, z);
					// System.out.println("Updating little sorting");
					compiled.setState(new LittleVertexBufferState(buffer, buffer.getVertexState()));
				}
				
				buffer.getByteBuffer().position(0);
				buffer.getByteBuffer().limit(buffer.getVertexFormat().getIntegerSize() * buffer.getVertexCount() * 4);
				
				try {
					added.setBoolean(buffer, true);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				// System.out.println("Second: " + (System.currentTimeMillis() - time));
			}
		}
		return super.uploadChunk(layer, buffer, chunk, compiled, p_188245_5_);
	}
	
	public static BufferBuilder.State emptyState = loadEmptyState();
	
	private static BufferBuilder.State loadEmptyState() {
		BufferBuilder buffer = new BufferBuilder(0);
		buffer.begin(7, DefaultVertexFormats.BLOCK);
		BufferBuilder.State state = buffer.getVertexState();
		buffer.finishDrawing();
		return state;
	}
	
	public static void resortTransparency(RenderChunk chunk, float x, float y, float z, ChunkCompileTaskGenerator generator) {
		CompiledChunk compiledchunk = generator.getCompiledChunk();
		
		if (compiledchunk.getState() != null && !compiledchunk.isLayerEmpty(BlockRenderLayer.TRANSLUCENT)) {
			BufferBuilder worldRendererIn = generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT);
			BlockPos pos = chunk.getPosition();
			worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
			worldRendererIn.setTranslation((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));
			
			generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT).setVertexState(compiledchunk.getState());
			
			worldRendererIn.sortVertexData(x, y, z);
			compiledchunk.setState(new LittleVertexBufferState(worldRendererIn, worldRendererIn.getVertexState()));
			
			worldRendererIn.finishDrawing();
		} else
			compiledchunk.setState(emptyState);
		
	}
	
	public static class LittleVertexBufferState extends BufferBuilder.State {
		
		public LittleVertexBufferState(BufferBuilder buffer, BufferBuilder.State state) {
			buffer.super(state.getRawBuffer(), state.getVertexFormat());
		}
		
	}
}
