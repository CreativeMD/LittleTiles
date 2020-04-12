package com.creativemd.littletiles.client.render.world;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.BufferBuilderUtils;
import com.creativemd.littletiles.client.render.cache.BlockLayerRenderBuffer;
import com.creativemd.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class LittleChunkDispatcher {
	
	public static int currentRenderIndex = Integer.MIN_VALUE;
	
	public static void onReloadRenderers(RenderGlobal renderGlobal) {
		if (mc.renderGlobal == renderGlobal) {
			currentRenderIndex++;
			if (mc.world != null)
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
	
	private static Method setLayerUseMethod = ReflectionHelper.findMethod(CompiledChunk.class, "setLayerUsed", "func_178486_a", BlockRenderLayer.class);
	
	private static Field setTileEntities = ReflectionHelper.findField(RenderChunk.class, new String[] { "setTileEntities", "field_181056_j" });
	
	private static Field littleTiles = ReflectionHelper.findField(RenderChunk.class, "littleTiles");
	private static Field updateQueue = ReflectionHelper.findField(RenderChunk.class, "updateQueue");
	private static Field dynamicLightUpdate = ReflectionHelper.findField(RenderChunk.class, "dynamicLightUpdate");
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static void addTileEntity(List<TileEntityLittleTiles> tiles, TileEntity te) {
		if (te instanceof TileEntityLittleTiles)
			tiles.add((TileEntityLittleTiles) te);
	}
	
	public static void onDoneRendering(RenderChunk chunk, List<TileEntityLittleTiles> tiles) {
		try {
			littleTiles.set(chunk, tiles);
			updateQueue.setInt(chunk, updateQueue.getInt(chunk) + 1);
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
	
	public static void uploadChunk(final BlockRenderLayer layer, final BufferBuilder buffer, final RenderChunk chunk, final CompiledChunk compiled, final double p_188245_5_) {
		try {
			if (added.getBoolean(buffer))
				return;
		} catch (IllegalArgumentException | IllegalAccessException e2) {
			e2.printStackTrace();
		}
		
		if (buffer.getVertexFormat() != null && (layer != BlockRenderLayer.TRANSLUCENT || (compiled.getState() != emptyState && !(compiled.getState() instanceof LittleVertexBufferState)))) {
			List<TileEntityLittleTiles> tiles = getLittleTE(chunk);
			
			if (tiles == null || tiles.isEmpty()) {
				try {
					added.setBoolean(buffer, true);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				return;
			}
			
			int expanded = 0;
			
			boolean dynamicUpdate = false;
			try {
				if (layer == BlockRenderLayer.SOLID)
					dynamicUpdate = dynamicLightUpdate.getBoolean(chunk);
			} catch (IllegalArgumentException | IllegalAccessException e2) {
				e2.printStackTrace();
			}
			
			if (!tiles.isEmpty()) {
				for (TileEntityLittleTiles te : tiles) {
					if (layer == BlockRenderLayer.SOLID) {
						if (dynamicUpdate)
							te.hasLightChanged = true;
						
						te.updateQuadCache(chunk);
					}
					
					BlockLayerRenderBuffer blockLayerBuffer = te.buffer;
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
				
				BufferBuilderUtils.growBufferSmall(buffer, buffer.getVertexFormat().getNextOffset() * expanded + buffer.getVertexFormat().getNextOffset());
				
				for (TileEntityLittleTiles te : tiles) {
					BlockLayerRenderBuffer blockLayerBuffer = te.buffer;
					if (blockLayerBuffer == null)
						continue;
					BufferBuilder teBuffer = blockLayerBuffer.getBufferByLayer(layer);
					if (teBuffer != null)
						BufferBuilderUtils.addBuffer(buffer, teBuffer);
				}
				
				if (layer == BlockRenderLayer.TRANSLUCENT && buffer.getVertexFormat() != null && mc.getRenderViewEntity() != null) {
					Entity entity = mc.getRenderViewEntity();
					float x = (float) entity.posX;
					float y = (float) entity.posY + entity.getEyeHeight();
					float z = (float) entity.posZ;
					
					BlockPos pos = chunk.getPosition();
					buffer.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
					
					buffer.sortVertexData(x, y, z);
					compiled.setState(new LittleVertexBufferState(buffer, buffer.getVertexState()));
				}
				
				buffer.getByteBuffer().position(0);
				buffer.getByteBuffer().limit(buffer.getVertexFormat().getNextOffset() * buffer.getVertexCount());
				
				try {
					added.setBoolean(buffer, true);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
				LittleTilesProfilerOverlay.uploaded++;
			}
		}
		
		int index = layer.ordinal(); // Check if another layer needs to be added if yes abort
		while (index < 3) {
			index++;
			if (compiled.isLayerStarted(BlockRenderLayer.values()[index]))
				return;
		}
		
		try {
			updateQueue.setInt(chunk, updateQueue.getInt(chunk) - 1);
			if (updateQueue.getInt(chunk) == 0)
				littleTiles.set(chunk, null); // Clear LTTiles cache
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
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
			worldRendererIn.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
			
			worldRendererIn.setVertexState(compiledchunk.getState());
			
			worldRendererIn.sortVertexData(x, y, z);
			compiledchunk.setState(new LittleVertexBufferState(worldRendererIn, worldRendererIn.getVertexState()));
			
			worldRendererIn.finishDrawing();
		} else
			compiledchunk.setState(emptyState);
		
	}
	
	private static float minAbs(float var0, float var1) {
		return Math.abs(var0) > Math.abs(var1) ? var1 : var0;
	}
	
	private static float closest(float to, float pos0, float pos1, float pos2, float pos3) {
		float f = pos0 - to;
		float f1 = pos1 - to;
		float f2 = pos2 - to;
		float f3 = pos3 - to;
		if ((f > 0 || f1 > 0 || f2 > 0 || f3 > 0) && (f < 0 || f1 < 0 || f2 < 0 || f3 < 0))
			return 0;
		return minAbs(f, minAbs(f1, minAbs(f2, f3)));
	}
	
	public static float getDistanceSq(FloatBuffer buffer, float x, float y, float z, int size, int index) {
		float f = buffer.get(index + 0);
		float f1 = buffer.get(index + 1);
		float f2 = buffer.get(index + 2);
		
		float f3 = buffer.get(index + size + 0);
		float f4 = buffer.get(index + size + 1);
		float f5 = buffer.get(index + size + 2);
		
		float f6 = buffer.get(index + size * 2 + 0);
		float f7 = buffer.get(index + size * 2 + 1);
		float f8 = buffer.get(index + size * 2 + 2);
		
		float f9 = buffer.get(index + size * 3 + 0);
		float f10 = buffer.get(index + size * 3 + 1);
		float f11 = buffer.get(index + size * 3 + 2);
		
		float closeX = closest(x, f, f3, f6, f9);
		float closeY = closest(y, f1, f4, f7, f10);
		float closeZ = closest(z, f2, f5, f8, f11);
		return closeX * closeX + closeY * closeY + closeZ * closeZ;
	}
	
	public static class LittleVertexBufferState extends BufferBuilder.State {
		
		public LittleVertexBufferState(BufferBuilder buffer, BufferBuilder.State state) {
			buffer.super(state.getRawBuffer(), state.getVertexFormat());
		}
		
	}
}
