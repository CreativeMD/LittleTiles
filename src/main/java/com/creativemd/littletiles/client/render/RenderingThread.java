package com.creativemd.littletiles.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.CreativeCubeConsumer;
import com.creativemd.creativecore.common.world.IBlockAccessFake;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.LittleTilesConfig;
import com.creativemd.littletiles.client.profile.LittleTilesProfiler;
import com.creativemd.littletiles.client.render.BlockLayerRenderBuffer.RenderOverlapException;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.SVertexBuilder;

@SideOnly(Side.CLIENT)
public class RenderingThread extends Thread {
	
	private static final String[] fakeWorldMods = new String[] { "chisel" };
	
	public static List<RenderingThread> threads;
	
	private static int threadIndex;
	
	public static synchronized RenderingThread getNextThread() {
		synchronized (threads) {
			RenderingThread thread = threads.get(threadIndex);
			threadIndex++;
			if (threadIndex >= threads.size())
				threadIndex = 0;
			return thread;
		}
	}
	
	public static void initThreads(int count) {
		if (count <= 0)
			throw new IllegalArgumentException("count has to be at least equal or greater than one");
		if (threads != null) {
			for (RenderingThread thread : threads) {
				thread.active = false;
			}
			
			for (RenderingThread thread : threads) {
				int i = 0;
				while (thread.isAlive() && i < 10000) {
					i++;
					try {
						sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				while (thread.updateCoords.size() > 0)
					thread.updateCoords.poll().te.resetRenderingState();
			}
		}
		threadIndex = 0;
		threads = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			threads.add(new RenderingThread(i));
		}
	}
	
	public static HashMap<Object, AtomicInteger> chunks = new HashMap<>();
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static void addCoordToUpdate(TileEntityLittleTiles te) {
		RenderingThread renderer = getNextThread();
		
		Object chunk;
		if (te.getWorld() instanceof IOrientatedWorld) {
			chunk = RenderUploader.getRenderChunk((IOrientatedWorld) te.getWorld(), te.getPos());
		} else {
			chunk = te.lastRenderedChunk;
			if (chunk == null) {
				te.lastRenderedChunk = RenderUploader.getRenderChunk(RenderUploader.getViewFrustum(), te.getPos());
				chunk = te.lastRenderedChunk;
			}
		}
		
		if (chunk == null) {
			System.out.println("Invalid tileentity with no rendering chunk! pos: " + te.getPos() + ", world: " + te.getWorld());
			return;
		}
		
		if (te.isEmpty() && (te.getBuffer() == null || !te.getBuffer().isEmpty())) {
			if (te.getWorld() instanceof IOrientatedWorld)
				RenderUploader.getRenderChunk((IOrientatedWorld) te.getWorld(), te.getPos()).deleteRenderData(te);
			te.setBuffer(new BlockLayerRenderBuffer());
			te.inRenderingQueue.set(false);
			synchronized (chunks) {
				if (!renderer.chunks.containsKey(chunk))
					if (te.getWorld() instanceof IOrientatedWorld)
						((LittleRenderChunk) chunk).markCompleted();
					else
						((RenderChunk) chunk).setNeedsUpdate(false);
			}
			return;
		}
		
		synchronized (chunks) {
			AtomicInteger count = renderer.chunks.get(chunk);
			if (count == null) {
				count = new AtomicInteger(0);
				renderer.chunks.put(chunk, count);
			}
			count.getAndIncrement();
		}
		renderer.updateCoords.add(new RenderingData(te, chunk));
	}
	
	static {
		initThreads(LittleTilesConfig.rendering.renderingThreadCount);
	}
	
	public ConcurrentLinkedQueue<RenderingData> updateCoords = new ConcurrentLinkedQueue<>();
	
	final int index;
	
	public RenderingThread(int index) {
		start();
		this.index = index;
	}
	
	public int getThreadIndex() {
		return index;
	}
	
	private final IBlockAccessFake fakeAccess = new IBlockAccessFake();
	public boolean active = true;
	
	@Override
	public void run() {
		while (active) {
			IBlockAccess world = mc.world;
			
			if (world != null && !updateCoords.isEmpty()) {
				RenderingData data = updateCoords.poll();
				
				try {
					data.te.buildingCache.set(true);
					BlockPos pos = data.te.getPos();
					RenderCubeLayerCache cubeCache = data.te.getCubeCache();
					
					if (data.te.getWorld() == null || !data.te.hasLoaded())
						throw new RenderingException("Tileentity is not loaded yet");
					
					for (BlockRenderLayer layer : BlockRenderLayer.values()) {
						cubeCache.setCubesByLayer(BlockTile.getRenderingCubes(data.state, data.te, null, layer), layer);
						
						List<LittleRenderingCube> cubes = cubeCache.getCubesByLayer(layer);
						for (int j = 0; j < cubes.size(); j++) {
							RenderCubeObject cube = cubes.get(j);
							if (cube.doesNeedQuadUpdate) {
								if (ArrayUtils.contains(fakeWorldMods, cube.block.getRegistryName().getResourceDomain())) {
									fakeAccess.set(data.te.getWorld(), pos, cube.getBlockState());
									world = fakeAccess;
								} else
									world = data.te.getWorld();
								
								IBlockState modelState = cube.getBlockState().getActualState(world, pos);
								IBakedModel blockModel = OptifineHelper.getRenderModel(mc.getBlockRendererDispatcher().getModelForState(modelState), world, modelState, pos);
								modelState = cube.getModelState(modelState, world, pos);
								BlockPos offset = cube.getOffset();
								for (int h = 0; h < EnumFacing.VALUES.length; h++) {
									EnumFacing facing = EnumFacing.VALUES[h];
									if (cube.shouldSideBeRendered(facing)) {
										if (cube.getQuad(facing) == null)
											cube.setQuad(facing, CreativeBakedModel.getBakedQuad(world, cube, pos, offset, modelState, blockModel, layer, facing, MathHelper.getPositionRandom(pos), false));
									} else
										cube.setQuad(facing, null);
								}
								cube.doesNeedQuadUpdate = false;
							}
						}
					}
					
					cubeCache.sortCache();
					
					world = mc.world;
					
					BlockLayerRenderBuffer layerBuffer = new BlockLayerRenderBuffer();
					if (!layerBuffer.isDrawing()) {
						data.te.renderIndex = LittleChunkDispatcher.currentRenderIndex.get();
						try {
							layerBuffer.setDrawing();
							
							if (!consumer.format.equals(DefaultVertexFormats.BLOCK))
								consumer = new CreativeCubeConsumer(DefaultVertexFormats.BLOCK, mc.getBlockColors());
							
							World renderWorld = data.te.getWorld();
							if (renderWorld instanceof SubWorld && !((SubWorld) renderWorld).shouldRender)
								renderWorld = ((SubWorld) renderWorld).parentWorld;
							
							consumer.setWorld(renderWorld);
							consumer.setBlockPos(pos);
							consumer.setState(data.state);
							consumer.getBlockInfo().updateLightMatrix();
							
							// Render vertex buffer
							for (int i = 0; i < BlockRenderLayer.values().length; i++) {
								BlockRenderLayer layer = BlockRenderLayer.values()[i];
								
								List<LittleRenderingCube> cubes = cubeCache.getCubesByLayer(layer);
								BufferBuilder buffer = null;
								
								if (cubes != null && cubes.size() > 0)
									buffer = layerBuffer.createVertexBuffer(cubes);
								
								if (buffer != null) {
									consumer.buffer = buffer;
									consumer.layer = layer;
									
									buffer.begin(7, DefaultVertexFormats.BLOCK);
									if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isRenderRegions() && !data.subWorld) {
										int bits = 8;
										int dx = data.te.lastRenderedChunk.getPosition().getX() >> bits << bits;
										int dy = data.te.lastRenderedChunk.getPosition().getY() >> bits << bits;
										int dz = data.te.lastRenderedChunk.getPosition().getZ() >> bits << bits;
										
										dx = OptifineHelper.getRenderChunkRegionX(data.te.lastRenderedChunk);
										dz = OptifineHelper.getRenderChunkRegionZ(data.te.lastRenderedChunk);
										
										int chunkX = MathHelper.intFloorDiv(pos.getX(), 16);
										int chunkY = MathHelper.intFloorDiv(pos.getY(), 16);
										int chunkZ = MathHelper.intFloorDiv(pos.getZ(), 16);
										int offsetX = pos.getX() - dx;
										int offsetY = pos.getY() - dy;
										int offsetZ = pos.getZ() - dz;
										buffer.setTranslation(offsetX, offsetY, offsetZ);
									} else {
										int chunkX = MathHelper.intFloorDiv(pos.getX(), 16);
										int chunkY = MathHelper.intFloorDiv(pos.getY(), 16);
										int chunkZ = MathHelper.intFloorDiv(pos.getZ(), 16);
										int offsetX = pos.getX() - (chunkX * 16);
										int offsetY = pos.getY() - (chunkY * 16);
										int offsetZ = pos.getZ() - (chunkZ * 16);
										buffer.setTranslation(offsetX, offsetY, offsetZ);
									}
									
									for (int j = 0; j < cubes.size(); j++) {
										RenderCubeObject cube = cubes.get(j);
										consumer.cube = cube;
										IBlockState state = cube.getBlockState();
										
										if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders()) {
											if (state.getBlock() instanceof IFakeRenderingBlock)
												state = ((IFakeRenderingBlock) state.getBlock()).getFakeState(state);
											SVertexBuilder.pushEntity(state, pos, data.te.getWorld(), buffer);
										}
										
										consumer.setState(state);
										consumer.getBlockInfo().updateShift();
										
										for (int h = 0; h < EnumFacing.VALUES.length; h++) {
											Object quadObject = cube.getQuad(EnumFacing.VALUES[h]);
											if (quadObject instanceof List) {
												List<BakedQuad> quads = (List<BakedQuad>) quadObject;
												if (quads != null && !quads.isEmpty()) {
													for (int k = 0; k < quads.size(); k++) {
														BakedQuad quad = quads.get(k);
														consumer.quad = quad;
														renderQuad(buffer, quad);
													}
												}
											} else if (quadObject instanceof BakedQuad) {
												consumer.quad = (BakedQuad) quadObject;
												renderQuad(buffer, (BakedQuad) quadObject);
											}
											
										}
										
										if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
											SVertexBuilder.popEntity(buffer);
										
										if (!LittleTilesConfig.rendering.useQuadCache)
											cube.deleteQuadCache();
									}
									
									consumer.quad = null;
									consumer.cube = null;
									
									if (FMLClientHandler.instance().hasOptifine() && OptifineHelper.isShaders())
										SVertexBuilder.calcNormalChunkLayer(buffer);
									
									buffer.finishDrawing();
									consumer.buffer = null;
									
									layerBuffer.setBufferByLayer(buffer, layer);
								}
							}
							
							layerBuffer.setFinishedDrawing();
							if (!setRendered(data, layerBuffer))
								updateCoords.add(data);
							
							consumer.setWorld(null);
							
						} catch (RenderOverlapException e) {
							updateCoords.add(data);
						} catch (Exception e) {
							e.printStackTrace();
							updateCoords.add(data);
							if (layerBuffer != null)
								layerBuffer.setFinishedDrawing();
						}
					} else
						updateCoords.add(data);
				} catch (Exception e) {
					updateCoords.add(data);
				} catch (OutOfMemoryError error) {
					updateCoords.add(data);
					error.printStackTrace();
				}
			} else if (world == null && (!updateCoords.isEmpty() || !chunks.isEmpty())) {
				updateCoords.clear();
				chunks.clear();
			} else if (world != null && !chunks.isEmpty()) {
				
			}
			try {
				sleep(1);
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}
		}
	}
	
	private CreativeCubeConsumer consumer = new CreativeCubeConsumer(DefaultVertexFormats.BLOCK, mc.getBlockColors());
	
	private synchronized void renderQuad(BufferBuilder buffer, BakedQuad quad) {
		if (quad.hasTintIndex()) {
			consumer.setQuadTint(quad.getTintIndex());
		}
		consumer.setApplyDiffuseLighting(quad.shouldApplyDiffuseLighting());
		// int[] eMap = mapFormats(consumer.getVertexFormat(),
		// DefaultVertexFormats.ITEM);
		float[] data = new float[4];
		VertexFormat formatFrom = consumer.format;
		VertexFormat formatTo = quad.getFormat();
		int countFrom = formatFrom.getElementCount();
		int countTo = formatTo.getElementCount();
		int[] eMap = LightUtil.mapFormats(formatFrom, formatTo); // getFormatMaps().getUnchecked(Pair.of(formatFrom,
		                                                         // formatTo));
		for (int v = 0; v < 4; v++) {
			for (int e = 0; e < countFrom; e++) {
				if (eMap[e] != countTo) {
					LightUtil.unpack(quad.getVertexData(), data, quad.getFormat(), v, eMap[e]);
					
					consumer.put(e, data);
				} else {
					consumer.put(e);
				}
			}
		}
	}
	
	public synchronized boolean setRendered(RenderingData data, BlockLayerRenderBuffer buffer) {
		TileEntityLittleTiles te = data.te;
		te.setBuffer(buffer);
		
		if (te.rebuildRenderingCache) {
			te.rebuildRenderingCache = false;
			te.getCubeCache().clearCache();
			te.buildingCache.set(false);
			return false;
		}
		
		te.lastRenderedChunk = null;
		te.inRenderingQueue.set(false);
		te.buildingCache.set(false);
		
		synchronized (chunks) {
			AtomicInteger count = chunks.get(data.chunk);
			if (count != null)
				count.getAndDecrement();
			
			if (data.subWorld)
				((LittleRenderChunk) data.chunk).addRenderData(te);
			
			te.clearWaitingAnimations();
			
			if (count == null || count.intValue() <= 0) {
				chunks.remove(data.chunk);
				if (data.subWorld) {
					LittleTilesProfiler.ltChunksUpdates++;
					((LittleRenderChunk) data.chunk).markCompleted();
				} else {
					LittleTilesProfiler.vanillaChunksUpdates++;
					((RenderChunk) data.chunk).setNeedsUpdate(false);
				}
			}
			
			boolean finished = true;
			for (RenderingThread thread : threads) {
				if (!thread.updateCoords.isEmpty()) {
					finished = false;
					break;
				}
			}
			if (finished)
				chunks.clear();
		}
		
		return true;
		
	}
	
	public static class RenderingException extends Exception {
		
		public RenderingException(String arg0) {
			super(arg0);
		}
	}
	
	private static class RenderingData {
		
		public TileEntityLittleTiles te;
		public IBlockState state;
		public Object chunk;
		public boolean subWorld;
		
		public RenderingData(TileEntityLittleTiles te, Object chunk) {
			this.te = te;
			this.state = te.getBlockTileState();
			this.chunk = chunk;
			this.subWorld = !(chunk instanceof RenderChunk);
		}
	}
}